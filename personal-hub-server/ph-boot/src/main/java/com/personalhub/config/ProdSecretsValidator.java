package com.personalhub.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 生产环境密钥校验：缺少或使用弱默认值时拒绝启动。
 */
@Component
@Profile("prod")
public class ProdSecretsValidator {

    private static final List<String> REJECTED_SECRETS = List.of(
            "123456",
            "password",
            "personal-hub-prod-jwt-secret-change-me-in-production",
            "change-me-mysql-password",
            "change-me-redis-password",
            "change-me-to-a-long-random-jwt-secret"
    );

    @Value("${jwt.secret:}")
    private String jwtSecret;

    @Value("${spring.datasource.password:}")
    private String mysqlPassword;

    @Value("${spring.data.redis.password:}")
    private String redisPassword;

    @PostConstruct
    public void validate() {
        requireStrong("JWT_SECRET / jwt.secret", jwtSecret);
        requireStrong("MYSQL_PASSWORD / spring.datasource.password", mysqlPassword);
        requireStrong("REDIS_PASSWORD / spring.data.redis.password", redisPassword);
    }

    private void requireStrong(String name, String value) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalStateException(
                    "生产环境必须设置 " + name + "，禁止空值或使用默认密钥启动");
        }
        String trimmed = value.trim();
        if (REJECTED_SECRETS.stream().anyMatch(r -> r.equalsIgnoreCase(trimmed))) {
            throw new IllegalStateException(
                    "生产环境 " + name + " 使用了不安全的默认值，请通过环境变量更换");
        }
        if (name.startsWith("JWT_SECRET") && trimmed.length() < 32) {
            throw new IllegalStateException(
                    "生产环境 " + name + " 长度过短（至少 32 位）");
        }
        if (!name.startsWith("JWT_SECRET") && trimmed.length() < 8) {
            throw new IllegalStateException(
                    "生产环境 " + name + " 长度过短（至少 8 位）");
        }
    }
}
