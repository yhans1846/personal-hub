package com.personalhub.backup;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 备份包清单。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BackupManifest {

    public static final int SCHEMA_VERSION = 1;
    public static final String APP = "personal-hub";

    private int schemaVersion;
    private String app;
    private String createdAt;
    private Long sourceUserId;
    @Builder.Default
    private List<String> modules = new ArrayList<>();
    @Builder.Default
    private List<String> warnings = new ArrayList<>();
}
