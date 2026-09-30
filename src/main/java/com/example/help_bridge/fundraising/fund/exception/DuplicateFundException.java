package com.example.help_bridge.fundraising.fund.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

import java.net.URI;
import java.time.Instant;

public class DuplicateFundException extends ErrorResponseException {

    public DuplicateFundException(String edrpou) {
        super(HttpStatus.CONFLICT, buildProblem(edrpou), null);
    }

    private static ProblemDetail buildProblem(String edrpou) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                "Fund with EDRPOU " + edrpou + " already exists"
        );
        problem.setTitle("Resource Already Exists");
        problem.setType(URI.create("https://api.example.com/errors/duplicate-resource"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }
}