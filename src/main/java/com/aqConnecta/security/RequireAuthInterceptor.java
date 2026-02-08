package com.aqConnecta.security;

import com.aqConnecta.exception.autenticacao.LoginNecessarioException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RequireAuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) return true;

        boolean isClassProtected = handlerMethod.getBeanType().isAnnotationPresent(RequireAuth.class);
        boolean isMethodProtected = handlerMethod.hasMethodAnnotation(RequireAuth.class);

        if (!(isClassProtected || isMethodProtected)) return true;

        var auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            throw new LoginNecessarioException();
        }

        return true;
    }
}
