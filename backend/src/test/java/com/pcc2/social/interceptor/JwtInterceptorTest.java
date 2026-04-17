package com.pcc2.social.interceptor;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class JwtInterceptorTest {

    @Test
    void shouldRejectProtectedEndpointWithoutToken() throws Exception {
        JwtInterceptor interceptor = new JwtInterceptor();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/post");
        request.setMethod("POST");
        MockHttpServletResponse response = new MockHttpServletResponse();

        java.lang.reflect.Field secretField = JwtInterceptor.class.getDeclaredField("jwtSecret");
        secretField.setAccessible(true);
        secretField.set(interceptor, "pcc2-social-platform-secret-key-2024");

        boolean allowed = interceptor.preHandle(request, response, new Object());

        assertFalse(allowed);
        assertEquals(401, response.getStatus());
    }
}
