package com.example.manud_jaya.service;

import com.example.manud_jaya.model.entity.Destination;
import com.example.manud_jaya.repository.DestinationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminDestinationServiceTest {

    @Mock
    private DestinationRepository destinationRepository;

    @InjectMocks
    private AdminDestinationService adminDestinationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void approveDestinationShouldSetApproved() {
        Destination d = Destination.builder().id("d1").approvalStatus("PENDING").build();
        when(destinationRepository.findById("d1")).thenReturn(Optional.of(d));
        when(destinationRepository.save(any(Destination.class))).thenAnswer(i -> i.getArgument(0));

        Destination result = adminDestinationService.approveDestination("d1");
        assertEquals("APPROVED", result.getApprovalStatus());
    }

    @Test
    void approveDestinationWithAuditShouldLog() {
        ModerationAuditService audit = mock(ModerationAuditService.class);
        ReflectionTestUtils.setField(adminDestinationService, "moderationAuditService", audit);

        Destination d = Destination.builder().id("d1").approvalStatus("PENDING").build();
        when(destinationRepository.findById("d1")).thenReturn(Optional.of(d));
        when(destinationRepository.save(any(Destination.class))).thenAnswer(i -> i.getArgument(0));

        adminDestinationService.approveDestination("d1", "admin-1");
        verify(audit).log("DESTINATION", "APPROVE", "admin-1", "d1", "Destination approved");
    }

    @Test
    void rejectDestinationShouldSetRejected() {
        Destination d = Destination.builder().id("d1").approvalStatus("PENDING").build();
        when(destinationRepository.findById("d1")).thenReturn(Optional.of(d));
        when(destinationRepository.save(any(Destination.class))).thenAnswer(i -> i.getArgument(0));

        Destination result = adminDestinationService.rejectDestination("d1");
        assertEquals("REJECTED", result.getApprovalStatus());
    }

    @Test
    void rejectDestinationWithoutAuditShouldNotLog() {
        ModerationAuditService audit = mock(ModerationAuditService.class);
        ReflectionTestUtils.setField(adminDestinationService, "moderationAuditService", audit);

        Destination d = Destination.builder().id("d1").approvalStatus("PENDING").build();
        when(destinationRepository.findById("d1")).thenReturn(Optional.of(d));
        when(destinationRepository.save(any(Destination.class))).thenAnswer(i -> i.getArgument(0));

        adminDestinationService.rejectDestination("d1", null);
        verify(audit, never()).log(any(), any(), any(), any(), any());
    }

    @Test
    void destinationNotFoundShouldThrow() {
        when(destinationRepository.findById("x")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> adminDestinationService.approveDestination("x"));
    }

    @Test
    void getPendingShouldReturnList() {
        when(destinationRepository.findByApprovalStatus("PENDING")).thenReturn(List.of());
        assertEquals(0, adminDestinationService.getPendingDestinations().size());
    }
}
