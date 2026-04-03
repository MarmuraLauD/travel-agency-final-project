package com.epam.finaltask.exception;

import lombok.Getter;

@Getter
public class InsufficientFundsException extends RuntimeException {
    private final double amount;

    public InsufficientFundsException(double amount) {
        super("Insufficient funds! You are short by: " + amount);
        this.amount = amount;
    }

}

