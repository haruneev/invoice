package com.kraken.invoice.controller;

import com.kraken.invoice.entity.InvoiceDto;
import com.kraken.invoice.service.InvoiceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Slf4j
@RestController
public class InvoiceController {
    @Autowired
    public InvoiceService invoiceService;

    @PostMapping(path = "/invoice",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<InvoiceDto>> uploadInvoices(
            @RequestParam("invoices") MultipartFile invoiceFile,
            @RequestParam("transactions") MultipartFile transactionFile) throws IOException {
        log.info("Received request to upload invoices and transactions. Invoice file: {}, Transaction file: {}",
                invoiceFile.getOriginalFilename(), transactionFile.getOriginalFilename());

            List<InvoiceDto> invoiceDtos = invoiceService.parseAndSave(invoiceFile, transactionFile);
            log.info("Successfully processed {} invoices.", invoiceDtos.size());
            return ResponseEntity.ok(invoiceDtos);

    }

    @GetMapping("/invoice")
    public ResponseEntity<List<InvoiceDto>> getAllInvoices() {
        return ResponseEntity.ok(invoiceService.getAllInvoices());
    }

    @GetMapping("/invoice/{invoiceId}")
    public ResponseEntity<InvoiceDto> getInvoiceById(@PathVariable Long invoiceId) {

        InvoiceDto dto = invoiceService.getInvoiceById(invoiceId);
        log.info("Invoice retrieved: ID = {}", dto.getInvoiceId());
        return ResponseEntity.ok(dto);

    }

    @GetMapping("/invoice/{invoiceId}/status")
    public ResponseEntity<Map<String, Object>> getInvoiceStatus(@PathVariable Long invoiceId) {
        log.info("Fetching status for invoice ID: {}", invoiceId);
        InvoiceDto dto = invoiceService.getInvoiceById(invoiceId);
        if (dto != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("invoiceId", dto.getInvoiceId());
            response.put("status", dto.getStatus());
            response.put("reason", dto.getReason());
            log.info("Invoice status fetched: {}", response);
            return ResponseEntity.ok(response);
        } else {
            log.error("Invoice not found with ID: {}", invoiceId);
            throw new NoSuchElementException("Invoice not found with ID: " + invoiceId);
        }
    }
    @GetMapping(value = "/")
    public String home() {
        return "Invoice API is up and running";
    }

}
