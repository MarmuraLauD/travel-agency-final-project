package com.epam.finaltask.exception;

import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.service.VoucherService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VoucherService voucherService;

    @Test
    @WithMockUser(username = "admin", authorities = {"voucher:create"})
    void handleValidationErrors_InvalidDTO_ReturnsBadRequest() throws Exception {
        // Given - Create voucher with invalid data (too short title)
        String invalidVoucher = """
                {
                    "title": "AB",
                    "description": "Short",
                    "price": 100
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/vouchers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidVoucher))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"))
                .andExpect(jsonPath("$.validationErrors").isArray());
    }

    @Test
    @WithMockUser(username = "user", authorities = {"voucher:update"})
    void handleInvalidDateException_ReturnsBadRequest() throws Exception {
        // Given
        String voucherId = UUID.randomUUID().toString();
        String userId = UUID.randomUUID().toString();

        when(voucherService.order(any(), any(), any()))
                .thenThrow(new InvalidDateException("Arrival date cannot be in the past"));

        // When & Then
        mockMvc.perform(post("/api/vouchers/{id}/order", voucherId)
                        .param("userId", userId)
                        .param("arrivalDate", "2020-01-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid Date"))
                .andExpect(jsonPath("$.message").value("Arrival date cannot be in the past"));
    }

    @Test
    @WithMockUser(username = "user", authorities = {"voucher:update"})
    void handleInvalidVoucherStatusException_ReturnsBadRequest() throws Exception {
        // Given
        UUID voucherId = UUID.randomUUID();

        when(voucherService.confirmOrder(voucherId))
                .thenThrow(new InvalidVoucherStatusException("Only vouchers in cart can be confirmed"));

        // When & Then
        mockMvc.perform(post("/api/vouchers/{id}/confirm", voucherId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid Voucher Status"))
                .andExpect(jsonPath("$.message").value("Only vouchers in cart can be confirmed"));
    }

    @Test
    @WithMockUser(username = "user", authorities = {"voucher:update"})
    void handleInsufficientFundsException_ReturnsBadRequest() throws Exception {
        // Given
        UUID voucherId = UUID.randomUUID();

        when(voucherService.confirmOrder(voucherId))
                .thenThrow(new InsufficientFundsException(150.50));

        // When & Then
        mockMvc.perform(post("/api/vouchers/{id}/confirm", voucherId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Insufficient Funds"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"voucher:create"})
    void handleDuplicateRequestException_ReturnsConflict() throws Exception {
        // Given
        when(voucherService.create(any(VoucherDTO.class)))
                .thenThrow(new DuplicateRequestException("Voucher already exists"));

        String voucherJson = """
                {
                    "title": "Duplicate Tour",
                    "description": "This is a duplicate tour",
                    "price": 500.0,
                    "tourType": "LEISURE",
                    "transferType": "BUS",
                    "hotelType": "THREE_STARS",
                    "status": "REGISTERED",
                    "hot": false
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/vouchers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(voucherJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Duplicate Request"))
                .andExpect(jsonPath("$.message").value("Voucher already exists"));
    }

    @Test
    @WithMockUser(username = "user", authorities = {"voucher:read", "voucher:update"})
    void handleEntityNotFoundException_ReturnsNotFound() throws Exception {
        // Given
        String nonExistentId = UUID.randomUUID().toString();

        when(voucherService.update(any(), any(VoucherDTO.class)))
                .thenThrow(new EntityNotFoundException("Voucher not found"));

        String updateJson = """
                {
                    "title": "Updated Tour",
                    "description": "Updated description",
                    "price": 1000.0,
                    "tourType": "SAFARI",
                    "transferType": "JEEPS",
                    "hotelType": "FOUR_STARS",
                    "status": "REGISTERED",
                    "hot": false
                }
                """;

        // When & Then
        mockMvc.perform(patch("/api/vouchers/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Resource Not Found"));
    }

    @Test
    @WithMockUser(username = "user", authorities = {"voucher:read"})
    void handleAccessDeniedException_ReturnsForbidden() throws Exception {
        // Given - User with only read permission trying to delete
        String voucherId = UUID.randomUUID().toString();

        // When & Then - Should be forbidden because user doesn't have voucher:delete
        mockMvc.perform(delete("/api/vouchers/{id}", voucherId))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void handleNoHandlerFoundException_ReturnsNotFound() throws Exception {
        // Given - Request to non-existent API endpoint
        // When & Then
        mockMvc.perform(get("/api/nonexistent-endpoint"))
                .andExpect(status().isNotFound());
    }
}
