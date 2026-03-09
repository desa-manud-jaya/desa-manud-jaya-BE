package com.example.manud_jaya.service;

import com.example.manud_jaya.model.dto.VendorProfile;
import com.example.manud_jaya.model.entity.User;
import com.example.manud_jaya.model.inbound.response.VendorBusinessDetailResponse;
import com.example.manud_jaya.model.inbound.response.VendorBusinessResponse;
import com.example.manud_jaya.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VendorService {

    private final UserRepository userRepository;

    public List<VendorBusinessResponse> getApprovedBusinesses() {

        List<User> vendors =
                userRepository.findByRoleAndVendorProfileApprovalStatus(
                        "VENDOR",
                        "APPROVED"
                );

        return vendors.stream()
                .map(user -> VendorBusinessResponse.builder()
                        .userId(user.getId())
                        .vendorName(user.getVendorProfile().getVendorName())
                        .phone(user.getVendorProfile().getPhone())
                        .address(user.getVendorProfile().getAddress())
                        .build())
                .toList();
    }

    public VendorBusinessDetailResponse getBusinessById(String vendorId) {

        User vendor = userRepository
                .findByIdAndRoleAndVendorProfileApprovalStatus(
                        vendorId,
                        "VENDOR",
                        "APPROVED"
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Business not found"
                ));

        VendorProfile profile = vendor.getVendorProfile();

        return VendorBusinessDetailResponse.builder()
                .userId(vendor.getId())
                .vendorName(profile.getVendorName())
                .phone(profile.getPhone())
                .address(profile.getAddress())
                .ktpNumber(profile.getKtpNumber())
                .approvalStatus(profile.getApprovalStatus())
                .approvedAt(profile.getApprovedAt())
                .build();
    }
}
