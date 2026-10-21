package com.example.help_bridge.common.web;

import com.example.help_bridge.common.exception.handler.GlobalExceptionHandler;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;

@ControllerAdvice
@Order(HIGHEST_PRECEDENCE)
public class HtmlErrorPagesAdvice {

    private final GlobalExceptionHandler apiHandler;

    public HtmlErrorPagesAdvice(GlobalExceptionHandler apiHandler) {
        this.apiHandler = apiHandler;
    }

    @ExceptionHandler({NoResourceFoundException.class, NoHandlerFoundException.class})
    public Object notFound(HttpServletRequest request, Exception e) {
        if (wantsHtml(request)) {
            return page("error/404", HttpStatus.NOT_FOUND);
        }
        return json(HttpStatus.NOT_FOUND, apiHandler.handleUnhandledException(e));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public Object forbidden(HttpServletRequest request) {
        if (wantsHtml(request)) {
            return page("error/403", HttpStatus.FORBIDDEN);
        }
        return json(HttpStatus.FORBIDDEN, apiHandler.handleAccessDenied(request));
    }

    private static boolean wantsHtml(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        return accept != null && accept.contains("text/html") && !request.getRequestURI().startsWith("/api/");
    }

    private static ModelAndView page(String view, HttpStatus status) {
        ModelAndView mav = new ModelAndView(view);
        mav.setStatus(status);
        return mav;
    }

    private static ResponseEntity<ProblemDetail> json(HttpStatus status, ProblemDetail body) {
        return ResponseEntity.status(status).contentType(APPLICATION_PROBLEM_JSON).body(body);
    }
}
