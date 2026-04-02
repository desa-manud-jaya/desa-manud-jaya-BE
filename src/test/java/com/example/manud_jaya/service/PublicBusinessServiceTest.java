package com.example.manud_jaya.service;

import com.example.manud_jaya.model.entity.Business;
import com.example.manud_jaya.repository.BusinessRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class PublicBusinessServiceTest {

    @Mock
    private BusinessRepository businessRepository;

    @InjectMocks
    private PublicBusinessService publicBusinessService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getApprovedBusinessesShouldUseApprovedStatus() {
        when(businessRepository.findByApprovalStatus("APPROVED", PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(Business.builder().id("b1").build()), PageRequest.of(0, 10), 1));

        var page = publicBusinessService.getApprovedBusinesses(0, 10);
        assertEquals(1, page.getTotalElements());
    }

    @Test
    void getBusinessDetailShouldReturnWhenFound() {
        when(businessRepository.findByIdAndApprovalStatus("b1", "APPROVED"))
                .thenReturn(Optional.of(Business.builder().id("b1").build()));

        assertEquals("b1", publicBusinessService.getBusinessDetail("b1").getId());
    }

    @Test
    void getBusinessDetailNotFoundShouldThrow() {
        when(businessRepository.findByIdAndApprovalStatus("b1", "APPROVED")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> publicBusinessService.getBusinessDetail("b1"));
    }

    @Test
    void getBusinessByVendorShouldReturnWhenFound() {
        when(businessRepository.findFirstByVendorId("v1")).thenReturn(Optional.of(Business.builder().id("b1").build()));
        assertEquals("b1", publicBusinessService.getBusinessByVendor("v1").getId());
    }

    @Test
    void getBusinessByVendorNotFoundShouldThrow() {
        when(businessRepository.findFirstByVendorId("v1")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> publicBusinessService.getBusinessByVendor("v1"));
    }
}
