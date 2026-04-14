package com.example.manud_jaya.service;

import com.example.manud_jaya.configuration.security.JwtService;
import com.example.manud_jaya.exception.ConflictException;
import com.example.manud_jaya.exception.ResourceNotFoundException;
import com.example.manud_jaya.exception.UnauthorizedException;
import com.example.manud_jaya.model.dto.ApprovalStatus;
import com.example.manud_jaya.model.dto.GuideProfile;
import com.example.manud_jaya.model.dto.VendorProfile;
import com.example.manud_jaya.model.entity.User;
import com.example.manud_jaya.model.inbound.request.GuideRegisterRequest;
import com.example.manud_jaya.model.inbound.request.LoginRequest;
import com.example.manud_jaya.model.inbound.request.UpdateBusinessProfile;
import com.example.manud_jaya.model.inbound.request.UserRegisterRequest;
import com.example.manud_jaya.model.inbound.request.VendorRegisterRequest;
import com.example.manud_jaya.model.inbound.response.LoginResponse;
import com.example.manud_jaya.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public User getUser(String username) {

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public User putUserVendor(String username, UpdateBusinessProfile updateBusinessProfile) {

        User user = getUser(username);
        user.setStatus(ApprovalStatus.PENDING.toString());
        VendorProfile vendorProfile = user.getVendorProfile();
        vendorProfile.setAddress(updateBusinessProfile.getAddress());
        vendorProfile.setPhone(updateBusinessProfile.getPhone());
        vendorProfile.setNib(updateBusinessProfile.getNib());
        vendorProfile.setSku(updateBusinessProfile.getSku());
        vendorProfile.setSiup(updateBusinessProfile.getSiup());
        vendorProfile.setNik(updateBusinessProfile.getNik());
        vendorProfile.setVendorName(updateBusinessProfile.getNamaUsaha());
        vendorProfile.setBankAccountName(updateBusinessProfile.getBankAccountName());
        vendorProfile.setBankAccountNumber(updateBusinessProfile.getBankAccountNumber());
        vendorProfile.setApprovalStatus(ApprovalStatus.PENDING.toString());

        return userRepository.save(user);
    }

    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        if (ApprovalStatus.REJECTED.name().equals(user.getStatus())) {
            throw new UnauthorizedException("User is rejected");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid password");
        }

        String token = jwtService.generateToken(user);

        return new LoginResponse(
                user.getId(),
                token,
                user.getUsername(),
                user.getRole()
        );
    }

    // REGISTER USER
    public void registerUser(UserRegisterRequest request) {

        String normalizedUsername = normalizeUsername(request.getUsername());
        String normalizedEmail = normalizeEmail(request.getEmail());

        validateUniqueRegistrationIdentity(normalizedUsername, normalizedEmail);

        User user = new User();

        user.setUsername(normalizedUsername);
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER");
        user.setStatus("ACTIVE");
        user.setCreatedAt(LocalDateTime.now());

        userRepository.save(user);
    }

    // REGISTER VENDOR
    public void registerVendor(VendorRegisterRequest request) {

        String normalizedUsername = normalizeUsername(request.getUsername());
        String normalizedEmail = normalizeEmail(request.getEmail());

        validateUniqueRegistrationIdentity(normalizedUsername, normalizedEmail);

        VendorProfile vendorProfile = VendorProfile.builder()
                .vendorName(request.getNamaUsaha())
                .ownerName(request.getNamaOwner())
                .jenisUsaha(request.getJenisUsaha())
                .description(request.getDescription())
                .phone(request.getPhone())
                .address(request.getAddress())
                .ktpNumber(request.getKtpNumber())
                .approvalStatus("PLEASE_FILL_PROFILE")
                .build();


        User user = new User();

        user.setUsername(normalizedUsername);
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("VENDOR");
        user.setStatus("PLEASE_FILL_PROFILE");
        user.setCreatedAt(LocalDateTime.now());
        user.setVendorProfile(vendorProfile);
        userRepository.save(user);
    }

    // REGISTER GUIDE
    public void registerGuide(GuideRegisterRequest request) {

        String normalizedUsername = normalizeUsername(request.getUsername());
        String normalizedEmail = normalizeEmail(request.getEmail());

        validateUniqueRegistrationIdentity(normalizedUsername, normalizedEmail);

        GuideProfile guideProfile = GuideProfile.builder()
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .licenseNumber(request.getLicenseNumber())
                .approvalStatus(ApprovalStatus.PENDING.name())
                .build();

        User user = new User();
        user.setUsername(normalizedUsername);
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("GUIDE");
        user.setStatus(ApprovalStatus.PENDING.name());
        user.setGuideProfile(guideProfile);
        user.setCreatedAt(LocalDateTime.now());

        userRepository.save(user);
    }

    private void validateUniqueRegistrationIdentity(String username, String email) {
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new ConflictException("Username already registered");
        }

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("Email already registered");
        }
    }

    private String normalizeUsername(String username) {
        return username == null ? "" : username.trim();
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}
