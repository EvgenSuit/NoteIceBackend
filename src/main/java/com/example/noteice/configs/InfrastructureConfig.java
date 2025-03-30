package com.example.noteice.configs;

import com.example.noteice.configs.security.SecurityConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({SecurityConfig.class, ApplicationConfig.class})
public class InfrastructureConfig {
}
