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
@Document("reviews")
public class Review {

    @Id
    private String id;

    private String userId;

    private String destinationId;

    private Integer rating;

    private String comment;

    private LocalDateTime createdAt;
}
