package com.example.manud_jaya.service;

import com.example.manud_jaya.exception.ResourceNotFoundException;
import com.example.manud_jaya.exception.ValidationException;
import com.example.manud_jaya.model.entity.Business;
import com.example.manud_jaya.model.entity.Destination;
import com.example.manud_jaya.model.entity.User;
import com.example.manud_jaya.model.inbound.request.CreateDestinationRequest;
import com.example.manud_jaya.repository.BusinessRepository;
import com.example.manud_jaya.repository.DestinationRepository;
import com.example.manud_jaya.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DestinationServiceTest {

    @Mock
    private DestinationRepository destinationRepository;

    @Mock
    private BusinessRepository businessRepository;

    @Mock
    private SupabaseStorageService storageService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DestinationService destinationService;

    private User vendor;
    private Business business;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        vendor = User.builder()
                .id("vendor-id")
                .username("vendor1")
                .role("VENDOR")
                .build();

        business = Business.builder()
                .id("business-id")
                .vendorId("vendor-id")
                .name("Test Business")
                .approvalStatus("APPROVED")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createDestinationSuccess() throws IOException {
        CreateDestinationRequest request = new CreateDestinationRequest(
                "Test Destination",
                "NATURE",
                "Sub",
                "Description",
                "Eco Value",
                List.of("Hiking")
        );

        MockMultipartFile image = new MockMultipartFile(
                "images",
                "test.jpg",
                "image/jpeg",
                "img".getBytes()
        );

        when(userRepository.findByUsername("vendor1")).thenReturn(Optional.of(vendor));
        when(businessRepository.findById("business-id")).thenReturn(Optional.of(business));
        when(storageService.uploadImage(any())).thenReturn("https://example.com/image.jpg");
        when(destinationRepository.save(any(Destination.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Destination result = destinationService.createDestination("business-id", request, List.of(image), "vendor1");

        assertEquals("Test Destination", result.getName());
        assertEquals("PENDING", result.getApprovalStatus());
        assertEquals(1, result.getImages().size());
        verify(destinationRepository, times(1)).save(any(Destination.class));
    }

    @Test
    void createDestinationVendorNotFoundThrows() {
        when(userRepository.findByUsername("vendor1")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                destinationService.createDestination("business-id", new CreateDestinationRequest(), List.of(), "vendor1")
        );
    }

    @Test
    void createDestinationBusinessNotFoundThrows() {
        when(userRepository.findByUsername("vendor1")).thenReturn(Optional.of(vendor));
        when(businessRepository.findById("business-id")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                destinationService.createDestination("business-id", new CreateDestinationRequest(), List.of(), "vendor1")
        );
    }

    @Test
    void createDestinationUnauthorizedThrows() {
        User anotherVendor = User.builder().id("another").username("vendor2").build();

        when(userRepository.findByUsername("vendor2")).thenReturn(Optional.of(anotherVendor));
        when(businessRepository.findById("business-id")).thenReturn(Optional.of(business));

        CreateDestinationRequest request = new CreateDestinationRequest();

        assertThrows(AccessDeniedException.class, () ->
                destinationService.createDestination("business-id", request, List.of(), "vendor2")
        );
    }

    @Test
    void createDestinationNonImageThrowsValidation() {
        CreateDestinationRequest request = new CreateDestinationRequest();
        MockMultipartFile file = new MockMultipartFile("images", "doc.pdf", "application/pdf", "x".getBytes());

        when(userRepository.findByUsername("vendor1")).thenReturn(Optional.of(vendor));
        when(businessRepository.findById("business-id")).thenReturn(Optional.of(business));

        assertThrows(ValidationException.class, () ->
                destinationService.createDestination("business-id", request, List.of(file), "vendor1")
        );
    }

    @Test
    void getBusinessDestinationsSuccess() {
        when(userRepository.findByUsername("vendor1")).thenReturn(Optional.of(vendor));
        when(businessRepository.findById("business-id")).thenReturn(Optional.of(business));
        when(destinationRepository.findByBusinessId("business-id"))
                .thenReturn(List.of(Destination.builder().id("d1").build()));

        List<Destination> result = destinationService.getBusinessDestinations("business-id", "vendor1");

        assertEquals(1, result.size());
        verify(destinationRepository).findByBusinessId("business-id");
    }

    @Test
    void getBusinessDestinationsUnauthorizedThrows() {
        User anotherVendor = User.builder().id("another").username("vendor2").build();
        when(userRepository.findByUsername("vendor2")).thenReturn(Optional.of(anotherVendor));
        when(businessRepository.findById("business-id")).thenReturn(Optional.of(business));

        assertThrows(AccessDeniedException.class,
                () -> destinationService.getBusinessDestinations("business-id", "vendor2"));
    }

    @Test
    void getApprovedDestinationsReturnsOnlyApprovedStatusQuery() {
        when(userRepository.findByUsername("vendor1")).thenReturn(Optional.of(vendor));
        when(businessRepository.findById("business-id")).thenReturn(Optional.of(business));
        when(destinationRepository.findByBusinessIdAndApprovalStatus("business-id", "APPROVED"))
                .thenReturn(List.of(Destination.builder().id("d1").approvalStatus("APPROVED").build()));

        List<Destination> result = destinationService.getApprovedDestinations("business-id", "vendor1");

        assertEquals(1, result.size());
        verify(destinationRepository, times(1))
                .findByBusinessIdAndApprovalStatus("business-id", "APPROVED");
    }

    @Test
    void getPendingDestinationsReturnsPendingStatusQuery() {
        when(userRepository.findByUsername("vendor1")).thenReturn(Optional.of(vendor));
        when(businessRepository.findById("business-id")).thenReturn(Optional.of(business));
        when(destinationRepository.findByBusinessIdAndApprovalStatus("business-id", "PENDING"))
                .thenReturn(List.of(Destination.builder().id("d2").approvalStatus("PENDING").build()));

        List<Destination> result = destinationService.getPendingDestinations("business-id", "vendor1");

        assertEquals(1, result.size());
        verify(destinationRepository, times(1))
                .findByBusinessIdAndApprovalStatus("business-id", "PENDING");
    }
}
