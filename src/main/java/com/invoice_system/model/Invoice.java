package com.invoice_system.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "invoices")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String invoiceNumber;
    private String clientName;
    private double amount;
    private LocalDate invoiceDate;


    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonBackReference
    private User user;
}
