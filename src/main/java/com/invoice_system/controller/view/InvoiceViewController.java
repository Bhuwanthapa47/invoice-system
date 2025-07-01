package com.invoice_system.controller.view;

import com.invoice_system.dto.InvoiceRequestDTO;
import com.invoice_system.dto.LineItemDTO;
import com.invoice_system.model.Invoice;
import com.invoice_system.model.InvoiceItem;
import com.invoice_system.model.User;
import com.invoice_system.repository.InvoiceRepository;
import com.invoice_system.repository.UserRepository;
import com.invoice_system.service.PDFGeneratorService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/invoices")
public class InvoiceViewController {

    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;
    private final PDFGeneratorService pdfGeneratorService;


    public InvoiceViewController(InvoiceRepository invoiceRepository,
                                 UserRepository userRepository,
                                 PDFGeneratorService pdfGeneratorService) {
        this.invoiceRepository = invoiceRepository;
        this.userRepository = userRepository;
        this.pdfGeneratorService = pdfGeneratorService;

    }

    // 🔹 List invoices (Admin & User)
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public String listInvoices(@RequestParam(required = false) String q, Model model) {
        List<Invoice> invoices = (q != null && !q.isBlank())
                ? invoiceRepository.searchInvoices(q)
                : invoiceRepository.findAll();

        model.addAttribute("invoices", invoices);
        model.addAttribute("query", q);
        return "invoice/list";
    }

    // 🔹 Show create form
    @GetMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public String showCreateForm(Model model) {
        model.addAttribute("invoice", new InvoiceRequestDTO());
        model.addAttribute("users", userRepository.findAll());
        return "invoice/create";
    }

    // 🔹 Handle invoice creation
    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public String createInvoiceFromForm(@ModelAttribute InvoiceRequestDTO dto,
                                        Model model,
                                        RedirectAttributes redirectAttributes) {
        try {
            Optional<User> userOpt = userRepository.findByUsername(dto.getUsername());
            if (userOpt.isEmpty()) {
                model.addAttribute("error", "User not found");
                model.addAttribute("invoice", dto);
                model.addAttribute("users", userRepository.findAll());
                return "invoice/create";
            }

            Invoice invoice = new Invoice();
            invoice.setInvoiceNumber(dto.getInvoiceNumber());
            invoice.setClientName(dto.getClientName());
            invoice.setGstPercentage(dto.getGstPercentage());
            invoice.setInvoiceDate(dto.getInvoiceDate());
            invoice.setDueDate(dto.getDueDate()); // ✅ ADD THIS LINE
            invoice.setUser(userOpt.get());
            invoice.setStatus(dto.getStatus());

            List<InvoiceItem> items = new ArrayList<>();
            for (LineItemDTO itemDto : dto.getItems()) {
                InvoiceItem item = new InvoiceItem();
                item.setItemName(itemDto.getItemName());
                item.setQuantity(itemDto.getQuantity());
                item.setUnitPrice(itemDto.getUnitPrice());
                item.setInvoice(invoice); // Set parent invoice
                items.add(item);
            }

            invoice.setItems(items); // Set items
            invoiceRepository.save(invoice); // Persist invoice + items

            redirectAttributes.addFlashAttribute("msg", "Invoice created successfully!");
            return "redirect:/invoices";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error: " + e.getMessage());
            model.addAttribute("invoice", dto);
            model.addAttribute("users", userRepository.findAll());
            return "invoice/create";
        }
    }


    // 🔹 Download invoice PDF
    @GetMapping("/{id}/download")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> downloadInvoicePdf(@PathVariable Long id) {
        return invoiceRepository.findById(id)
                .map(invoice -> {
                    try {
                        ByteArrayInputStream pdf = pdfGeneratorService.generateInvoicePDF(invoice);
                        byte[] bytes = pdf.readAllBytes();

                        return ResponseEntity.ok()
                                .header("Content-Disposition", "attachment; filename=invoice_" + id + ".pdf")
                                .header("Content-Type", "application/pdf")
                                .body(bytes);
                    } catch (Exception e) {
                        return ResponseEntity.internalServerError().<byte[]>build();
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // 🔹 Edit invoice (basic data only for now)
    @GetMapping("/edit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String showEditForm(@PathVariable Long id, Model model) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid invoice ID: " + id));

        InvoiceRequestDTO dto = new InvoiceRequestDTO();
        dto.setUsername(invoice.getUser().getUsername());
        dto.setInvoiceNumber(invoice.getInvoiceNumber());
        dto.setClientName(invoice.getClientName());
        dto.setGstPercentage(invoice.getGstPercentage());
        dto.setInvoiceDate(invoice.getInvoiceDate());
        dto.setDueDate(invoice.getDueDate());

        model.addAttribute("invoice", dto);
        model.addAttribute("invoiceId", id);
        return "invoice/edit"; // You can update this view to support items if needed
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateInvoice(@PathVariable Long id,
                                @ModelAttribute("invoice") InvoiceRequestDTO dto,
                                RedirectAttributes redirectAttributes) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid invoice ID: " + id));

        Optional<User> userOpt = userRepository.findByUsername(dto.getUsername());
        if (userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "User not found");
            return "redirect:/invoices";
        }

        invoice.setUser(userOpt.get());
        invoice.setInvoiceNumber(dto.getInvoiceNumber());
        invoice.setClientName(dto.getClientName());
        invoice.setGstPercentage(dto.getGstPercentage());
        invoice.setInvoiceDate(dto.getInvoiceDate());
        invoice.setDueDate(dto.getDueDate());
        invoice.setStatus(dto.getStatus());

        invoiceRepository.save(invoice);
        redirectAttributes.addFlashAttribute("msg", "Invoice updated successfully!");
        return "redirect:/invoices";
    }

    // 🔹 Delete invoice
    @GetMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteInvoice(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (invoiceRepository.existsById(id)) {
            invoiceRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("msg", "Invoice deleted successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Invoice not found!");
        }
        return "redirect:/invoices";
    }


}
