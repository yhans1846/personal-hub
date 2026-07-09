package com.personalhub;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

/**
 * Personal Hub 启动类
 */
@Slf4j
@SpringBootApplication
public class PersonalHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(PersonalHubApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onReady(ApplicationReadyEvent event) {
        String port = event.getApplicationContext().getEnvironment()
                .getProperty("server.port", "8080");
        log.info("\n=========================================================" +
                "\n  Personal Hub 启动完成！（port: {}）" +
                "\n  访问地址: http://localhost:{}" +
                "\n  API 文档: http://localhost:{}/swagger-ui.html" +
                "\n=========================================================",
                port, port, port);
    }
}
