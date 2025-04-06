package com.kraken.invoice.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Invoice {
    @Id
    private Long invoiceId;
    private String invoiceNumber;
    private BigDecimal grossAmount;
    private BigDecimal gstAmount;
    private BigDecimal netAmount;
    private LocalDateTime receiptDate;
    private LocalDateTime paymentDueDate;
    private Integer totalNoTrxn;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL)
    private List<Transaction> transactions;
    private String status;
    private String reason;

}



