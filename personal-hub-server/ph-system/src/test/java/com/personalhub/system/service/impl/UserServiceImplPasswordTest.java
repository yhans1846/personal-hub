package com.personalhub.system.service.impl;

import com.personalhub.common.exception.BusinessException;
import com.personalhub.system.entity.User;
import com.personalhub.system.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplPasswordTest {

    @Mock UserMapper userMapper;
    @Mock PasswordEncoder passwordEncoder;

    UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userMapper, passwordEncoder);
    }

    @Test
    void updatePassword_wrongOld_rejected() {
        User user = new User();
        user.setId(1L);
        user.setPassword("hash");
        when(userMapper.selectById(1L)).thenReturn(user);
        when(passwordEncoder.matches("bad", "hash")).thenReturn(false);

        assertThrows(BusinessException.class,
                () -> userService.updatePassword(1L, "bad", "newpass"));
        verify(userMapper, never()).updateById(isA(User.class));
    }
}
