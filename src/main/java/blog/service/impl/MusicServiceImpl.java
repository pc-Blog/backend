package blog.service.impl;

import blog.common.PageDTO;
import blog.common.PageVO;
import blog.dto.MusicQueryDTO;
import blog.dto.PlayRequest;
import blog.entity.Music;
import blog.entity.MusicCategory;
import blog.entity.Singer;
import blog.exception.BaseException;
import blog.mapper.MusicCategoryMapper;
import blog.mapper.MusicMapper;
import blog.mapper.SingerMapper;
import blog.service.AboutService;
import blog.service.MediaService;
import blog.service.MusicService;
import blog.util.PageUtil;
import blog.vo.MusicVO;
import blog.vo.PlayResultVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MusicServiceImpl extends ServiceImpl<MusicMapper, Music> implements MusicService {

    /** 累计播放时长在 t_about 中的 key（is_system=1） */
    private static final String PLAY_DURATION_KEY = "music_play_duration";

    private static final String MODE_LOOP = "loop";
    private static final String MODE_REVERSE = "reverse";
    private static final String MODE_SINGLE = "single";
    private static final String MODE_RANDOM = "random";

    private final SingerMapper singerMapper;
    private final MusicCategoryMapper musicCategoryMapper;
    private final MediaService mediaService;
    private final AboutService aboutService;

    public MusicServiceImpl(SingerMapper singerMapper,
                            MusicCategoryMapper musicCategoryMapper,
                            MediaService mediaService,
                            AboutService aboutService) {
        this.singerMapper = singerMapper;
        this.musicCategoryMapper = musicCategoryMapper;
        this.mediaService = mediaService;
        this.aboutService = aboutService;
    }

    // ==================== 查询 ====================

    @Override
    public PageVO<MusicVO> page(PageDTO<MusicQueryDTO> dto) {
        LambdaQueryWrapper<Music> wrapper = buildWrapper(dto.getQuery());
        var page = PageUtil.<Music>toPage(dto);
        page(page, wrapper);
        return new PageVO<>(page.getTotal(), toVOList(page.getRecords()));
    }

    @Override
    public MusicVO detail(Long id) {
        Music music = getById(id);
        if (music == null) {
            throw new BaseException("音乐不存在");
        }
        return toVO(music);
    }

    @Override
    public long getTotalPlayDuration() {
        String v = aboutService.getSystemValue(PLAY_DURATION_KEY);
        if (v == null || v.isBlank()) {
            return 0L;
        }
        try {
            return Long.parseLong(v.trim());
        } catch (NumberFormatException e) {
            log.warn("music_play_duration 值非法: {}", v);
            return 0L;
        }
    }

    // ==================== 增删改 ====================

    @Override
    @Transactional
    public List<Long> batchUpload(MultipartFile[] files, Long singerId, Long categoryId) {
        if (files == null || files.length == 0) {
            throw new BaseException("请选择要上传的音频文件");
        }
        checkSingerValid(singerId);
        checkCategoryValid(categoryId);

        List<Long> ids = new ArrayList<>(files.length);
        for (MultipartFile file : files) {
            // 音频本体统一登记到 t_media（relation_type='music'），返回的 fileUrl 作为引用副本
            var media = mediaService.upload(file, "music");

            Music music = new Music();
            music.setTitle(stripExtension(file.getOriginalFilename()));
            music.setFileUrl(media.getFileUrl());
            music.setDuration(0);
            music.setPlayCount(0);
            music.setIsFavorite(false);
            music.setSingerId(singerId);
            music.setCategoryId(categoryId);
            save(music);

            ids.add(music.getId());
        }
        return ids;
    }

    @Override
    public void updateMusic(Music music) {
        if (music.getId() == null) {
            throw new BaseException("音乐ID不能为空");
        }
        if (getById(music.getId()) == null) {
            throw new BaseException("音乐不存在");
        }
        if (music.getTitle() != null && music.getTitle().isBlank()) {
            throw new BaseException("歌曲标题不能为空");
        }
        checkSingerValid(music.getSingerId());
        checkCategoryValid(music.getCategoryId());
        // 只更新业务字段，file_url / duration / play_count 不由此接口改动
        LambdaUpdateWrapper<Music> wrapper = new LambdaUpdateWrapper<Music>()
                .eq(Music::getId, music.getId())
                // 歌手与分类必须用语句级 set 写入：字段级策略会跳过 null，分类就永远清不掉
                .set(Music::getSingerId, music.getSingerId())
                .set(Music::getCategoryId, music.getCategoryId())
                // update(Wrapper) 不带实体，FieldFill.INSERT_UPDATE 不触发，更新时间必须手动写
                .set(Music::getUpdateTime, LocalDateTime.now());
        if (music.getTitle() != null) {
            wrapper.set(Music::getTitle, music.getTitle());
        }
        if (music.getIsFavorite() != null) {
            wrapper.set(Music::getIsFavorite, music.getIsFavorite());
        }
        update(wrapper);
    }

    @Override
    public void deleteMusic(Long id) {
        if (getById(id) == null) {
            throw new BaseException("音乐不存在");
        }
        // 逻辑删除：只置 deleted=1，不动 t_media、不动 MinIO 文件。
        // 文件由前端孤儿扫描在处理（扫描时须过滤 deleted=0）
        removeById(id);
    }

    @Override
    public boolean toggleFavorite(Long id) {
        Music music = getById(id);
        if (music == null) {
            throw new BaseException("音乐不存在");
        }
        boolean next = !Boolean.TRUE.equals(music.getIsFavorite());
        Music update = new Music();
        update.setId(id);
        update.setIsFavorite(next);
        updateById(update);
        return next;
    }

    // ==================== 播放 ====================

    @Override
    @Transactional
    public PlayResultVO play(PlayRequest request) {
        String mode = request.getPlayMode() == null ? MODE_LOOP : request.getPlayMode();
        if (!MODE_LOOP.equals(mode) && !MODE_REVERSE.equals(mode)
                && !MODE_SINGLE.equals(mode) && !MODE_RANDOM.equals(mode)) {
            throw new BaseException("不支持的播放模式: " + mode);
        }

        Music current = request.getCurrentMusicId() == null
                ? null : getById(request.getCurrentMusicId());
        Music next = selectNext(current, mode, request.getQuery());

        if (next == null) {
            throw new BaseException("没有可播放的音乐");
        }

        // last_played 永远更新给下一首（手动/自动切换都会更新）
        Music touch = new Music();
        touch.setId(next.getId());
        touch.setLastPlayed(LocalDateTime.now());
        updateById(touch);

        // play_count 与累计时长仅在自动切换时累加
        if (Boolean.TRUE.equals(request.getAddPlay()) && current != null) {
            Music counted = new Music();
            counted.setId(current.getId());
            counted.setPlayCount((current.getPlayCount() == null ? 0 : current.getPlayCount()) + 1);
            updateById(counted);

            int seconds = current.getDuration() == null ? 0 : current.getDuration();
            if (seconds > 0) {
                aboutService.setSystemValue(PLAY_DURATION_KEY,
                        String.valueOf(getTotalPlayDuration() + seconds));
            }
        }

        PlayResultVO result = new PlayResultVO();
        result.setNextMusic(toVO(next));
        result.setPosition(calcPosition(next.getId(), request.getQuery(), request.getPageSize()));
        return result;
    }

    /**
     * 按播放模式选下一首。
     *
     * <p>{@code loop} / {@code reverse} 为环形：到尾回到第一首、到头回到最后一首，
     * 因此顺序播放不会卡死（源项目在边界返回自身）。</p>
     */
    private Music selectNext(Music current, String mode, MusicQueryDTO query) {
        if (MODE_SINGLE.equals(mode) && current != null) {
            // 单曲循环：原地重播
            return current;
        }

        List<Music> scope = list(buildWrapper(query).orderByDesc(Music::getId));
        if (scope.isEmpty()) {
            return null;
        }
        if (current == null) {
            // 首次播放：顺序取第一首，随机取任意一首
            return MODE_RANDOM.equals(mode)
                    ? scope.get(ThreadLocalRandom.current().nextInt(scope.size()))
                    : scope.get(0);
        }

        if (MODE_RANDOM.equals(mode)) {
            if (scope.size() == 1) {
                return scope.get(0);
            }
            // 避免随机到当前这首，否则表现为「没有切换」
            Music picked;
            do {
                picked = scope.get(ThreadLocalRandom.current().nextInt(scope.size()));
            } while (Objects.equals(picked.getId(), current.getId()));
            return picked;
        }

        int idx = indexOfId(scope, current.getId());
        if (idx < 0) {
            // 当前歌不在过滤范围内（例如切换了筛选条件），顺序取第一首
            return scope.get(0);
        }
        int size = scope.size();
        int nextIdx = MODE_REVERSE.equals(mode)
                ? (idx - 1 + size) % size   // 环形往前
                : (idx + 1) % size;         // 环形往后
        return scope.get(nextIdx);
    }

    /** 计算歌曲在过滤条件下的页码（从 1 开始） */
    private int calcPosition(Long musicId, MusicQueryDTO query, Integer pageSize) {
        int size = (pageSize == null || pageSize <= 0) ? 10 : pageSize;
        List<Music> scope = list(buildWrapper(query).orderByDesc(Music::getId));
        int idx = indexOfId(scope, musicId);
        return idx < 0 ? 1 : idx / size + 1;
    }

    private int indexOfId(List<Music> list, Long id) {
        for (int i = 0; i < list.size(); i++) {
            if (Objects.equals(list.get(i).getId(), id)) {
                return i;
            }
        }
        return -1;
    }

    private LambdaQueryWrapper<Music> buildWrapper(MusicQueryDTO query) {
        var wrapper = new LambdaQueryWrapper<Music>().eq(Music::getDeleted, 0);
        if (query == null) {
            return wrapper;
        }
        if (query.getTitle() != null && !query.getTitle().isBlank()) {
            wrapper.like(Music::getTitle, query.getTitle());
        }
        if (query.getSingerId() != null) {
            wrapper.eq(Music::getSingerId, query.getSingerId());
        }
        if (query.getCategoryId() != null) {
            wrapper.eq(Music::getCategoryId, query.getCategoryId());
        }
        if (Boolean.TRUE.equals(query.getOnlyFavorite())) {
            wrapper.eq(Music::getIsFavorite, true);
        }
        return wrapper;
    }

    // ==================== VO 组装 ====================

    private List<MusicVO> toVOList(List<Music> list) {
        if (list == null || list.isEmpty()) {
            return List.of();
        }
        // 批量取歌手与分类，避免逐行 selectById 造成 N+1
        var singerIds = list.stream().map(Music::getSingerId)
                .filter(Objects::nonNull).distinct().toList();
        var categoryIds = list.stream().map(Music::getCategoryId)
                .filter(Objects::nonNull).distinct().toList();

        Map<Long, Singer> singers = singerIds.isEmpty() ? Map.of()
                : singerMapper.selectBatchIds(singerIds).stream()
                        .collect(Collectors.toMap(Singer::getId, Function.identity()));
        Map<Long, MusicCategory> categories = categoryIds.isEmpty() ? Map.of()
                : musicCategoryMapper.selectBatchIds(categoryIds).stream()
                        .collect(Collectors.toMap(MusicCategory::getId, Function.identity()));

        return list.stream().map(m -> toVO(m, singers, categories)).toList();
    }

    private MusicVO toVO(Music music) {
        Map<Long, Singer> singers = music.getSingerId() == null ? Map.of()
                : singerMapper.selectBatchIds(List.of(music.getSingerId())).stream()
                        .collect(Collectors.toMap(Singer::getId, Function.identity()));
        Map<Long, MusicCategory> categories = music.getCategoryId() == null ? Map.of()
                : musicCategoryMapper.selectBatchIds(List.of(music.getCategoryId())).stream()
                        .collect(Collectors.toMap(MusicCategory::getId, Function.identity()));
        return toVO(music, singers, categories);
    }

    private MusicVO toVO(Music music, Map<Long, Singer> singers, Map<Long, MusicCategory> categories) {
        MusicVO vo = new MusicVO();
        vo.setId(music.getId());
        vo.setTitle(music.getTitle());
        vo.setFileUrl(music.getFileUrl());
        vo.setDuration(music.getDuration());
        vo.setPlayCount(music.getPlayCount());
        vo.setIsFavorite(music.getIsFavorite());
        vo.setLastPlayed(music.getLastPlayed());
        vo.setSingerId(music.getSingerId());
        vo.setCategoryId(music.getCategoryId());
        vo.setCreateTime(music.getCreateTime());
        vo.setUpdateTime(music.getUpdateTime());

        // 歌手的删除不级联到歌曲：名称解析不到即保持 null，前端不渲染该标签
        Singer singer = music.getSingerId() == null ? null : singers.get(music.getSingerId());
        if (singer != null) {
            vo.setSingerName(singer.getName());
            vo.setSingerPictureUrl(singer.getPictureUrl());
        }
        MusicCategory category = music.getCategoryId() == null ? null : categories.get(music.getCategoryId());
        if (category != null) {
            vo.setCategoryName(category.getName());
        }
        return vo;
    }

    // ==================== 校验 ====================

    private void checkSingerValid(Long singerId) {
        if (singerId == null) {
            return;   // NULL 表示未分配歌手
        }
        Singer singer = singerMapper.selectById(singerId);
        if (singer == null || singer.getDeleted() == 1) {
            throw new BaseException("歌手不存在");
        }
    }

    private void checkCategoryValid(Long categoryId) {
        if (categoryId == null) {
            return;   // NULL 表示未分配分类
        }
        MusicCategory category = musicCategoryMapper.selectById(categoryId);
        if (category == null || category.getDeleted() == 1) {
            throw new BaseException("分类不存在");
        }
    }

    private static String stripExtension(String filename) {
        if (filename == null || filename.isBlank()) {
            return "未命名";
        }
        int dot = filename.lastIndexOf('.');
        String name = dot > 0 ? filename.substring(0, dot) : filename;
        return name.isBlank() ? "未命名" : name;
    }
}
