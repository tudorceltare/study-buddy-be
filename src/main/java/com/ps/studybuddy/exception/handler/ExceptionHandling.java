package com.ps.studybuddy.exception.handler;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.ps.studybuddy.domain.entities.HttpResponse;
import com.ps.studybuddy.exception.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;
import java.io.IOException;
import java.util.Objects;

import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
public class ExceptionHandling implements ErrorController {
    public static final Logger LOGGER = LoggerFactory.getLogger(ExceptionHandling.class);
    public static final String ACCOUNT_LOCKED = "Account locked. Contact administration.";
    public static final String METHOD_IS_NOT_ALLOWED = "This request method is not allowed on this endpoint. Please send a '%s' request.";
    public static final String INTERNAL_SERVER_ERROR_MSG = "An error occurred while processing the request";
    public static final String INCORRECT_CREDENTIALS = "Username / password incorrect. Please try again";
    public static final String ACCOUNT_DISABLED = "Your account has been disabled. If this is an error, please contact administration";
    public static final String ERROR_PROCESSING_FILE = "Error occurred while processing file";
    public static final String NOT_ENOUGH_PERMISSION = "You do not have enough permission";
    public static final String ENTITY_NOT_FOUND = "Entity was not found";
    public static final String SAME_PERSON_EXCEPTION = "You cannot request to connect to the same person";
    public static final String ANONYMOUS_USER_EXCEPTION = "You must be logged in to perform this action";
    public static final String NOT_ADMIN_OF_GROUP = "You are not an admin of this group";
    public static final String ERROR_PATH = "/error";

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<HttpResponse> accountDisabledException() {
        return createHttpResponse(BAD_REQUEST, ACCOUNT_DISABLED);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<HttpResponse> badCredentialsException() {
        return createHttpResponse(BAD_REQUEST, INCORRECT_CREDENTIALS);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<HttpResponse> accessDeniedException() {
        return createHttpResponse(FORBIDDEN, NOT_ENOUGH_PERMISSION);
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<HttpResponse> lockedException() {
        return createHttpResponse(UNAUTHORIZED, ACCOUNT_LOCKED);
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<HttpResponse> tokenExpiredException(TokenExpiredException exception) {
        return createHttpResponse(UNAUTHORIZED, exception.getMessage());
    }

    @ExceptionHandler(EmailExistException.class)
    public ResponseEntity<HttpResponse> emailExistException(EmailExistException exception) {
        return createHttpResponse(BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(UsernameExistException.class)
    public ResponseEntity<HttpResponse> usernameExistException(UsernameExistException exception) {
        return createHttpResponse(BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(EmailNotFoundException.class)
    public ResponseEntity<HttpResponse> emailNotFoundException(EmailNotFoundException exception) {
        return createHttpResponse(BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<HttpResponse> emailNotFoundException(UserNotFoundException exception) {
        return createHttpResponse(BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<HttpResponse> methodNotSupportedException(HttpRequestMethodNotSupportedException exception) {
        HttpMethod supportedMethod = Objects.requireNonNull(exception.getSupportedHttpMethods()).iterator().next();
        return createHttpResponse(METHOD_NOT_ALLOWED, String.format(METHOD_IS_NOT_ALLOWED, supportedMethod));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<HttpResponse> internalServerErrorException(Exception exception) {
        LOGGER.error(exception.getMessage());
        return createHttpResponse(INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR_MSG);
    }

    @ExceptionHandler(NoResultException.class)
    public ResponseEntity<HttpResponse> notFoundException(NoResultException exception) {
        LOGGER.error(exception.getMessage());
        return createHttpResponse(NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<HttpResponse> iOException(IOException exception) {
        LOGGER.error(exception.getMessage());
        return createHttpResponse(INTERNAL_SERVER_ERROR, ERROR_PROCESSING_FILE);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<HttpResponse> entityNotFoundException(EntityNotFoundException exception) {
        LOGGER.error(exception.getMessage());
        return createHttpResponse(NOT_FOUND, ENTITY_NOT_FOUND);
    }

    @ExceptionHandler(ConnectionToSamePersonException.class)
    public ResponseEntity<HttpResponse> connectionToSamePersonException(ConnectionToSamePersonException exception) {
        LOGGER.error(exception.getMessage());
        return createHttpResponse(INTERNAL_SERVER_ERROR, SAME_PERSON_EXCEPTION);
    }

    @ExceptionHandler(AnonymousUserException.class)
    public ResponseEntity<HttpResponse> anonymousUserException(AnonymousUserException exception) {
        LOGGER.error(exception.getMessage());
        return createHttpResponse(UNAUTHORIZED, ANONYMOUS_USER_EXCEPTION);
    }

    @ExceptionHandler(NotAdminOfGroupException.class)
    public ResponseEntity<HttpResponse> notAdminOfGroupException(NotAdminOfGroupException exception) {
        LOGGER.error(exception.getMessage());
        return createHttpResponse(UNAUTHORIZED, NOT_ADMIN_OF_GROUP);
    }

    @ExceptionHandler(UserExistsInMemberListException.class)
    public ResponseEntity<HttpResponse> userExistsInMemberListException(UserExistsInMemberListException exception) {
        LOGGER.error(exception.getMessage());
        return createHttpResponse(CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(UserNotFoundInGroupException.class)
    public ResponseEntity<HttpResponse> userNotFoundInGroupException(UserNotFoundInGroupException exception) {
        LOGGER.error(exception.getMessage());
        return createHttpResponse(NOT_FOUND, exception.getMessage());
    }

    private ResponseEntity<HttpResponse> createHttpResponse(HttpStatus httpStatus, String message) {
        HttpResponse httpResponse = new HttpResponse(
                httpStatus.value(),
                httpStatus,
                httpStatus.getReasonPhrase().toUpperCase(),
                message.toUpperCase()
        );
        return new ResponseEntity<>(httpResponse, httpStatus);
    }

    @RequestMapping(ERROR_PATH)
    public ResponseEntity<HttpResponse> notFound404() {
        return createHttpResponse(NOT_FOUND, "There is no mapping for this URL");
    }

    public String getErrorPath() {
        return ERROR_PATH;
    }
}
