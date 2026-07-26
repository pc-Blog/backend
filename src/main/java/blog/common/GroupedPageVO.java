package blog.common;

import lombok.Getter;

import java.util.List;

@Getter
public class GroupedPageVO<T> extends PageVO<T> {
    private final long articleTotal;

    public GroupedPageVO(long displayTotal, long articleTotal, List<T> rows) {
        super(displayTotal, rows);
        this.articleTotal = articleTotal;
    }
}
