package com.example.manud_jaya.service;

import com.example.manud_jaya.model.entity.Business;
import com.example.manud_jaya.model.entity.User;
import com.example.manud_jaya.model.inbound.request.CreateBusinessRequest;
import com.example.manud_jaya.repository.BusinessRepository;
import com.example.manud_jaya.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class VendorServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BusinessRepository businessRepository;

    @InjectMocks
    private VendorService vendorService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createBusinessSuccess() {
        User vendor = User.builder().id("vendor-1").username("vendor1").build();
        CreateBusinessRequest request = CreateBusinessRequest.builder()
                .name("Business A")
                .description("Desc")
                .address("Address")
                .city("City")
                .latitude(1.0)
                .longitude(2.0)
                .build();

        when(userRepository.findByUsername("vendor1")).thenReturn(Optional.of(vendor));
        when(businessRepository.findByVendorId("vendor-1")).thenReturn(Optional.empty());
        when(businessRepository.save(any(Business.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Business result = vendorService.createBusiness(request, "vendor1");

        assertEquals("vendor-1", result.getVendorId());
        assertEquals("APPROVED", result.getApprovalStatus());
    }

    @Test
    void createBusinessDuplicateThrows() {
        User vendor = User.builder().id("vendor-1").username("vendor1").build();
        when(userRepository.findByUsername("vendor1")).thenReturn(Optional.of(vendor));
        when(businessRepository.findByVendorId("vendor-1"))
                .thenReturn(Optional.of(Business.builder().id("b-1").vendorId("vendor-1").build()));

        assertThrows(RuntimeException.class, () ->
                vendorService.createBusiness(CreateBusinessRequest.builder().name("X").build(), "vendor1")
        );
    }

    @Test
    void getBusinessDetailSuccess() {
        User vendor = User.builder().id("vendor-1").username("vendor1").build();
        Business business = Business.builder().id("b-1").vendorId("vendor-1").name("Biz").build();

        when(userRepository.findByUsername("vendor1")).thenReturn(Optional.of(vendor));
        when(businessRepository.findById("b-1")).thenReturn(Optional.of(business));

        Business result = vendorService.getBusinessDetail("b-1", "vendor1");

        assertEquals("Biz", result.getName());
    }

    @Test
    void getBusinessDetailUnauthorizedThrows() {
        User vendor = User.builder().id("vendor-1").username("vendor1").build();
        Business business = Business.builder().id("b-1").vendorId("vendor-other").build();

        when(userRepository.findByUsername("vendor1")).thenReturn(Optional.of(vendor));
        when(businessRepository.findById("b-1")).thenReturn(Optional.of(business));

        assertThrows(RuntimeException.class, () -> vendorService.getBusinessDetail("b-1", "vendor1"));
    }
}
