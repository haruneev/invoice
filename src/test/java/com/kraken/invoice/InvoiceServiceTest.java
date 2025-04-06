package com.kraken.invoice;


import com.kraken.invoice.entity.Invoice;
import com.kraken.invoice.entity.InvoiceDto;
import com.kraken.invoice.entity.Transaction;
import com.kraken.invoice.entity.TransactionDto;
import com.kraken.invoice.repository.InvoiceRepository;
import com.kraken.invoice.repository.TransactionRepository;
import com.kraken.invoice.service.InvoiceService;
import com.kraken.invoice.service.InvoiceTransactionParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.anyCollection;
import static org.mockito.Mockito.when;

class InvoiceServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private InvoiceTransactionParser invoiceTransactionParser;

    @InjectMocks
    private InvoiceService invoiceService;


    @Test
    void testGetInvoiceById_Found() {
        Invoice invoice = new Invoice();
        invoice.setInvoiceId(1L);
        invoice.setTransactions(Collections.emptyList());

        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));

        InvoiceDto dto = invoiceService.getInvoiceById(1L);

        assertNotNull(dto);
        assertEquals(1L, dto.getInvoiceId());
    }

    @Test
    void testGetAllInvoices() {
        Invoice invoice = new Invoice();
        invoice.setInvoiceId(10L);
        invoice.setTransactions(Collections.emptyList());

        when(invoiceRepository.findAll()).thenReturn(List.of(invoice));

        List<InvoiceDto> result = invoiceService.getAllInvoices();

        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getInvoiceId());
    }

    @Test
    void testInvoiceValidationLogic() {
        InvoiceDto dto = new InvoiceDto();
        dto.setInvoiceId(1L);
        dto.setNetAmount(BigDecimal.valueOf(100));
        dto.setGstAmount(BigDecimal.valueOf(10));
        dto.setTotalNoTrxn(2);


        Transaction txn1 = new Transaction();
        txn1.setNetTransactionAmount(BigDecimal.valueOf(50));
        txn1.setGstAmount(BigDecimal.valueOf(5));

        Transaction txn2 = new Transaction();
        txn2.setNetTransactionAmount(BigDecimal.valueOf(50));
        txn2.setGstAmount(BigDecimal.valueOf(5));

        dto.setTransactions(List.of());


        Invoice invoice = new Invoice();
        invoice.setInvoiceId(dto.getInvoiceId());
        invoice.setTransactions(List.of(txn1, txn2));

        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));

        InvoiceDto result = invoiceService.getInvoiceById(1L);
        assertEquals(1L, result.getInvoiceId());
    }

    @Test
    void testParseAndSave() throws IOException {
        // Load the CSV files from the resources directory
        File invoiceFile = new ClassPathResource("sampleFiles/InvoicesTest.csv").getFile();
        File transactionFile = new ClassPathResource("sampleFiles/TransactionTest.csv").getFile();
        InvoiceDto dto = getInvoiceDto();
        Invoice invoice = invoiceTransactionParser.mapToEntity(dto);

        // Create MultipartFile instances
        MultipartFile invoiceCsv = new MockMultipartFile("invoices.csv", Files.readAllBytes(invoiceFile.toPath()));
        MultipartFile transactionCsv = new MockMultipartFile("transactions.csv", Files.readAllBytes(transactionFile.toPath()));
        when(invoiceRepository.saveAll(anyCollection())).thenReturn(List.of(invoice));
        // Invoke the method
        List<InvoiceDto> resultedInvoices = invoiceService.parseAndSave(invoiceCsv, transactionCsv);

        // Assertions
        assertNotNull(resultedInvoices);
        assertEquals(1, resultedInvoices.size());
        assertEquals(dto.getInvoiceId(), resultedInvoices.get(0).getInvoiceId());
        assertEquals(dto.getStatus(), resultedInvoices.get(0).getStatus());

    }

    private InvoiceDto getInvoiceDto() {
        InvoiceDto dto = new InvoiceDto();
        dto.setInvoiceId(31550L);
        dto.setStatus("valid");
        TransactionDto t1 = new TransactionDto();
        TransactionDto t2 = new TransactionDto();
        TransactionDto t3 = new TransactionDto();
        TransactionDto t4 = new TransactionDto();
        dto.setTransactions(List.of(t1, t2, t3, t4));

        return dto;

    }
}
