package com.personalhub.system.controller;

import com.personalhub.common.result.Result;
import com.personalhub.common.util.CurrentUser;
import com.personalhub.system.dto.LayoutSaveDTO;
import com.personalhub.system.service.UserLayoutService;
import com.personalhub.system.vo.LayoutVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "布局配置")
@RestController
@RequestMapping("/api/layout")
@RequiredArgsConstructor
public class LayoutController {

    private final UserLayoutService userLayoutService;

    @Operation(summary = "获取用户所有布局配置")
    @GetMapping
    public Result<List<LayoutVO>> getAll(Authentication auth) {
        Long userId = CurrentUser.id(auth);
        return Result.success(userLayoutService.getAll(userId));
    }

    @Operation(summary = "获取指定类型布局配置")
    @GetMapping("/{layoutType}")
    public Result<LayoutVO> get(Authentication auth, @PathVariable String layoutType) {
        Long userId = CurrentUser.id(auth);
        LayoutVO vo = userLayoutService.get(userId, layoutType);
        return Result.success(vo != null ? vo : new LayoutVO());
    }

    @Operation(summary = "保存或更新布局配置")
    @PutMapping
    public Result<Void> save(Authentication auth, @Valid @RequestBody LayoutSaveDTO dto) {
        Long userId = CurrentUser.id(auth);
        userLayoutService.save(userId, dto.getLayoutType(), dto.getLayoutJson());
        return Result.success();
    }

    @Operation(summary = "导入布局配置")
    @PostMapping("/import")
    public Result<Void> importLayout(Authentication auth, @RequestBody String layoutJson) {
        Long userId = CurrentUser.id(auth);
        userLayoutService.importLayout(userId, layoutJson);
        return Result.success();
    }

    @Operation(summary = "恢复默认布局（删除用户配置）")
    @DeleteMapping("/{layoutType}")
    public Result<Void> resetToDefault(Authentication auth, @PathVariable String layoutType) {
        Long userId = CurrentUser.id(auth);
        userLayoutService.delete(userId, layoutType);
        return Result.success();
    }
}
