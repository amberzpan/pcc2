package com.pcc2.social.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class UserSchemaCompatibilityServiceTest {

    @Test
    void applyShouldNotThrowWhenJdbcTemplateIsMissing() {
        UserSchemaCompatibilityService service = new UserSchemaCompatibilityService(null);

        assertDoesNotThrow(service::apply);
        org.junit.jupiter.api.Assertions.assertFalse(service.apply());
    }
}
