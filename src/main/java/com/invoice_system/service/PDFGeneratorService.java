package com.invoice_system.service;

import com.invoice_system.model.Invoice;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

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
            document.add(new Paragraph("Amount: ₹" + invoice.getAmount()));
            document.add(new Paragraph("Invoice Date: " + invoice.getInvoiceDate()));

            document.close();
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
