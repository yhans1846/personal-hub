package com.personalhub.backup;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 笔记-分类关联（备份用）。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoteCategoryRelRow {
    private Long noteId;
    private Long categoryId;
}
