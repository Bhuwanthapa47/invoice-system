package com.invoice_system.controller.api;

import com.invoice_system.dto.InvoiceRequestDTO;
import com.invoice_system.dto.LineItemDTO;
import com.invoice_system.model.*;
import com.invoice_system.repository.InvoiceRepository;
import com.invoice_system.repository.UserRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

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

    // 🔹 Create via raw entity
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
        if (invoice.getStatus() == null) {
            invoice.setStatus(InvoiceStatus.DRAFT); // Default if missing
        }

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
            invoice.setGstPercentage(updated.getGstPercentage());
            invoice.setStatus(updated.getStatus() != null ? updated.getStatus() : invoice.getStatus());

            invoice.setItems(updated.getItems());

            for (InvoiceItem item : updated.getItems()) {
                item.setInvoice(invoice);
            }

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

    // 🔹 Search
    @GetMapping("/search")
    public ResponseEntity<List<Invoice>> searchInvoices(
            @RequestParam(required = false) String client,
            @RequestParam(required = false) Double minAmount,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate
    ) {
        List<Invoice> results = invoiceRepository.searchInvoices(client, startDate);

        if (minAmount != null) {
            results = results.stream()
                    .filter(inv -> inv.getTotalAmount() >= minAmount)
                    .toList();
        }

        return ResponseEntity.ok(results);
    }

    // 🔹 Create via DTO (with status support)
    @PostMapping("/create-by-username")
    public ResponseEntity<?> createInvoiceByUsername(@RequestBody InvoiceRequestDTO dto) {
        Optional<User> userOpt = userRepository.findByUsername(dto.getUsername());
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("User not found");
        }

        User user = userOpt.get();

        List<InvoiceItem> items = new ArrayList<>();
        if (dto.getItems() != null) {
            for (LineItemDTO itemDTO : dto.getItems()) {
                InvoiceItem item = new InvoiceItem();
                item.setItemName(itemDTO.getItemName());
                item.setQuantity(itemDTO.getQuantity());
                item.setUnitPrice(itemDTO.getUnitPrice());
                items.add(item);
            }
        }

        Invoice invoice = Invoice.builder()
                .invoiceNumber(dto.getInvoiceNumber())
                .clientName(dto.getClientName())
                .invoiceDate(dto.getInvoiceDate())
                .gstPercentage(dto.getGstPercentage())
                .status(dto.getStatus() != null ? dto.getStatus() : InvoiceStatus.DRAFT) // ✅ Set status
                .user(user)
                .items(items)
                .build();

        // Link each item to invoice
        for (InvoiceItem item : items) {
            item.setInvoice(invoice);
        }

        return ResponseEntity.ok(invoiceRepository.save(invoice));
    }

    // 🔹 Summary
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getInvoiceSummary() {
        List<Invoice> allInvoices = invoiceRepository.findAll();

        long totalInvoices = allInvoices.size();
        double totalAmount = allInvoices.stream()
                .mapToDouble(Invoice::getTotalAmount)
                .sum();

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalInvoices", totalInvoices);
        summary.put("totalAmount", totalAmount);

        return ResponseEntity.ok(summary);
    }
}
