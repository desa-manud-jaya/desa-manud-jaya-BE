package com.example.manud_jaya.controller;

import com.example.manud_jaya.model.entity.Business;
import com.example.manud_jaya.model.entity.User;
import com.example.manud_jaya.model.inbound.request.UpdateBusinessProfile;
import com.example.manud_jaya.service.AuthService;
import com.example.manud_jaya.service.PublicBusinessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/vendor")
@RequiredArgsConstructor
@Tag(name = "Vendor Profile", description = "Vendor profile and vendor business lookup endpoints")
@SecurityRequirement(name = "bearer-jwt")
public class VendorController {

    private final PublicBusinessService publicBusinessService;
    private final AuthService authService;


    @GetMapping("")
    @Operation(summary = "Get vendor profile")
    public ResponseEntity<?> getVendor(
            Authentication authentication
    ) {

        String username = authentication.getName();

        User user = authService.getUser(username);

        return ResponseEntity.ok(user);
    }

    @PutMapping("")
    @Operation(summary = "Complete/update vendor profile", description = "Update vendor profile fields and move status to PENDING for admin review.")
    public ResponseEntity<?> putVendor(
            Authentication authentication,
            @RequestBody UpdateBusinessProfile updateBusinessProfile
    ) {

        String username = authentication.getName();

        User user = authService.putUserVendor(username, updateBusinessProfile);

        return ResponseEntity.ok(user);
    }

    @GetMapping("/{vendorId}/business")
    @Operation(summary = "Get business by vendor ID")
    public ResponseEntity<?> getBusiness(
            @Parameter(description = "Vendor ID") @PathVariable String vendorId
    ) {
        Business business = publicBusinessService.getBusinessByVendor(vendorId);
        return ResponseEntity.ok(business);
    }
}
