package com.invoice_system.controller.api;

import com.invoice_system.model.Invoice;
import com.invoice_system.model.User;
import com.invoice_system.repository.InvoiceRepository;
import com.invoice_system.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/invoices")
@PreAuthorize("hasRole('ADMIN')")
public class InvoiceController {

    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;

    public InvoiceController(InvoiceRepository invoiceRepository, UserRepository userRepository) {
        this.invoiceRepository = invoiceRepository;
        this.userRepository = userRepository;
    }

    // 🔹 Create
    @PostMapping
    public ResponseEntity<?> createInvoice(@RequestBody Invoice invoice) {
        if (invoice.getUser() == null || invoice.getUser().getId() == null) {
            return ResponseEntity.badRequest().body("User ID must be provided");
        }

        User user = userRepository.findById(invoice.getUser().getId())
                .orElse(null);

        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }

        invoice.setUser(user);
        Invoice saved = invoiceRepository.save(invoice);
        return ResponseEntity.ok(saved);
    }

    // 🔹 Get all
    @GetMapping
    public ResponseEntity<List<Invoice>> getAllInvoices() {
        return ResponseEntity.ok(invoiceRepository.findAll());
    }

    // 🔹 Get by ID
    @GetMapping("/{id}")
    public ResponseEntity<Invoice> getInvoiceById(@PathVariable Long id) {
        return invoiceRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 🔹 Update
    @PutMapping("/{id}")
    public ResponseEntity<?> updateInvoice(@PathVariable Long id, @RequestBody Invoice updated) {
        return invoiceRepository.findById(id).map(invoice -> {
            invoice.setInvoiceNumber(updated.getInvoiceNumber());
            invoice.setClientName(updated.getClientName());
            invoice.setInvoiceDate(updated.getInvoiceDate());
            invoice.setAmount(updated.getAmount());
            return ResponseEntity.ok(invoiceRepository.save(invoice));
        }).orElse(ResponseEntity.notFound().build());
    }

    // 🔹 Delete
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInvoice(@PathVariable Long id) {
        if (!invoiceRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        invoiceRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
