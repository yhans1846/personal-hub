package com.personalhub.backup;

import com.personalhub.common.result.Result;
import com.personalhub.common.util.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户数据清空
 */
@Tag(name = "数据管理", description = "清空业务数据")
@Validated
@RestController
@RequestMapping("/api/data")
@RequiredArgsConstructor
public class DataPurgeController {

    private final DataPurgeService dataPurgeService;

    @Operation(summary = "清空数据", description = "验证码通过后先备份再清空业务数据；保留账号/资料/user_layout；历史只留刚打的备份")
    @PostMapping("/purge")
    public Result<DataPurgeVO> purge(
            @Parameter(hidden = true) Authentication authentication,
            @Valid @RequestBody DataPurgeDTO dto) {
        Long userId = CurrentUser.id(authentication);
        return Result.success(dataPurgeService.purge(userId, dto.getCaptchaId(), dto.getCaptchaCode()));
    }
}
