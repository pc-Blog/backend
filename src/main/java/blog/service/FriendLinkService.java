package blog.service;

import blog.common.PageDTO;
import blog.common.PageVO;
import blog.entity.FriendLink;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface FriendLinkService extends IService<FriendLink> {
    PageVO<FriendLink> page(PageDTO<FriendLink> dto);
    List<FriendLink> getPublishedList();
}
