package com.personalhub.resource.vo;

import com.personalhub.resource.entity.FileResource;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/** 文件资源 VO */
@Data
@Schema(description = "文件资源详情")
public class FileVO {
    @Schema(description = "文件ID")
    private Long id;

    @Schema(description = "原始文件名")
    private String name;

    @Schema(description = "扩展名")
    private String type;

    @Schema(description = "文件类型图标标识")
    private String typeIcon;

    @Schema(description = "文件类型中文标签")
    private String typeLabel;

    @Schema(description = "大小（字节）")
    private Long size;

    @Schema(description = "格式化后的大小")
    private String sizeFormatted;

    @Schema(description = "MIME 类型")
    private String mimeType;

    @Schema(description = "文件分类ID")
    private Long categoryId;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    public static FileVO from(FileResource resource) {
        FileVO vo = new FileVO();
        vo.setId(resource.getId());
        vo.setName(resource.getName());
        vo.setType(resource.getType());
        vo.setSize(resource.getSize());
        vo.setMimeType(resource.getMimeType());
        vo.setCategoryId(resource.getCategoryId());
        vo.setCreatedAt(resource.getCreatedAt());
        // 格式化文件大小
        vo.setSizeFormatted(formatSize(resource.getSize()));
        // 设置类型图标和标签
        String ext = resource.getType() != null ? resource.getType().toLowerCase() : "";
        vo.setTypeIcon(getTypeIcon(ext));
        vo.setTypeLabel(getTypeLabel(ext));
        return vo;
    }

    private static String formatSize(Long bytes) {
        if (bytes == null) return "0 B";
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }

    private static String getTypeIcon(String ext) {
        return switch (ext) {
            case "jpg", "jpeg", "png", "gif", "svg", "webp", "bmp" -> "image";
            case "pdf" -> "pdf";
            case "doc", "docx" -> "word";
            case "xls", "xlsx" -> "excel";
            case "ppt", "pptx" -> "ppt";
            case "md", "txt" -> "text";
            case "zip", "rar", "7z", "tar", "gz" -> "archive";
            default -> "file";
        };
    }

    private static String getTypeLabel(String ext) {
        return switch (ext) {
            case "jpg", "jpeg", "png", "gif", "svg", "webp", "bmp" -> "图片";
            case "pdf" -> "PDF";
            case "doc", "docx" -> "Word";
            case "xls", "xlsx" -> "Excel";
            case "ppt", "pptx" -> "PPT";
            case "md" -> "Markdown";
            case "txt" -> "文本";
            case "zip", "rar", "7z" -> "压缩包";
            default -> ext.toUpperCase();
        };
    }
}
