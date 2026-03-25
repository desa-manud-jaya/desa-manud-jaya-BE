package com.example.manud_jaya.controller;

import com.example.manud_jaya.service.ApprovalCenterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/approval-center")
@Tag(name = "Admin Approval Center", description = "Unified approval center queue endpoint")
@SecurityRequirement(name = "bearer-jwt")
public class AdminApprovalCenterController {

    private final ApprovalCenterService approvalCenterService;

    @GetMapping
    @Operation(summary = "Get approval center queue")
    public ResponseEntity<?> getQueue(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        return ResponseEntity.ok(approvalCenterService.getQueue(type, status, q, page, size, sortDir));
    }
}
