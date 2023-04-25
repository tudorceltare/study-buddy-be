package com.ps.studybuddy.exception.domain;

public class MeetingDateIsInThePastException extends Exception {
    public MeetingDateIsInThePastException(String message) {
        super(message);
    }
}
