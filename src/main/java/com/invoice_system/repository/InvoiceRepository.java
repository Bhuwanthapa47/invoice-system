package com.invoice_system.repository;

import com.invoice_system.model.Invoice;
import com.invoice_system.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    // 🔹 All invoices created by a specific user
    List<Invoice> findByUserUsername(String username);

    // 🔹 Search invoices by client name and date (no amount here because it's @Transient)
    @Query("SELECT i FROM Invoice i WHERE " +
            "(:client IS NULL OR LOWER(i.clientName) LIKE LOWER(CONCAT('%', :client, '%'))) AND " +
            "(:startDate IS NULL OR i.invoiceDate >= :startDate)")
    List<Invoice> searchInvoices(
            @Param("client") String client,
            @Param("startDate") LocalDate startDate
    );

    // 🔹 Count of all invoices
    @Query("SELECT COUNT(i) FROM Invoice i")
    Long getTotalInvoiceCount();

    // 🔹 Recent invoices
    List<Invoice> findTop3ByOrderByInvoiceDateDesc();

    // 🔹 General search (invoice number, client name, date — no amount)
    @Query("SELECT i FROM Invoice i WHERE " +
            "LOWER(i.invoiceNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(i.clientName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "STR(i.invoiceDate) LIKE :query")
    List<Invoice> searchInvoices(@Param("query") String query);

    List<Invoice> findByUser_Username(String username);

    List<Invoice> findByUserUsernameAndClientNameContainingIgnoreCase(String username, String clientName);

    List<Invoice> findByUserAndDueDateBetween(User user, LocalDate start, LocalDate end);


}
