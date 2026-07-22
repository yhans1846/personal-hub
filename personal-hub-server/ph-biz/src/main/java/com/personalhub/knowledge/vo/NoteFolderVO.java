package com.personalhub.knowledge.vo;

import com.personalhub.knowledge.entity.NoteFolder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "笔记文件夹树节点")
public class NoteFolderVO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "父ID，根为 null")
    private Long parentId;

    @Schema(description = "同级排序")
    private Integer sortOrder;

    @Schema(description = "直属笔记数（不含子夹、不含回收站）")
    private long noteCount;

    @Schema(description = "直属笔记摘要（侧栏/首页目录）")
    private List<NoteFolderNoteItem> notes = new ArrayList<>();

    @Schema(description = "子节点")
    private List<NoteFolderVO> children = new ArrayList<>();

    public static NoteFolderVO from(NoteFolder f) {
        NoteFolderVO vo = new NoteFolderVO();
        vo.setId(f.getId());
        vo.setName(f.getName());
        vo.setParentId(f.getParentId());
        vo.setSortOrder(f.getSortOrder() != null ? f.getSortOrder() : 0);
        vo.setNoteCount(0);
        return vo;
    }
}
