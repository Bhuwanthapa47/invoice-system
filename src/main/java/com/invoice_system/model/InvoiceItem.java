package com.invoice_system.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "invoice_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String itemName;
    private int quantity;
    private double unitPrice;

    public double getTotalPrice() {
        return quantity * unitPrice;
    }

    @ManyToOne
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;
}
