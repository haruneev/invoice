package com.kraken.invoice.exception;

public class InvoiceFileProcessingException extends RuntimeException {
    public InvoiceFileProcessingException(String message) {
        super(message);
    }
}