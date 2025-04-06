package com.kraken.invoice.service;

import com.kraken.invoice.entity.Invoice;
import com.kraken.invoice.entity.InvoiceDto;
import com.kraken.invoice.entity.Transaction;
import com.kraken.invoice.entity.TransactionDto;
import com.kraken.invoice.exception.InvoiceFileProcessingException;
import com.kraken.invoice.exception.TransactionFileProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class InvoiceTransactionParser {


    public Map<Long, InvoiceDto> parseInvoices(MultipartFile csv) throws IOException {
        Map<Long, InvoiceDto> map = new HashMap<>();

        try (
                Reader reader = new InputStreamReader(csv.getInputStream());
                CSVParser parser = CSVFormat.DEFAULT
                        .withFirstRecordAsHeader()
                        .withIgnoreHeaderCase()
                        .withTrim()
                        .parse(reader)
        ) {
            for (CSVRecord record : parser) {
                try {
                    InvoiceDto dto = new InvoiceDto();

                    dto.setInvoiceId(parseLongSafe(record.get("invoice_id")));
                    dto.setInvoiceNumber(record.get("invoice_number"));
                    dto.setGrossAmount(parseBigDecimalSafe(record.get("gross_amount")));
                    dto.setGstAmount(parseBigDecimalSafe(record.get("gst_amount")));
                    dto.setNetAmount(parseBigDecimalSafe(record.get("net_amount")));
                    dto.setReceiptDate(parseDateTimeSafe(record.get("receipt_date")));
                    dto.setPaymentDueDate(parseDateTimeSafe(record.get("payment_due_date")));
                    dto.setTotalNoTrxn(parseIntSafe(record.get("total_no_trxn")));

                    if (dto.getInvoiceId() != null) {
                        map.put(dto.getInvoiceId(), dto);
                    }
                    else {
                        log.error("Skipping record in Invoice File: missing invoice ID at line {}",
                                record.getRecordNumber());
                    }
                } catch (Exception e) {
                    log.error("Error due to invalid record in Invoice File at line {}: {}", record.getRecordNumber(),
                            e.getMessage());
                    throw new InvoiceFileProcessingException("Error Processing Invoice File .Invalid invoice record " +
                            "at line " + record.getRecordNumber());

                }
            }
        }

        return map;
    }


    public Map<Long, List<TransactionDto>> parseTransactions(MultipartFile csv) throws IOException {
        Map<Long, List<TransactionDto>> map = new HashMap<>();

        try (
                Reader reader = new InputStreamReader(csv.getInputStream());
                CSVParser parser = CSVFormat.DEFAULT
                        .withFirstRecordAsHeader()
                        .withIgnoreHeaderCase()
                        .withTrim()
                        .parse(reader)
        ) {
            for (CSVRecord record : parser) {
                try {
                    TransactionDto dto = new TransactionDto();
                    Long transactionId = parseLongSafe(record.get("trxn_id"));
                    if(transactionId != null) {

                        dto.setTrxnId(transactionId);
                        dto.setDateReceived(parseDateTimeSafe(record.get("date_received")));
                        dto.setTransactionDate(parseDateTimeSafe(record.get("transaction_date")));
                        Long invoiceId = parseLongSafe(record.get("invoice_id"));

                        dto.setInvoiceNumber(record.get("invoice_number"));
                        dto.setBillingPeriodStart(parseDateTimeSafe(record.get("billing_period_start")));
                        dto.setBillingPeriodEnd(parseDateTimeSafe(record.get("billing_period_end")));
                        dto.setNetTransactionAmount(parseBigDecimalSafe(record.get("net_transaction_amount")));
                        dto.setGstAmount(parseBigDecimalSafe(record.get("gst_amount")));

                        if (invoiceId != null) {
                            map.computeIfAbsent(invoiceId, k -> new ArrayList<>()).add(dto);
                        } else {
                            log.error("Skipping record in transaction file: missing invoice ID at line {}",
                                    record.getRecordNumber());
                        }
                    } else {
                        log.error("Skipping record in transaction file: missing transaction ID at line {}",
                                record.getRecordNumber());
                    }
                } catch (Exception e) {
                    log.error("Error due to invalid record at line {}: {}", record.getRecordNumber(), e.getMessage());
                    throw new TransactionFileProcessingException("Error processing Transaction file. Invalid " +
                            "transaction record at line " + record.getRecordNumber());

                }
            }
        }

        return map;
    }

    public  Invoice mapToEntity(InvoiceDto dto) {
        Invoice invoice = new Invoice();
        BeanUtils.copyProperties(dto, invoice);
        List<Transaction> txns = dto.getTransactions().stream().map(txnDto -> {
            Transaction txn = new Transaction();
            BeanUtils.copyProperties(txnDto, txn);
            return txn;
        }).toList();
        invoice.setTransactions(txns);
        return invoice;
    }

    public InvoiceDto mapToDTO(Invoice invoice) {
        InvoiceDto dto = new InvoiceDto();
        BeanUtils.copyProperties(invoice, dto);
        List<TransactionDto> txns = invoice.getTransactions().stream().map(txn -> {
            TransactionDto t = new TransactionDto();
            BeanUtils.copyProperties(txn, t);
            return t;
        }).toList();
        dto.setTransactions(txns);
        return dto;
    }

    public Long parseLongSafe(String value) {
        try {
            return (value == null || value.isBlank()) ? null : Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public BigDecimal parseBigDecimalSafe(String value) {
        try {
            return (value == null || value.isBlank()) ? null : new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public LocalDateTime parseDateTimeSafe(String value) {
        try {
            return (value == null || value.isBlank()) ? null : LocalDateTime.parse(value.trim().replace(" ", "T"));
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public Integer parseIntSafe(String value) {
        try {
            return (value == null || value.isBlank()) ? null : Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            log.error("Invalid integer value: '" + value + "'"); // Optional logging
            return null;
        }
    }

}

