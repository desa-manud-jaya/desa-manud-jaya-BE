package com.example.manud_jaya.configuration.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

class JwtFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private FilterChain filterChain;

    private JwtFilter jwtFilter;
    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        jwtFilter = new JwtFilter();
        ReflectionTestUtils.setField(jwtFilter, "jwtService", jwtService);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() throws Exception {
        SecurityContextHolder.clearContext();
        mocks.close();
    }

    @Test
    void shouldContinueWhenAuthorizationHeaderMissing() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/vendor/bookings");
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldAuthenticateWhenTokenValid() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/vendor/bookings");
        request.addHeader("Authorization", "Bearer valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.extractUsername("valid-token")).thenReturn("vendor1");
        when(jwtService.extractRole("valid-token")).thenReturn("VENDOR");

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("vendor1", SecurityContextHolder.getContext().getAuthentication().getName());
        assertEquals("ROLE_VENDOR", SecurityContextHolder.getContext().getAuthentication().getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void shouldReturn401WhenTokenExpired() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/vendor/bookings");
        request.addHeader("Authorization", "Bearer expired-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        ExpiredJwtException expired = new ExpiredJwtException(null, null, "JWT expired");
        when(jwtService.extractUsername("expired-token")).thenThrow(expired);

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, never()).doFilter(request, response);
        assertEquals(401, response.getStatus());
        org.junit.jupiter.api.Assertions.assertTrue(response.getContentType().startsWith("application/json"));
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        String body = response.getContentAsString();
        org.junit.jupiter.api.Assertions.assertTrue(body.contains("\"message\":\"Token expired\""));
        org.junit.jupiter.api.Assertions.assertTrue(body.contains("\"error\":\"Unauthorized\""));
        org.junit.jupiter.api.Assertions.assertTrue(body.contains("\"path\":\"/vendor/bookings\""));
    }

    @Test
    void shouldReturn401WhenTokenInvalid() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/vendor/bookings");
        request.addHeader("Authorization", "Bearer invalid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.extractUsername("invalid-token")).thenThrow(new MalformedJwtException("Bad token"));

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, never()).doFilter(request, response);
        assertEquals(401, response.getStatus());
        org.junit.jupiter.api.Assertions.assertTrue(response.getContentType().startsWith("application/json"));
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        String body = response.getContentAsString();
        org.junit.jupiter.api.Assertions.assertTrue(body.contains("\"message\":\"Invalid token\""));
        org.junit.jupiter.api.Assertions.assertTrue(body.contains("\"error\":\"Unauthorized\""));
        org.junit.jupiter.api.Assertions.assertTrue(body.contains("\"path\":\"/vendor/bookings\""));
    }
}
