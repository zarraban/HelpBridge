package com.example.help_bridge.common.web;

import com.example.help_bridge.fundraising.request.exception.InvalidRequestStateException;
import com.example.help_bridge.fundraising.request.exception.RequestNotFoundException;
import com.example.help_bridge.fundraising.request.exception.RequestDocumentNotFoundException;
import com.example.help_bridge.fundraising.request.web.RequestWebController;
import com.example.help_bridge.fundraising.fundraiser.web.FundraiserWebController;
import com.example.help_bridge.fundraising.fundraiser.exception.FundraiserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

@ControllerAdvice(basePackageClasses = {LoginController.class, RequestWebController.class, FundraiserWebController.class})
@Order(HIGHEST_PRECEDENCE + 1)
public class WebExceptionAdvice {
    private static final Logger log = LoggerFactory.getLogger(WebExceptionAdvice.class);

    @ExceptionHandler({RequestNotFoundException.class, RequestDocumentNotFoundException.class, FundraiserNotFoundException.class})
    public ModelAndView missingRequest() {
        return new ModelAndView("error/404", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({InvalidRequestStateException.class, ObjectOptimisticLockingFailureException.class})
    public ModelAndView conflict() {
        return new ModelAndView("error/409", HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ModelAndView invalidParameter() {
        ModelAndView page = new ModelAndView("error/4xx", HttpStatus.BAD_REQUEST);
        page.addObject("status", 400);
        return page;
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView unexpected(Exception exception) {
        if (exception instanceof ErrorResponse error && error.getStatusCode().is4xxClientError()) {
            ModelAndView page = new ModelAndView("error/4xx", error.getStatusCode());
            page.addObject("status", error.getStatusCode().value());
            return page;
        }
        log.error("Failed to process web request", exception);
        return new ModelAndView("error/500", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
