package com.innowise.userservice.domain.exeption;

public class CardLimitExceededException extends RuntimeException {
    public CardLimitExceededException(Long userId) {
        super("User with id " + userId + " already has 5 cards, cannot create more");
    }
}