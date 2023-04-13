package com.ps.studybuddy.exception.domain;

public class UserExistsInMemberListException extends Exception {
    public UserExistsInMemberListException(String message) {
        super(message);
    }
}
