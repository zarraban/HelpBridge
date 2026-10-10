package com.example.help_bridge.common.exception.handler;

import com.example.help_bridge.fundraising.request.exception.RequestDocumentNotFoundException;
import com.example.help_bridge.fundraising.fund.exception.FundHasRequestsException;
import com.example.help_bridge.fundraising.fund.exception.FundNotFoundException;
import com.example.help_bridge.fundraising.fund.exception.InvalidFundStatusTransitionException;
import com.example.help_bridge.fundraising.request.exception.RequesterNotFoundException;
import com.example.help_bridge.users.donor.exception.DonorNotFoundException;
import com.example.help_bridge.fundraising.fundraiser.exception.AssignmentNotFoundException;
import com.example.help_bridge.fundraising.fundraiser.exception.FundraiserNotFoundException;
import com.example.help_bridge.fundraising.fundraiser.exception.EvidenceNotFoundException;
import com.example.help_bridge.fundraising.fundraiser.exception.InvalidAssignmentStateException;
import com.example.help_bridge.fundraising.fundraiser.exception.InvalidFundraiserStateException;
import com.example.help_bridge.fundraising.fundraiser.exception.InvalidEvidenceException;
import com.example.help_bridge.users.systemadmin.exception.DuplicateSystemAdminException;
import com.example.help_bridge.users.systemadmin.exception.SystemAdminNotFoundException;
import com.example.help_bridge.users.volunteer.exception.DuplicateVolunteerException;
import com.example.help_bridge.users.volunteer.exception.VolunteerNotFoundException;
import com.example.help_bridge.fundraising.request.exception.RequestNotFoundException;
import com.example.help_bridge.fundraising.request.exception.InvalidRequestStateException;
import com.example.help_bridge.fundraising.request.exception.FundNotApprovedException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import com.example.help_bridge.fundraising.fund.exception.DuplicateFundException;
import com.example.help_bridge.users.fundrepresentative.exception.DuplicateFundRepresentativeException;
import com.example.help_bridge.users.fundrepresentative.exception.FundRepresentativeNotFoundException;
import com.example.help_bridge.users.systemadmin.exception.SystemAdminHasVerificationActsException;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnhandledException(Exception ex) {
        if (ex instanceof ErrorResponse errorResponse) {
            return errorResponse.getBody();
        }

        log.error("Unhandled exception", ex);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error"
        );

        problemDetail.setTitle("Internal Server Error");
        problemDetail.setType(URI.create("https://api.example.com/errors/internal-server-error"));
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Validation failed for request body"
        );

        problemDetail.setTitle("Validation Error");
        problemDetail.setType(URI.create("https://api.example.com/errors/validation-error"));

        Map<String, String> errors = ex.getBindingResult().getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Invalid value",
                        (existing, _) -> existing
                ));

        problemDetail.setProperty("errors", errors);
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ProblemDetail handleMethodValidationException(HandlerMethodValidationException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Validation failed for method parameters"
        );

        problemDetail.setTitle("Constraint Violation");
        problemDetail.setType(URI.create("https://api.example.com/errors/constraint-violation"));

        Map<String, String> errors = new HashMap<>();
        for (var result : ex.getParameterValidationResults()) {
            for (var error : result.getResolvableErrors()) {
                if (error instanceof FieldError fieldError) {
                    errors.put(fieldError.getField(), fieldError.getDefaultMessage());
                } else {
                    String paramName = result.getMethodParameter().getParameterName();
                    if (paramName == null) paramName = result.getMethodParameter().getParameter().getName();
                    errors.put(paramName, error.getDefaultMessage());
                }
            }
        }

        problemDetail.setProperty("errors", errors);
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Validation constraints were violated"
        );

        problemDetail.setTitle("Constraint Violation");
        problemDetail.setType(URI.create("https://api.example.com/errors/constraint-violation"));

        Map<String, String> errors = ex.getConstraintViolations()
                .stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        ConstraintViolation::getMessage,
                        (existing, _) -> existing
                ));

        Map<String, String> cleanErrors = new HashMap<>();
        errors.forEach((path, message) -> {
            String[] parts = path.split("\\.");
            String cleanField = parts.length > 0 ? parts[parts.length - 1] : path;
            cleanErrors.put(cleanField, message);
        });

        problemDetail.setProperty("errors", cleanErrors);
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Parameter '" + ex.getName() + "' has invalid value"
        );
        problemDetail.setTitle("Type Mismatch");
        problemDetail.setType(URI.create("https://api.example.com/errors/type-mismatch"));
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleNotReadable() {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Malformed JSON request body"
        );
        problemDetail.setTitle("Malformed Request");
        problemDetail.setType(URI.create("https://api.example.com/errors/malformed-request"));
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    @ExceptionHandler({
            FundraiserNotFoundException.class,
            DonorNotFoundException.class,
            AssignmentNotFoundException.class,
            VolunteerNotFoundException.class,
            FundNotFoundException.class,
            RequestNotFoundException.class,
            FundRepresentativeNotFoundException.class,
            RequesterNotFoundException.class,
            RequestDocumentNotFoundException.class,
            EvidenceNotFoundException.class
    })
    public ProblemDetail handleNotFoundDomainExceptions(RuntimeException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                ex.getMessage()
        );
        problemDetail.setTitle("Resource Not Found");
        problemDetail.setType(URI.create("https://api.example.com/errors/not-found"));
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    @ExceptionHandler({InvalidAssignmentStateException.class, InvalidFundraiserStateException.class})
    public ProblemDetail handleInvalidStateException(RuntimeException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                ex.getMessage()
        );

        problemDetail.setTitle("Invalid State Transition");
        problemDetail.setType(URI.create("https://api.example.com/errors/invalid-state"));
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    @ExceptionHandler(FundHasRequestsException.class)
    public ProblemDetail handleFundHasRequests(FundHasRequestsException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                ex.getMessage()
        );
        problemDetail.setTitle("Fund Has Requests");
        problemDetail.setType(URI.create("https://api.example.com/errors/fund-has-requests"));
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    @ExceptionHandler(InvalidFundStatusTransitionException.class)
    public ProblemDetail handleInvalidFundStatusTransition(InvalidFundStatusTransitionException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                ex.getMessage()
        );
        problemDetail.setTitle("Invalid Fund Status Transition");
        problemDetail.setType(URI.create("https://api.example.com/errors/invalid-fund-status-transition"));
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    @ExceptionHandler(DuplicateVolunteerException.class)
    public ProblemDetail handleDuplicateResourceException(DuplicateVolunteerException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                ex.getMessage()
        );
        problemDetail.setTitle("Resource Already Exists");
        problemDetail.setType(URI.create("https://api.example.com/errors/duplicate-resource"));
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    @ExceptionHandler(InvalidEvidenceException.class)
    public ProblemDetail handleInvalidEvidenceException(InvalidEvidenceException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
        problemDetail.setTitle("Invalid Evidence");
        problemDetail.setType(URI.create("https://api.example.com/errors/invalid-evidence"));
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    @ExceptionHandler({InvalidRequestStateException.class, FundNotApprovedException.class,
            com.example.help_bridge.fundraising.fund.exception.FundNotApprovedException.class})
    public ProblemDetail handleRequestConflictExceptions(RuntimeException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                ex.getMessage()
        );
        problemDetail.setTitle("Invalid Request State");
        problemDetail.setType(URI.create("https://api.example.com/errors/invalid-request-state"));
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }
    @ExceptionHandler(SystemAdminNotFoundException.class)
    public ProblemDetail handleSystemAdminNotFound(SystemAdminNotFoundException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                ex.getMessage()
        );
        problemDetail.setTitle("Resource Not Found");
        problemDetail.setType(URI.create("https://api.example.com/errors/not-found"));
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    @ExceptionHandler(DuplicateSystemAdminException.class)
    public ProblemDetail handleDuplicateSystemAdmin(DuplicateSystemAdminException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                ex.getMessage()
        );
        problemDetail.setTitle("Resource Already Exists");
        problemDetail.setType(URI.create("https://api.example.com/errors/duplicate-resource"));
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    @ExceptionHandler({
            DuplicateFundException.class,
            DuplicateFundRepresentativeException.class
    })
    public ProblemDetail handleDuplicateFundDomain(RuntimeException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                ex.getMessage()
        );
        problemDetail.setTitle("Resource Already Exists");
        problemDetail.setType(URI.create("https://api.example.com/errors/duplicate-resource"));
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    @ExceptionHandler(SystemAdminHasVerificationActsException.class)
    public ProblemDetail handleSystemAdminHasVerificationActs(SystemAdminHasVerificationActsException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                ex.getMessage()
        );
        problemDetail.setTitle("Admin Has Verification Acts");
        problemDetail.setType(URI.create("https://api.example.com/errors/admin-has-verification-acts"));
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ProblemDetail handleOptimisticLock(ObjectOptimisticLockingFailureException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                "The resource was modified by another request. Please retry."
        );
        problemDetail.setTitle("Concurrent Modification");
        problemDetail.setType(URI.create("https://api.example.com/errors/concurrent-modification"));
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

}