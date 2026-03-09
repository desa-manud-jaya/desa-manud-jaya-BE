package com.example.manud_jaya.model.entity;

import com.example.manud_jaya.model.dto.TripDestination;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "trip_plans")
public class TripPlan {

    @Id
    private String id;

    private String userId;

    private String title;

    private LocalDate startDate;

    private LocalDate endDate;

    private List<TripDestination> listDestination;

    private Double totalPrice;

    private LocalDateTime createdAt;
}
