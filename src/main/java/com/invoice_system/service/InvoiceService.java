package com.invoice_system.service;

import com.invoice_system.model.Invoice;
import com.invoice_system.model.InvoiceStatus;
import com.invoice_system.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;

    public void updateOverdueInvoices() {
        List<Invoice> unpaidInvoices = invoiceRepository.findByStatus(InvoiceStatus.UNPAID);
        LocalDate today = LocalDate.now();

        for (Invoice invoice : unpaidInvoices) {
            if (invoice.getDueDate() != null && invoice.getDueDate().isBefore(today)) {
                invoice.setStatus(InvoiceStatus.OVERDUE);
            }
        }

        invoiceRepository.saveAll(unpaidInvoices);
    }
}
