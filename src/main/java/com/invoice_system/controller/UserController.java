package com.invoice_system.controller;

import com.invoice_system.dto.UserDTO;
import com.invoice_system.dto.UserResponseDTO;
import com.invoice_system.model.Invoice;
import com.invoice_system.model.InvoiceStatus;
import com.invoice_system.model.User;
import com.invoice_system.repository.InvoiceRepository;
import com.invoice_system.repository.UserRepository;
import com.invoice_system.service.PDFGeneratorService;
import com.invoice_system.service.UserService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayInputStream;
import java.security.Principal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final InvoiceRepository invoiceRepository;
    private final PDFGeneratorService pdfGeneratorService;
    private final UserService userService;

    // 🟢 Show login page
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    // 🟢 Show registration form
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new UserDTO());
        return "register";
    }


    // 🟢 Handle registration form submission
    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") UserDTO userDTO, Model model) {
        System.out.println(">> Form submitted: " + userDTO.getUsername() + " | " + userDTO.getRole());

        try {
            userService.registerUser(userDTO);
            model.addAttribute("msg", "Registration successful! Please login.");
            return "redirect:/login";
        } catch (Exception e) {
            System.out.println(">> Exception: " + e.getMessage());
            model.addAttribute("error", "Registration failed: " + e.getMessage());
            return "register";
        }
    }


    // 🟢 Dashboard for Admin
    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public String dashboard(Model model) {
        long invoiceCount = invoiceRepository.count();
        double totalAmount = invoiceRepository.findAll()
                .stream()
                .mapToDouble(Invoice::getTotalAmount)
                .sum();

        List<Invoice> recentInvoices = invoiceRepository.findTop3ByOrderByInvoiceDateDesc();

        model.addAttribute("invoiceCount", invoiceCount);
        model.addAttribute("totalAmount", totalAmount);
        model.addAttribute("recentInvoices", recentInvoices);

        return "dashboard";
    }



    // 🟢 User PDF Download
    @GetMapping("/user/invoices/{id}/download")
    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<byte[]> downloadInvoicePDF(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();

        return invoiceRepository.findById(id)
                .filter(invoice -> invoice.getUser().getUsername().equals(username))
                .map(invoice -> {
                    try {
                        ByteArrayInputStream pdf = pdfGeneratorService.generateInvoicePDF(invoice);
                        byte[] bytes = pdf.readAllBytes();

                        return ResponseEntity.ok()
                                .header("Content-Disposition", "attachment; filename=invoice_" + id + ".pdf")
                                .header("Content-Type", "application/pdf")
                                .body(bytes);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return ResponseEntity.internalServerError().<byte[]>build();
                    }
                })
                .orElseGet(() -> ResponseEntity.status(403).<byte[]>build());
    }

    // 🟢 User invoices in JSON
    @GetMapping("/user/invoices")
    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<Invoice>> getUserInvoices(Authentication authentication) {
        String username = authentication.getName();
        List<Invoice> invoices = invoiceRepository.findByUserUsername(username);
        return ResponseEntity.ok(invoices);
    }



    // 🟢 User profile
    @GetMapping("/user/profile")
    @PreAuthorize("hasRole('USER')")
    @ResponseBody
    public ResponseEntity<User> getProfile(Authentication authentication) {
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 🟢 Admin: Get all users
    @GetMapping("/api/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserResponseDTO> dtos = users.stream()
                .map(u -> new UserResponseDTO(u.getId(), u.getUsername(), u.getRole()))
                .toList();

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/user/dashboard")
    @PreAuthorize("hasRole('USER')")
    public String userDashboard(Model model, Authentication authentication) {
        String username = authentication.getName();
        List<Invoice> invoices = invoiceRepository.findByUserUsername(username);

        if (invoices == null) invoices = List.of();

        double totalAmount = invoices.stream()
                .mapToDouble(Invoice::getTotalAmount)
                .sum();

        model.addAttribute("username", username);
        model.addAttribute("invoices", invoices);
        model.addAttribute("totalAmount", totalAmount);

        return "user/dashboard";
    }

    @GetMapping("/user/invoice-page")
    @PreAuthorize("hasRole('USER')")
    public String userInvoicePage(
            Model model,
            Authentication authentication,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "sort", required = false, defaultValue = "desc") String sortOrder,
            @RequestParam(value = "status", required = false) String status
    ) {
        String username = authentication.getName();

        List<Invoice> invoices;

        // 🔍 Search by client name
        if (search != null && !search.isEmpty()) {
            invoices = invoiceRepository.findByUserUsernameAndClientNameContainingIgnoreCase(username, search);
        } else {
            invoices = invoiceRepository.findByUserUsername(username);
        }

        // 🟡 Status Filter
        if (status != null && !status.isBlank()) {
            try {
                InvoiceStatus selectedStatus = InvoiceStatus.valueOf(status.trim().toUpperCase());
                invoices = invoices.stream()
                        .filter(inv -> selectedStatus.equals(inv.getStatus()))
                        .collect(Collectors.toList());
                model.addAttribute("selectedStatus", selectedStatus.name());
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                model.addAttribute("selectedStatus", "");
            }
        } else {
            model.addAttribute("selectedStatus", "");
        }

        // 📅 Sort by invoice date
        if ("asc".equalsIgnoreCase(sortOrder)) {
            invoices.sort(Comparator.comparing(Invoice::getInvoiceDate));
        } else {
            invoices.sort(Comparator.comparing(Invoice::getInvoiceDate).reversed());
        }

        // 💰 Total Amount & Status Counts
        double totalAmount = invoices.stream()
                .mapToDouble(Invoice::getTotalAmount)
                .sum();
        long paidCount = invoices.stream().filter(i -> i.getStatus() == InvoiceStatus.PAID).count();
        long unpaidCount = invoices.stream().filter(i -> i.getStatus() == InvoiceStatus.UNPAID).count();

        // ⏰ Payment Due Reminder: Invoices due within next 3 days
        LocalDate today = LocalDate.now();
        LocalDate in3Days = today.plusDays(3);
        Set<Long> dueSoonInvoiceIds = invoices.stream()
                .filter(i -> i.getDueDate() != null &&
                        !i.getDueDate().isBefore(today) &&
                        !i.getDueDate().isAfter(in3Days) &&
                        i.getStatus() != InvoiceStatus.PAID)
                .map(Invoice::getId)
                .collect(Collectors.toSet());

        model.addAttribute("dueSoonInvoiceIds", dueSoonInvoiceIds);


        // 📦 Model attributes
        model.addAttribute("username", username);
        model.addAttribute("invoices", invoices);
        model.addAttribute("totalAmount", totalAmount);
        model.addAttribute("search", search);
        model.addAttribute("sortOrder", sortOrder);
        model.addAttribute("statusValues", InvoiceStatus.values());

        model.addAttribute("paidCount", paidCount);
        model.addAttribute("unpaidCount", unpaidCount);

        return "user/dashboard";
    }




    @GetMapping("/user/invoices/{id}")
    @PreAuthorize("hasRole('USER')")
    public String viewInvoiceDetails(@PathVariable Long id, Model model, Authentication authentication) {
        String username = authentication.getName();

        return invoiceRepository.findById(id)
                .filter(invoice -> invoice.getUser().getUsername().equals(username))
                .map(invoice -> {
                    model.addAttribute("invoice", invoice);
                    return "user/invoice-details"; // This will be our new HTML page
                })
                .orElse("error/403"); // or redirect to access denied
    }

    
}
