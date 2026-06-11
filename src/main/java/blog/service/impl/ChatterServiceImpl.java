package blog.service.impl;

import blog.common.PageDTO;
import blog.common.PageVO;
import blog.entity.Chatter;
import blog.entity.ChatterImage;
import blog.mapper.ChatterImageMapper;
import blog.mapper.ChatterMapper;
import blog.service.ChatterService;
import blog.util.PageUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatterServiceImpl extends ServiceImpl<ChatterMapper, Chatter> implements ChatterService {

    @Autowired
    private ChatterImageMapper chatterImageMapper;

    @Override
    @Transactional
    public boolean save(Chatter chatter) {
        boolean ok = super.save(chatter);
        if (ok && chatter.getImages() != null && !chatter.getImages().isEmpty()) {
            saveImages(chatter.getId(), chatter.getImages());
        }
        return ok;
    }

    @Override
    @Transactional
    public boolean updateById(Chatter chatter) {
        boolean ok = super.updateById(chatter);
        if (chatter.getImages() != null) {
            chatterImageMapper.delete(
                new LambdaQueryWrapper<ChatterImage>().eq(ChatterImage::getChatterId, chatter.getId()));
            if (!chatter.getImages().isEmpty()) {
                saveImages(chatter.getId(), chatter.getImages());
            }
        }
        return ok;
    }

    @Override
    public Chatter getById(java.io.Serializable id) {
        Chatter chatter = super.getById(id);
        if (chatter != null) {
            chatter.setImages(loadImages(chatter.getId()));
        }
        return chatter;
    }

    @Override
    public List<Chatter> getPublishedListWithImages() {
        List<Chatter> list = lambdaQuery()
                .eq(Chatter::getDeleted, 0)
                .eq(Chatter::getIsPublished, 1)
                .orderByDesc(Chatter::getCreateTime)
                .list();
        for (Chatter c : list) {
            c.setImages(loadImages(c.getId()));
        }
        return list;
    }

    @Override
    public PageVO<Chatter> page(PageDTO<Chatter> dto) {
        var wrapper = new LambdaQueryWrapper<Chatter>().eq(Chatter::getDeleted, 0);
        Chatter query = dto.getQuery();
        if (query != null && query.getContent() != null && !query.getContent().isBlank())
            wrapper.like(Chatter::getContent, query.getContent());
        wrapper.orderByDesc(Chatter::getCreateTime);
        var page = PageUtil.<Chatter>toPage(dto);
        page(page, wrapper);
        for (Chatter c : page.getRecords()) {
            c.setImages(loadImages(c.getId()));
        }
        return new PageVO<>(page.getTotal(), page.getRecords());
    }

    private void saveImages(Long chatterId, List<String> urls) {
        for (int i = 0; i < urls.size(); i++) {
            ChatterImage ci = new ChatterImage();
            ci.setChatterId(chatterId);
            ci.setUrl(urls.get(i));
            ci.setSortOrder(i);
            chatterImageMapper.insert(ci);
        }
    }

    private List<String> loadImages(Long chatterId) {
        return chatterImageMapper.selectList(
                new LambdaQueryWrapper<ChatterImage>()
                        .eq(ChatterImage::getChatterId, chatterId)
                        .orderByAsc(ChatterImage::getSortOrder))
                .stream()
                .map(ChatterImage::getUrl)
                .collect(Collectors.toList());
    }
}
