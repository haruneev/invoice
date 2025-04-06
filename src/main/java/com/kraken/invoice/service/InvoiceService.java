package com.kraken.invoice.service;

import com.kraken.invoice.entity.Invoice;
import com.kraken.invoice.entity.InvoiceDto;
import com.kraken.invoice.entity.TransactionDto;
import com.kraken.invoice.repository.InvoiceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;


@Slf4j
@Service
public class InvoiceService {
    @Autowired
    public InvoiceRepository invoiceRepository;
    @Autowired
    public InvoiceTransactionParser invoiceTransactionParser;

    public List<InvoiceDto> parseAndSave(MultipartFile invoiceCsv, MultipartFile transactionCsv) throws IOException {

        try {
            log.info("Starting invoice and transaction parsing...");
            Map<Long, InvoiceDto> invoiceMap = invoiceTransactionParser.parseInvoices(invoiceCsv);
            log.info("Parsed {} invoices", invoiceMap.size());
            Map<Long, List<TransactionDto>> transactionMap = invoiceTransactionParser.parseTransactions(transactionCsv);
            log.info("Parsed transactions for {} invoices", transactionMap.size());
            List<Invoice> invoicesToSave = new ArrayList<>();

            for (Map.Entry<Long, InvoiceDto> entry : invoiceMap.entrySet()) {

                Long invoiceId = entry.getKey();
                log.debug("Processing invoice ID: {}", invoiceId);
                InvoiceDto invoiceDto = entry.getValue();
                List<TransactionDto> txnList = transactionMap.getOrDefault(invoiceId, new ArrayList<>());
                //set transactions
                invoiceDto.setTransactions(txnList);
                //set status and reason
                BigDecimal netSum = BigDecimal.ZERO;
                BigDecimal gstSum = BigDecimal.ZERO;

                if (txnList != null) {
                    for (TransactionDto txn : txnList) {
                        netSum = netSum.add(Optional.ofNullable(txn.getNetTransactionAmount()).orElse(BigDecimal.ZERO));
                        gstSum = gstSum.add(Optional.ofNullable(txn.getGstAmount()).orElse(BigDecimal.ZERO));
                    }
                }
                boolean isValid = true;
                StringBuilder reason = new StringBuilder();

                if (invoiceDto.getNetAmount() == null || invoiceDto.getGstAmount() == null) {
                    isValid = false;
                    reason.append("Missing invoice net or GST amount. ");
                }

                if (invoiceDto.getNetAmount() != null && netSum.compareTo(invoiceDto.getNetAmount()) != 0) {
                    isValid = false;
                    reason.append("Net amount mismatch. ");
                }

                if (invoiceDto.getGstAmount() != null && gstSum.compareTo(invoiceDto.getGstAmount()) != 0) {
                    isValid = false;
                    reason.append("GST amount mismatch. ");
                }
                // Validation based on number of transactions and total number of transactions in invoice
                int actualTxnCount = txnList != null ? txnList.size() : 0;

                if (invoiceDto.getTotalNoTrxn() != null && invoiceDto.getTotalNoTrxn() != actualTxnCount) {
                    isValid = false;
                    reason.append("Transaction count mismatch (expected ")
                            .append(invoiceDto.getTotalNoTrxn())
                            .append(", found ")
                            .append(actualTxnCount)
                            .append("). ");
                }

                invoiceDto.setStatus(isValid ? "valid" : "invalid");
                invoiceDto.setReason(isValid ? "Valid invoice." : reason.toString().trim());

                //Save invoice and transaction to DB
                Invoice invoice = invoiceTransactionParser.mapToEntity(invoiceDto);
                invoice.getTransactions().forEach(txn -> txn.setInvoice(invoice));
                invoicesToSave.add(invoice);
            }

            List<Invoice> savedInvoices = invoiceRepository.saveAll(invoicesToSave);
            log.info("Saved {} invoices to database", savedInvoices.size());
            return savedInvoices.stream().map(invoiceTransactionParser::mapToDTO).toList();
        } catch (IOException e) {
            log.error("Error parsing CSV files: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to parse and save invoices", e);
        }
    }


    public List<InvoiceDto> getAllInvoices() {
        log.info("Fetching all invoices...");
        List<InvoiceDto> result = invoiceRepository.findAll().stream()
                .map(invoiceTransactionParser::mapToDTO)
                .toList();
        log.info("Found {} invoices", result.size());
        return result;
    }

    public InvoiceDto getInvoiceById(Long invoiceId) {
        log.info("Fetching invoice by ID: {}", invoiceId);
        Optional<Invoice> invoice = invoiceRepository.findById(invoiceId);
        if (invoice.isPresent()) {
            log.info("Invoice found with ID: {}", invoiceId);
            return invoiceTransactionParser.mapToDTO(invoice.get());
        } else {
            log.error("Invoice not found with ID: {}", invoiceId);
            throw new NoSuchElementException("Invoice not found with ID: " + invoiceId);
        }
    }

}