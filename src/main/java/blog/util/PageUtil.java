package blog.util;

import blog.common.PageDTO;
import blog.common.SortDirection;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public class PageUtil {

    public static <T> Page<T> toPage(PageDTO<?> dto) {
        Page<T> page = Page.of(dto.getPageNum(), dto.getPageSize());
        if (dto.getSortFields() != null && !dto.getSortFields().isEmpty()) {
            for (PageDTO.SortField sf : dto.getSortFields()) {
                String column = camelToSnake(sf.getField());
                if (sf.getDirection() == SortDirection.DESC) {
                    page.addOrder(OrderItem.desc(column));
                } else {
                    page.addOrder(OrderItem.asc(column));
                }
            }
        } else {
            page.addOrder(OrderItem.desc("id"));
        }
        return page;
    }

    private static String camelToSnake(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }
}
