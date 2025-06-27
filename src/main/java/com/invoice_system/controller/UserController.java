package com.invoice_system.controller;

import com.invoice_system.model.User;
import com.invoice_system.repository.InvoiceRepository;
import com.invoice_system.repository.UserRepository;
import com.invoice_system.service.PDFGeneratorService;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

import java.io.ByteArrayInputStream;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class UserController {
	

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final InvoiceRepository invoiceRepository;
	private final PDFGeneratorService pdfGeneratorService;

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
	                    return ResponseEntity.internalServerError().<byte[]>build(); // 👈 fix here
	                }
	            })
	            .orElseGet(() -> ResponseEntity.status(403).<byte[]>build()); // 👈 fix here too
	}

	


    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String processRegister(@ModelAttribute("user") User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // For testing purposes: give all new users ROLE_USER
        user.setRole("ROLE_USER");

        userRepository.save(user);
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }
    
    
    
    @GetMapping("/user/invoices")
    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<com.invoice_system.model.Invoice>> getUserInvoices(Authentication authentication) {
        String username = authentication.getName();
        List<com.invoice_system.model.Invoice> invoices = invoiceRepository.findByUserUsername(username);
        return ResponseEntity.ok(invoices);
    
    }
    
    
    
    
    
}
