package com.pcc2.social.config;

import com.pcc2.social.service.UserSchemaCompatibilityService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class StartupSchemaSync implements ApplicationRunner {

    private final UserSchemaCompatibilityService userSchemaCompatibilityService;

    public StartupSchemaSync(UserSchemaCompatibilityService userSchemaCompatibilityService) {
        this.userSchemaCompatibilityService = userSchemaCompatibilityService;
    }

    @Override
    public void run(ApplicationArguments args) {
        userSchemaCompatibilityService.apply();
    }
}
