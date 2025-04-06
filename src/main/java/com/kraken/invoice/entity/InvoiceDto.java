package com.kraken.invoice.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class InvoiceDto {
    private Long invoiceId;
    private String invoiceNumber;
    private BigDecimal grossAmount;
    private BigDecimal gstAmount;
    private BigDecimal netAmount;
    private LocalDateTime receiptDate;
    private LocalDateTime paymentDueDate;
    private Integer totalNoTrxn;
    private List<TransactionDto> transactions;
    private String status; // "valid" or "invalid"
    private String reason;

}
