package blog.service;

import blog.entity.About;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

public interface AboutService extends IService<About> {

    // ==================== 前端「关于」管理页：只读写 is_system=0 ====================

    /** 读取全部前端可编辑项（is_system=0），供关于页整页展示 */
    Map<String, String> getAboutMap();

    /** 整页保存：只覆盖 is_system=0 的项，系统项不受影响 */
    void updateAboutMap(Map<String, String> map);

    // ==================== 后端模块内部使用：只读写 is_system=1 ====================

    /**
     * 读取系统项（is_system=1）的值。
     *
     * <p>供后端模块保存自己的全局单值，例如音乐模块的累计播放时长。
     * 与 {@link #getAboutMap()} 互不干扰：前者面向关于页，此处面向系统值。</p>
     *
     * @return 值；该项不存在时返回 {@code null}
     */
    String getSystemValue(String key);

    /**
     * 写入系统项（is_system=1）的值，不存在则新建。
     *
     * <p>{@code is_system} 由本方法固定为 1，调用方无法指定，
     * 因此不会误改关于页的配置项；已有项只更新值，不改变其归属。</p>
     */
    void setSystemValue(String key, String value);
}
