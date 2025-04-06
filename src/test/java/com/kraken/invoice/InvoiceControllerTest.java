package com.kraken.invoice;


import com.kraken.invoice.controller.InvoiceController;
import com.kraken.invoice.entity.InvoiceDto;
import com.kraken.invoice.service.InvoiceService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest
class InvoiceControllerTest {

    @Mock
    private InvoiceService invoiceService;

    @InjectMocks
    private InvoiceController controller;

    @Test
    void testGetAllInvoices() {
        InvoiceDto dto = new InvoiceDto();
        dto.setInvoiceId(1L);

        when(invoiceService.getAllInvoices()).thenReturn(List.of(dto));

        ResponseEntity<List<InvoiceDto>> response = controller.getAllInvoices();

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1L, response.getBody().get(0).getInvoiceId());
    }

    @Test
    void testGetInvoiceById() {
        InvoiceDto dto = new InvoiceDto();
        dto.setInvoiceId(2L);

        when(invoiceService.getInvoiceById(2L)).thenReturn(dto);

        ResponseEntity<InvoiceDto> response = controller.getInvoiceById(2L);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(2L, response.getBody().getInvoiceId());
    }

    @Test
    void testGetInvoiceByIdFail() {

        when(invoiceService.getInvoiceById(99L)).thenThrow(new NoSuchElementException("Invoice not found with ID: " + 99L));

        assertThrows(NoSuchElementException.class, () -> {
            controller.getInvoiceById(1L);
        });
    }

    @Test
    void testUploadInvoices() throws Exception {
        MockMultipartFile invoices = new MockMultipartFile("invoices", "invoices.csv", "text/csv", "csv-data".getBytes());
        MockMultipartFile transactions = new MockMultipartFile("transactions", "transactions.csv", "text/csv", "csv-data".getBytes());

        when(invoiceService.parseAndSave(any(), any())).thenReturn(Collections.emptyList());

        ResponseEntity<?> response = controller.uploadInvoices(invoices, transactions);

        assertEquals(200, response.getStatusCodeValue());
        verify(invoiceService, times(1)).parseAndSave(any(), any());
    }

    //include test when upload throws exception

    @Test
    void testGetInvoiceStatus() {
        InvoiceDto dto = new InvoiceDto();
        dto.setInvoiceId(3L);
        dto.setStatus("valid");
        dto.setReason("Valid invoice.");

        when(invoiceService.getInvoiceById(3L)).thenReturn(dto);

        ResponseEntity<Map<String, Object>> response = controller.getInvoiceStatus(3L);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("valid", response.getBody().get("status"));
    }
}

