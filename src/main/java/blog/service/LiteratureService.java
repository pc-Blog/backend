package blog.service;

import blog.common.PageDTO;
import blog.common.PageVO;
import blog.dto.LiteratureQueryDTO;
import blog.entity.Category;
import blog.entity.Literature;

import java.util.List;

public interface LiteratureService {

    /** 访客端：全部已发布作品，按分类 sort_order + 写作日期降序 */
    List<Literature> publicList();

    /** 访客端：已发布总数 */
    long publicCount();

    /** 访客端：单篇详情（仅已发布） */
    Literature publicDetail(Long id);

    /** 文学分类下拉选项（按 sort_order 升序） */
    List<Category> categoryOptions();

    /** 管理端：分页查询（含隐藏作品） */
    PageVO<Literature> adminPage(PageDTO<LiteratureQueryDTO> dto);

    /** 管理端：单篇详情（含隐藏作品） */
    Literature adminDetail(Long id);

    /** 新增作品 */
    void save(Literature literature);

    /** 更新作品 */
    void update(Literature literature);

    /** 逻辑删除作品 */
    void delete(Long id);

    /** 校验分类存在且属于 LITERATURE 类型 */
    void checkCategoryValid(Long categoryId);

    void publish(Long id);

    void unpublish(Long id);
}
