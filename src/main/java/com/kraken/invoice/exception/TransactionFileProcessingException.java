package com.kraken.invoice.exception;

public class TransactionFileProcessingException extends RuntimeException {
    public TransactionFileProcessingException(String message) {
        super(message);
    }
}
