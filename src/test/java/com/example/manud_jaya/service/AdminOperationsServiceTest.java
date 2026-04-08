package com.example.manud_jaya.service;

import com.example.manud_jaya.model.entity.Booking;
import com.example.manud_jaya.model.entity.Review;
import com.example.manud_jaya.model.inbound.response.PagedResponse;
import com.example.manud_jaya.repository.BookingRepository;
import com.example.manud_jaya.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class AdminOperationsServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private AdminOperationsService adminOperationsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getBookingsShouldReturnPagedResponse() {
        when(bookingRepository.findAll(PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(Booking.builder().id("b1").build()), PageRequest.of(0, 10), 1));

        PagedResponse<Booking> result = adminOperationsService.getBookings(0, 10);
        assertEquals(1, result.getTotal());
        assertEquals("b1", result.getItems().get(0).getId());
    }

    @Test
    void getReviewsShouldReturnPagedResponse() {
        when(reviewRepository.findAll(PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(Review.builder().id("r1").build()), PageRequest.of(0, 10), 1));

        PagedResponse<Review> result = adminOperationsService.getReviews(0, 10);
        assertEquals(1, result.getTotal());
        assertEquals("r1", result.getItems().get(0).getId());
    }
}
