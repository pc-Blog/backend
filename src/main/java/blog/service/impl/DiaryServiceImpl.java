package blog.service.impl;

import blog.dto.DiaryRequestDTO;
import blog.entity.Diary;
import blog.entity.DiaryActivity;
import blog.exception.BaseException;
import blog.mapper.DiaryActivityMapper;
import blog.mapper.DiaryMapper;
import blog.service.DiaryService;
import blog.vo.DiaryActivityVO;
import blog.vo.DiaryStatsVO;
import blog.vo.DiaryVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DiaryServiceImpl implements DiaryService {

    /** 天气枚举值下限（1=晴） */
    private static final int WEATHER_MIN = 1;
    /** 天气枚举值上限（8=雷） */
    private static final int WEATHER_MAX = 8;
    /** 分类枚举值下限（1=学习） */
    private static final int CATEGORY_MIN = 1;
    /** 分类枚举值上限（6=社交） */
    private static final int CATEGORY_MAX = 6;

    private final DiaryMapper diaryMapper;
    private final DiaryActivityMapper diaryActivityMapper;

    public DiaryServiceImpl(DiaryMapper diaryMapper, DiaryActivityMapper diaryActivityMapper) {
        this.diaryMapper = diaryMapper;
        this.diaryActivityMapper = diaryActivityMapper;
    }

    // ==================== 查询 ====================

    @Override
    public List<DiaryVO> listAll() {
        List<Diary> diaries = diaryMapper.selectList(new LambdaQueryWrapper<Diary>()
                .orderByDesc(Diary::getRecordDate));
        if (diaries.isEmpty()) {
            return new ArrayList<>();
        }

        // 一次捞出全部活动后按 diaryId 分组，避免逐天查询
        Map<Long, List<DiaryActivity>> grouped = loadActivities(
                diaries.stream().map(Diary::getId).collect(Collectors.toList()));

        return diaries.stream()
                .map(d -> toVO(d, grouped.getOrDefault(d.getId(), new ArrayList<>())))
                .collect(Collectors.toList());
    }

    @Override
    public DiaryVO get(Long id) {
        Diary diary = getExisting(id);
        return toVO(diary, selectActivities(id));
    }

    // ==================== 写入 ====================

    @Override
    @Transactional
    public Long create(DiaryRequestDTO request) {
        if (request.getRecordDate() == null) {
            throw new BaseException("日期不能为空");
        }
        checkWeather(request.getWeather());
        checkLogs(request.getLogs());
        checkNotFuture(request.getRecordDate());

        Long exists = diaryMapper.selectCount(new LambdaQueryWrapper<Diary>()
                .eq(Diary::getRecordDate, request.getRecordDate()));
        if (exists != null && exists > 0) {
            throw new BaseException("该日期的日记已存在");
        }

        Diary diary = new Diary();
        diary.setRecordDate(request.getRecordDate());
        diary.setWeather(request.getWeather());
        diaryMapper.insert(diary);

        insertActivities(diary.getId(), request.getLogs());
        return diary.getId();
    }

    @Override
    @Transactional
    public void update(DiaryRequestDTO request) {
        if (request.getId() == null) {
            throw new BaseException("日记ID不能为空");
        }
        checkWeather(request.getWeather());
        checkLogs(request.getLogs());

        // 日期不可修改：请求体里的 recordDate 一律忽略
        Diary existing = getExisting(request.getId());

        // 整天整体替换
        diaryActivityMapper.delete(new LambdaQueryWrapper<DiaryActivity>()
                .eq(DiaryActivity::getDiaryId, existing.getId()));
        insertActivities(existing.getId(), request.getLogs());

        Diary update = new Diary();
        update.setId(existing.getId());
        update.setWeather(request.getWeather());
        update.setUpdateTime(LocalDateTime.now());
        diaryMapper.updateById(update);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        getExisting(id);
        diaryActivityMapper.delete(new LambdaQueryWrapper<DiaryActivity>()
                .eq(DiaryActivity::getDiaryId, id));
        diaryMapper.deleteById(id);
    }

    // ==================== 统计 ====================

    @Override
    public DiaryStatsVO stats() {
        // 全量捞进内存聚合：存量为数百条量级，无需聚合 SQL
        List<DiaryActivity> activities = diaryActivityMapper.selectList(null);

        Map<Integer, Long> categoryCount = new LinkedHashMap<>();
        Map<Integer, Map<String, Long>> subcategoryCount = new LinkedHashMap<>();
        for (DiaryActivity a : activities) {
            if (a.getCategory() == null) {
                continue;
            }
            categoryCount.merge(a.getCategory(), 1L, Long::sum);
            if (a.getSubcategory() != null && !a.getSubcategory().isBlank()) {
                subcategoryCount
                        .computeIfAbsent(a.getCategory(), k -> new LinkedHashMap<>())
                        .merge(a.getSubcategory(), 1L, Long::sum);
            }
        }

        return new DiaryStatsVO(activities.size(), dailyCount(), categoryCount, subcategoryCount);
    }

    /** 每天的条目数：[["2025-06-20", 3], ...]，按日期升序 */
    private List<List<Object>> dailyCount() {
        List<Diary> diaries = diaryMapper.selectList(new LambdaQueryWrapper<Diary>()
                .orderByAsc(Diary::getRecordDate));
        if (diaries.isEmpty()) {
            return new ArrayList<>();
        }

        Map<Long, List<DiaryActivity>> grouped = loadActivities(
                diaries.stream().map(Diary::getId).collect(Collectors.toList()));

        List<List<Object>> result = new ArrayList<>(diaries.size());
        for (Diary d : diaries) {
            result.add(List.of(d.getRecordDate().toString(),
                    (Object) grouped.getOrDefault(d.getId(), new ArrayList<>()).size()));
        }
        return result;
    }

    @Override
    public List<String> subcategories(Integer category) {
        if (category == null) {
            throw new BaseException("分类不能为空");
        }
        checkCategory(category);

        // 小分类不是独立实体：已用过哪些值，就从活动条目里推导
        List<Object> rows = diaryActivityMapper.selectObjs(new QueryWrapper<DiaryActivity>()
                .select("DISTINCT subcategory")
                .eq("category", category)
                .isNotNull("subcategory")
                .ne("subcategory", "")
                .orderByAsc("subcategory"));

        List<String> result = new ArrayList<>(rows.size());
        for (Object row : rows) {
            if (row != null) {
                result.add(row.toString());
            }
        }
        return result;
    }

    // ==================== 内部方法 ====================

    private Diary getExisting(Long id) {
        if (id == null) {
            throw new BaseException("日记ID不能为空");
        }
        Diary diary = diaryMapper.selectById(id);
        if (diary == null) {
            throw new BaseException("日记不存在");
        }
        return diary;
    }

    /** 指定日记的活动条目，按录入顺序 */
    private List<DiaryActivity> selectActivities(Long diaryId) {
        return diaryActivityMapper.selectList(new LambdaQueryWrapper<DiaryActivity>()
                .eq(DiaryActivity::getDiaryId, diaryId)
                .orderByAsc(DiaryActivity::getId));
    }

    /** 批量取活动并按 diaryId 分组，一次查询搞定 */
    private Map<Long, List<DiaryActivity>> loadActivities(List<Long> diaryIds) {
        List<DiaryActivity> activities = diaryActivityMapper.selectList(
                new LambdaQueryWrapper<DiaryActivity>()
                        .in(DiaryActivity::getDiaryId, diaryIds)
                        .orderByAsc(DiaryActivity::getDiaryId)
                        .orderByAsc(DiaryActivity::getId));
        return activities.stream().collect(Collectors.groupingBy(DiaryActivity::getDiaryId));
    }

    private void insertActivities(Long diaryId, List<DiaryRequestDTO.LogEntryDTO> logs) {
        for (DiaryRequestDTO.LogEntryDTO log : logs) {
            DiaryActivity activity = new DiaryActivity();
            activity.setDiaryId(diaryId);
            activity.setActivity(log.getActivity());
            activity.setCategory(log.getCategory());
            activity.setSubcategory(log.getSubcategory());
            diaryActivityMapper.insert(activity);
        }
    }

    private DiaryVO toVO(Diary diary, List<DiaryActivity> activities) {
        List<DiaryActivityVO> logs = activities.stream()
                .map(a -> new DiaryActivityVO(a.getId(), a.getActivity(), a.getCategory(), a.getSubcategory()))
                .collect(Collectors.toList());
        return new DiaryVO(diary.getId(), diary.getRecordDate(), diary.getWeather(),
                diary.getCreateTime(), diary.getUpdateTime(), logs);
    }

    // ==================== 校验 ====================

    private void checkNotFuture(LocalDate date) {
        if (date.isAfter(LocalDate.now())) {
            throw new BaseException("日期不能是未来");
        }
    }

    private void checkWeather(Integer weather) {
        if (weather == null) {
            throw new BaseException("天气不能为空");
        }
        // 9=中雨，是后补的档位，故不能用 WEATHER_MAX 单一上界判断
        if ((weather < WEATHER_MIN || weather > WEATHER_MAX) && weather != 9) {
            throw new BaseException("天气取值非法: " + weather);
        }
    }

    private void checkCategory(Integer category) {
        if (category == null || category < CATEGORY_MIN || category > CATEGORY_MAX) {
            throw new BaseException("活动分类取值非法: " + category);
        }
    }

    private void checkLogs(List<DiaryRequestDTO.LogEntryDTO> logs) {
        if (logs == null || logs.isEmpty()) {
            throw new BaseException("至少需要一条活动记录");
        }
        for (DiaryRequestDTO.LogEntryDTO log : logs) {
            if (log.getActivity() == null || log.getActivity().isBlank()) {
                throw new BaseException("活动内容不能为空");
            }
            checkCategory(log.getCategory());
        }
    }
}
