package com.example.manud_jaya.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "payments")
public class Payment {

    @Id
    private String id;

    private String bookingId;

    private String paymentMethod;

    private Double amount;

    private String paymentStatus; // PENDING, SUCCESS, FAILED

    private String transactionId;

    private LocalDateTime paidAt;
}
