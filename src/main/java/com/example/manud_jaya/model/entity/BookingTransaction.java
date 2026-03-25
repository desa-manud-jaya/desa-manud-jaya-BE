package com.example.manud_jaya.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "transactions")
public class BookingTransaction {

    @Id
    private String id;

    private String userId;
    private String businessId;
    private String packageId;

    private Integer quantity;
    private Double amount;

    @Builder.Default
    private String status = "pending";

    private LocalDateTime createdAt;

    @DBRef(lazy = true)
    private User user;

    @DBRef(lazy = true)
    private Business business;
}
