package com.example.manud_jaya.service;

import com.example.manud_jaya.model.entity.Booking;
import com.example.manud_jaya.model.entity.Review;
import com.example.manud_jaya.model.inbound.response.PagedResponse;
import com.example.manud_jaya.repository.BookingRepository;
import com.example.manud_jaya.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminOperationsService {

    private final BookingRepository bookingRepository;
    private final ReviewRepository reviewRepository;

    public PagedResponse<Booking> getBookings(int page, int size) {
        var data = bookingRepository.findAll(PageRequest.of(page, size));
        return PagedResponse.<Booking>builder()
                .items(data.getContent())
                .page(page)
                .size(size)
                .total(data.getTotalElements())
                .build();
    }

    public PagedResponse<Review> getReviews(int page, int size) {
        var data = reviewRepository.findAll(PageRequest.of(page, size));
        return PagedResponse.<Review>builder()
                .items(data.getContent())
                .page(page)
                .size(size)
                .total(data.getTotalElements())
                .build();
    }
}
