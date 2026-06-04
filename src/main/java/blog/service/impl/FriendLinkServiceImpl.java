package blog.service.impl;

import blog.common.PageDTO;
import blog.common.PageVO;
import blog.entity.FriendLink;
import blog.mapper.FriendLinkMapper;
import blog.service.FriendLinkService;
import blog.util.PageUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FriendLinkServiceImpl extends ServiceImpl<FriendLinkMapper, FriendLink> implements FriendLinkService {

    @Override
    public PageVO<FriendLink> page(PageDTO<FriendLink> dto) {
        var wrapper = new LambdaQueryWrapper<FriendLink>().eq(FriendLink::getDeleted, 0);
        FriendLink query = dto.getQuery();
        if (query != null && query.getName() != null && !query.getName().isBlank())
            wrapper.like(FriendLink::getName, query.getName());
        var page = PageUtil.<FriendLink>toPage(dto);
        page(page, wrapper);
        return new PageVO<>(page.getTotal(), page.getRecords());
    }

    @Override
    public List<FriendLink> getPublishedList() {
        return lambdaQuery()
                .eq(FriendLink::getDeleted, 0)
                .eq(FriendLink::getIsPublished, 1)
                .orderByAsc(FriendLink::getSortOrder)
                .orderByDesc(FriendLink::getId)
                .list();
    }
}
