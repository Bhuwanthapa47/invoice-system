package com.invoice_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication
@EnableMethodSecurity // 👈 important for @PreAuthorize to work
public class InvoiceSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(InvoiceSystemApplication.class, args);
	}

}
