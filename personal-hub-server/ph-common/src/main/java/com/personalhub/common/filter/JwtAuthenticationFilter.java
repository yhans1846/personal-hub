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
import java.util.regex.Pattern;

/**
 * JWT 认证过滤器。
 * query 参数 token 仅允许用于笔记静态资源（img 无法带 Authorization header）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /** /api/notes/{id}/images|attachments/... */
    private static final Pattern NOTE_STATIC_RESOURCE =
            Pattern.compile("^/api/notes/\\d+/(images|attachments)/.+$");

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        String token = null;
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        } else if (allowQueryToken(request.getRequestURI())) {
            token = request.getParameter("token");
        }

        if (token != null && !token.isBlank()) {
            if (jwtUtil.validateToken(token)) {
                Long userId = jwtUtil.getUserId(token);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                String.valueOf(userId), null, Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.trace("JWT 认证通过: userId={}, uri={}", userId, request.getRequestURI());
            } else {
                log.trace("JWT 令牌无效: uri={}", request.getRequestURI());
            }
        }

        filterChain.doFilter(request, response);
    }

    static boolean allowQueryToken(String uri) {
        if (uri == null || uri.isBlank()) {
            return false;
        }
        String path = uri;
        int q = path.indexOf('?');
        if (q >= 0) {
            path = path.substring(0, q);
        }
        return NOTE_STATIC_RESOURCE.matcher(path).matches();
    }
}
