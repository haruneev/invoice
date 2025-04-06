package com.kraken.invoice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Transaction {
    @Id
    private Long trxnId;
    private LocalDateTime dateReceived;
    private LocalDateTime transactionDate;
    private String invoiceNumber;
    private LocalDateTime billingPeriodStart;
    private LocalDateTime billingPeriodEnd;
    private BigDecimal netTransactionAmount;
    private BigDecimal gstAmount;

    @ManyToOne
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;
}
