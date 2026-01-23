package com.innowise.userservice.domain.exeption;

public class CardAlreadyExistsException extends RuntimeException {
    public CardAlreadyExistsException(String number) {
        super("Card with number '" + number + "' already exists");
    }
}
