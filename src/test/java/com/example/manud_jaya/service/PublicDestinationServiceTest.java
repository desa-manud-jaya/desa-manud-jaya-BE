package com.example.manud_jaya.service;

import com.example.manud_jaya.exception.ValidationException;
import com.example.manud_jaya.model.entity.Business;
import com.example.manud_jaya.model.entity.Destination;
import com.example.manud_jaya.repository.BusinessRepository;
import com.example.manud_jaya.repository.DestinationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class PublicDestinationServiceTest {

    @Mock
    private DestinationRepository destinationRepository;

    @Mock
    private BusinessRepository businessRepository;

    @InjectMocks
    private PublicDestinationService publicDestinationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllApprovedDestinationsShouldReturnList() {
        when(destinationRepository.findByApprovalStatus("APPROVED")).thenReturn(List.of(Destination.builder().id("d1").build()));
        assertEquals(1, publicDestinationService.getAllApprovedDestinations().size());
    }

    @Test
    void getAllApprovedDestinationsPagedShouldValidateAndReturn() {
        when(destinationRepository.findByApprovalStatus("APPROVED", PageRequest.of(0, 10))).thenReturn(List.of());
        assertEquals(0, publicDestinationService.getAllApprovedDestinations(0, 10).size());
    }

    @Test
    void getAllApprovedDestinationsPagedInvalidShouldThrow() {
        assertThrows(ValidationException.class, () -> publicDestinationService.getAllApprovedDestinations(-1, 10));
        assertThrows(ValidationException.class, () -> publicDestinationService.getAllApprovedDestinations(0, 0));
    }

    @Test
    void countAllApprovedDestinationsShouldReturnCount() {
        when(destinationRepository.countByApprovalStatus("APPROVED")).thenReturn(3L);
        assertEquals(3L, publicDestinationService.countAllApprovedDestinations());
    }

    @Test
    void getBusinessDestinationsShouldReturnList() {
        when(businessRepository.findByIdAndApprovalStatus("b1", "APPROVED"))
                .thenReturn(Optional.of(Business.builder().id("b1").build()));
        when(destinationRepository.findByBusinessIdAndApprovalStatus("b1", "APPROVED")).thenReturn(List.of(Destination.builder().id("d1").build()));

        assertEquals(1, publicDestinationService.getBusinessDestinations("b1").size());
    }

    @Test
    void getBusinessDestinationsBusinessNotFoundShouldThrow() {
        when(businessRepository.findByIdAndApprovalStatus("b1", "APPROVED")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> publicDestinationService.getBusinessDestinations("b1"));
    }

    @Test
    void getDestinationDetailShouldReturn() {
        when(destinationRepository.findByIdAndApprovalStatus("d1", "APPROVED"))
                .thenReturn(Optional.of(Destination.builder().id("d1").build()));
        assertEquals("d1", publicDestinationService.getDestinationDetail("d1").getId());
    }

    @Test
    void getDestinationDetailNotFoundShouldThrow() {
        when(destinationRepository.findByIdAndApprovalStatus("d1", "APPROVED")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> publicDestinationService.getDestinationDetail("d1"));
    }
}
