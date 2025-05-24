package org.apiary.model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "Payment")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transactionId")
    private Integer transactionId;

    @OneToOne
    @JoinColumn(name = "orderId", nullable = false)
    private Order order;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "status", nullable = false)
    private String status; // "SUCCESS", "FAILED", "PENDING"

    @Column(name = "date", nullable = false)
    private LocalDateTime date;

    // Default constructor required by JPA
    public Payment() {
        this.date = LocalDateTime.now();
    }

    public Payment(Order order, BigDecimal amount, String status) {
        this.order = order;
        this.amount = amount;
        this.status = status;
        this.date = LocalDateTime.now();
    }

    // Getters and setters
    public Integer getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Integer transactionId) {
        this.transactionId = transactionId;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Payment payment = (Payment) o;
        return Objects.equals(transactionId, payment.transactionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionId);
    }

    @Override
    public String toString() {
        return "Payment{" +
                "transactionId=" + transactionId +
                ", status='" + status + '\'' +
                ", amount=" + amount +
                ", date=" + date +
                '}';
    }
}