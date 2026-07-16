package com.personalhub.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.personalhub.system.entity.UserLayout;
import com.personalhub.system.mapper.UserLayoutMapper;
import com.personalhub.system.service.UserLayoutService;
import com.personalhub.system.vo.LayoutVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户布局配置 Service 实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserLayoutServiceImpl implements UserLayoutService {

    private final UserLayoutMapper userLayoutMapper;

    @Override
    public LayoutVO get(Long userId, String layoutType) {
        UserLayout entity = userLayoutMapper.selectByUserAndType(userId, layoutType);
        return entity != null ? LayoutVO.from(entity) : null;
    }

    @Override
    public List<LayoutVO> getAll(Long userId) {
        List<UserLayout> list = userLayoutMapper.selectList(
                new LambdaQueryWrapper<UserLayout>().eq(UserLayout::getUserId, userId));
        return list.stream().map(LayoutVO::from).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void save(Long userId, String layoutType, String layoutJson) {
        UserLayout existing = userLayoutMapper.selectByUserAndType(userId, layoutType);
        if (existing != null) {
            existing.setLayoutJson(layoutJson);
            userLayoutMapper.updateById(existing);
            log.info("更新布局配置: userId={}, type={}", userId, layoutType);
        } else {
            var entity = UserLayout.builder()
                    .userId(userId)
                    .layoutType(layoutType)
                    .layoutJson(layoutJson)
                    .build();
            userLayoutMapper.insert(entity);
            log.info("创建布局配置: userId={}, type={}", userId, layoutType);
        }
    }

    @Override
    @Transactional
    public void importLayout(Long userId, String layoutJson) {
        log.info("导入布局配置: userId={}", userId);
    }

    @Override
    @Transactional
    public void delete(Long userId, String layoutType) {
        userLayoutMapper.delete(
                new LambdaQueryWrapper<UserLayout>()
                        .eq(UserLayout::getUserId, userId)
                        .eq(UserLayout::getLayoutType, layoutType));
        log.info("删除布局配置: userId={}, type={}", userId, layoutType);
    }
}
