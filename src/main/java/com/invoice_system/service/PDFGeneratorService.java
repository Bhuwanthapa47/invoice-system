package com.invoice_system.service;

import com.invoice_system.model.Invoice;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class PDFGeneratorService {

    public ByteArrayInputStream generateInvoicePDF(Invoice invoice) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            document.add(new Paragraph("INVOICE").setBold().setFontSize(20));
            document.add(new Paragraph("Invoice Number: " + invoice.getInvoiceNumber()));
            document.add(new Paragraph("Client Name: " + invoice.getClientName()));
            document.add(new Paragraph("Invoice Date: " + invoice.getInvoiceDate()));
            document.add(new Paragraph(" "));

            // 🧾 Line Items
            document.add(new Paragraph("Items:").setBold());
            for (var item : invoice.getItems()) {
                String line = String.format(
                        "%s (x%d) @ ₹%.2f = ₹%.2f",
                        item.getItemName(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getTotalPrice()
                );
                document.add(new Paragraph(line));
            }

            document.add(new Paragraph(" ")); // spacing

            // 💰 Totals
            document.add(new Paragraph("Subtotal: ₹" + String.format("%.2f", invoice.getSubTotal())));
            document.add(new Paragraph("GST (" + invoice.getGstPercentage() + "%): ₹" +
                    String.format("%.2f", invoice.getSubTotal() * invoice.getGstPercentage() / 100.0)));
            document.add(new Paragraph("Total Amount: ₹" + String.format("%.2f", invoice.getTotalAmount()))
                    .setBold());

            document.close();
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }



    public ByteArrayInputStream generateAllInvoicesPDF(List<Invoice> invoices) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            document.add(new Paragraph("ALL INVOICES").setBold().setFontSize(22));

            for (Invoice invoice : invoices) {
                document.add(new Paragraph("\n--------------------------------------------------"));
                document.add(new Paragraph("Invoice Number: " + invoice.getInvoiceNumber()));
                document.add(new Paragraph("Client Name: " + invoice.getClientName()));
                document.add(new Paragraph("Invoice Date: " + invoice.getInvoiceDate()));
                document.add(new Paragraph(" "));

                // 🧾 Line Items
                document.add(new Paragraph("Items:").setBold());
                for (var item : invoice.getItems()) {
                    String line = String.format(
                            "%s (x%d) @ ₹%.2f = ₹%.2f",
                            item.getItemName(),
                            item.getQuantity(),
                            item.getUnitPrice(),
                            item.getTotalPrice()
                    );
                    document.add(new Paragraph(line));
                }

                document.add(new Paragraph(" ")); // spacing

                // 💰 Totals
                document.add(new Paragraph("Subtotal: ₹" + String.format("%.2f", invoice.getSubTotal())));
                document.add(new Paragraph("GST (" + invoice.getGstPercentage() + "%): ₹" +
                        String.format("%.2f", invoice.getSubTotal() * invoice.getGstPercentage() / 100.0)));
                document.add(new Paragraph("Total Amount: ₹" + String.format("%.2f", invoice.getTotalAmount()))
                        .setBold());
            }

            document.close();
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


}
