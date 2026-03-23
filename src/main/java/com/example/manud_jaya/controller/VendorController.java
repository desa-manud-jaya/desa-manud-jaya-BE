package com.example.manud_jaya.controller;

import com.example.manud_jaya.model.entity.Business;
import com.example.manud_jaya.model.entity.User;
import com.example.manud_jaya.model.inbound.request.UpdateBusinessProfile;
import com.example.manud_jaya.service.AuthService;
import com.example.manud_jaya.service.PublicBusinessService;
import com.example.manud_jaya.service.SupabaseStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/vendor")
@RequiredArgsConstructor
public class VendorController {

    private final SupabaseStorageService storageService;
    private final PublicBusinessService publicBusinessService;
    private final AuthService authService;


    @GetMapping("")
    public ResponseEntity<?> getVendor(
            Authentication authentication
    ) {

        String username = authentication.getName();

        User user = authService.getUser(username);

        return ResponseEntity.ok(user);
    }

    @PutMapping("")
    public ResponseEntity<?> putVendor(
            Authentication authentication,
            @RequestBody UpdateBusinessProfile updateBusinessProfile
    ) {

        String username = authentication.getName();

        User user = authService.putUserVendor(username, updateBusinessProfile);

        return ResponseEntity.ok(user);
    }

    @GetMapping("/{vendorId}/business")
    public ResponseEntity<?> getBusiness(
            @PathVariable String vendorId
    ) {
        Business business = publicBusinessService.getBusinessByVendor(vendorId);
        return ResponseEntity.ok(business);
    }
}
