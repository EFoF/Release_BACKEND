package com.service.releasenote.global.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.service.releasenote.global.util.WithMockCustomUserSecurityContextFactory;
import org.springframework.security.test.context.support.WithSecurityContext;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockCustomUserSecurityContextFactory.class)
public @interface WithMockCustomUser {

    String userName() default "1";

    String role() default "ROLE_USER";
}