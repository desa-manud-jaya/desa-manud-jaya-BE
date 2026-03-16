package com.example.manud_jaya.model.entity;

import com.example.manud_jaya.model.dto.BookingItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "bookings")
public class Booking {

    @Id
    private String id;
    private String userId;
    private String tripId;
    private List<BookingItem> items;
    private String bookingStatus; // PENDING, CONFIRMED, CANCELLED
    private Double totalPrice;
    private LocalDateTime createdAt;
}
