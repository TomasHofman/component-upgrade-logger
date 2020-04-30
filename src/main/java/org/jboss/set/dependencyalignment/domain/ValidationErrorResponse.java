package org.jboss.set.dependencyalignment.domain;

public class ValidationErrorResponse {

    public String status = "Bad request";
    public String message;

    public ValidationErrorResponse(String message) {
        this.message = message;
    }

}
