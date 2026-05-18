package blog.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProjectDetailVO extends ProjectListVO {
    private String content;
    private ProjectPrevNextVO prev;
    private ProjectPrevNextVO next;

    @Data
    public static class ProjectPrevNextVO {
        private Long id;
        private String name;

        public ProjectPrevNextVO(Long id, String name) {
            this.id = id;
            this.name = name;
        }
    }
}
