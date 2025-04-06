package com.kraken.invoice.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class TransactionDto {
    private Long trxnId;
    private LocalDateTime dateReceived;
    private LocalDateTime transactionDate;
    private String invoiceNumber;
    private LocalDateTime billingPeriodStart;
    private LocalDateTime billingPeriodEnd;
    private BigDecimal netTransactionAmount;
    private BigDecimal gstAmount;
}
