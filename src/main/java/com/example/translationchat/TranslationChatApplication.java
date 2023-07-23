package com.example.translationchat;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.example.translationchat.client.domain.repository")
@EnableJpaAuditing
@EntityScan("com.example.translationchat.client.domain.model")
@ServletComponentScan
@EnableWebSecurity
@RequiredArgsConstructor
@SpringBootApplication
public class TranslationChatApplication {

    public static void main(String[] args) {
        SpringApplication.run(TranslationChatApplication.class, args);
    }

}
