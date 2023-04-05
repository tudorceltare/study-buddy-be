package com.ps.studybuddy.exception.domain;

public class ConnectionToSamePersonException extends Exception{
    public ConnectionToSamePersonException(String message) {
        super(message);
    }
}
