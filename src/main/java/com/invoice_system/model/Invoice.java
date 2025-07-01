package com.invoice_system.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
    private LocalDate invoiceDate;
    private double gstPercentage;



    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonBackReference
    private User user;

    @Enumerated(EnumType.STRING)
    private InvoiceStatus status;

    @Column(name = "due_date")
    private LocalDate dueDate;




    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InvoiceItem> items = new ArrayList<>();

    @Transient
    public double getSubTotal() {
        return items.stream().mapToDouble(InvoiceItem::getTotalPrice).sum();
    }

    @Transient
    public double getTotalAmount() {
        return getSubTotal() + (getSubTotal() * gstPercentage / 100.0);
    }


}
