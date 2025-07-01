package com.invoice_system.dto;

import com.invoice_system.model.InvoiceStatus;
import lombok.Data;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class InvoiceRequestDTO {
    private String username;         // 👈 instead of userId
    private String invoiceNumber;
    private String clientName;
    private double amount;
    private LocalDate invoiceDate;
    private double gstPercentage;
    private InvoiceStatus status;

    // getter and setter
    public InvoiceStatus getStatus() {
        return status;
    }

    public void setStatus(InvoiceStatus status) {
        this.status = status;
    }

    private List<LineItemDTO> items = new ArrayList<>();


}
