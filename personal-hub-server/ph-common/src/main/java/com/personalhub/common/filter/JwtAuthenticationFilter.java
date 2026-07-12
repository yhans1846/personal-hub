package com.personalhub.common.filter;

import com.personalhub.common.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT 认证过滤器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        String token = null;
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        } else {
            // 兜底：从 query 参数读取 token（用于 <img> 标签等无法自定义 header 的场景）
            token = request.getParameter("token");
        }

        if (token != null && !token.isBlank()) {
            if (jwtUtil.validateToken(token)) {
                Long userId = jwtUtil.getUserId(token);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                String.valueOf(userId), null, Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("JWT 认证通过: userId={}, uri={}", userId, request.getRequestURI());
            } else {
                log.debug("JWT 令牌无效: uri={}", request.getRequestURI());
            }
        }

        filterChain.doFilter(request, response);
    }
}
