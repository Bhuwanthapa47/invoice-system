package com.invoice_system.dto;

import lombok.Data;

@Data
public class LineItemDTO {
    private String itemName;
    private int quantity;
    private double unitPrice;

    public double getTotal() {
        return quantity * unitPrice;
    }
}
