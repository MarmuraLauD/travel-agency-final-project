package com.epam.finaltask.controller;

import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.model.VoucherStatus;
import com.epam.finaltask.service.VoucherService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class VoucherRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VoucherService voucherService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "test_user", authorities = {"voucher:read"})
    void getAllVouchers_WithPagination_Success() throws Exception {
        // Given
        VoucherDTO voucher = new VoucherDTO();
        voucher.setId(UUID.randomUUID().toString());
        voucher.setTitle("Paris Tour");
        voucher.setPrice(1000.0);

        Page<VoucherDTO> page = new PageImpl<>(List.of(voucher));

        when(voucherService.searchWithFilters(any(), any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/vouchers")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vouchers[0].title").value("Paris Tour"))
                .andExpect(jsonPath("$.totalItems").value(1));
    }

    @Test
    @WithMockUser(username = "test_user", authorities = {"voucher:update"})
    void orderVoucher_ValidData_Success() throws Exception {
        // Given
        String voucherId = UUID.randomUUID().toString();
        String userId = UUID.randomUUID().toString();
        LocalDate arrivalDate = LocalDate.now().plusDays(5);

        VoucherDTO orderedVoucher = new VoucherDTO();
        orderedVoucher.setId(voucherId);
        orderedVoucher.setTitle("Paris Tour");
        orderedVoucher.setStatus("PENDING");

        when(voucherService.order(eq(voucherId), eq(userId), any(LocalDate.class)))
                .thenReturn(orderedVoucher);

        // When & Then
        mockMvc.perform(post("/api/vouchers/{id}/order", voucherId)
                        .param("userId", userId)
                        .param("arrivalDate", arrivalDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Paris Tour"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @WithMockUser(username = "test_user", authorities = {"voucher:update"})
    void orderVoucher_PastDate_ReturnsBadRequest() throws Exception {
        // Given
        String voucherId = UUID.randomUUID().toString();
        String userId = UUID.randomUUID().toString();
        LocalDate pastDate = LocalDate.now().minusDays(1);

        when(voucherService.order(eq(voucherId), eq(userId), any(LocalDate.class)))
                .thenThrow(new com.epam.finaltask.exception.InvalidDateException("Arrival date cannot be in the past"));

        // When & Then
        mockMvc.perform(post("/api/vouchers/{id}/order", voucherId)
                        .param("userId", userId)
                        .param("arrivalDate", pastDate.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test_user", authorities = {"voucher:update"})
    void confirmOrder_SufficientFunds_Success() throws Exception {
        // Given
        UUID voucherId = UUID.randomUUID();

        VoucherDTO confirmedVoucher = new VoucherDTO();
        confirmedVoucher.setId(voucherId.toString());
        confirmedVoucher.setStatus("CONFIRMED");

        when(voucherService.confirmOrder(voucherId)).thenReturn(confirmedVoucher);

        // When & Then
        mockMvc.perform(post("/api/vouchers/{id}/confirm", voucherId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    @WithMockUser(username = "test_user", authorities = {"voucher:update"})
    void confirmOrder_InsufficientFunds_ReturnsBadRequest() throws Exception {
        // Given
        UUID voucherId = UUID.randomUUID();

        when(voucherService.confirmOrder(voucherId))
                .thenThrow(new com.epam.finaltask.exception.InsufficientFundsException(100.0));

        // When & Then
        mockMvc.perform(post("/api/vouchers/{id}/confirm", voucherId))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test_user", authorities = {"voucher:update"})
    void cancelOrder_PendingVoucher_Success() throws Exception {
        // Given
        UUID voucherId = UUID.randomUUID();

        doNothing().when(voucherService).cancelOrder(voucherId);

        // When & Then
        mockMvc.perform(post("/api/vouchers/{id}/cancel", voucherId))
                .andExpect(status().isOk());

        verify(voucherService, times(1)).cancelOrder(voucherId);
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"voucher:create"})
    void createVoucher_ValidData_Success() throws Exception {
        // Given
        VoucherDTO voucherDTO = new VoucherDTO();
        voucherDTO.setTitle("New Tour");
        voucherDTO.setDescription("Amazing tour description");
        voucherDTO.setPrice(1500.0);
        voucherDTO.setTourType("LEISURE");
        voucherDTO.setTransferType("PLANE");
        voucherDTO.setHotelType("FIVE_STARS");

        VoucherDTO createdVoucher = new VoucherDTO();
        createdVoucher.setId(UUID.randomUUID().toString());
        createdVoucher.setTitle("New Tour");

        when(voucherService.create(any(VoucherDTO.class))).thenReturn(createdVoucher);

        // When & Then
        mockMvc.perform(post("/api/vouchers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(voucherDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Voucher created successfully"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"voucher:create"})
    void createVoucher_InvalidData_ReturnsBadRequest() throws Exception {
        // Given
        VoucherDTO invalidVoucher = new VoucherDTO();
        invalidVoucher.setTitle("AB"); // Too short (min 3 characters)
        invalidVoucher.setDescription("Short"); // Too short (min 10 characters)

        // When & Then
        mockMvc.perform(post("/api/vouchers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidVoucher)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"voucher:update"})
    void updateVoucher_ValidData_Success() throws Exception {
        // Given
        String voucherId = UUID.randomUUID().toString();

        VoucherDTO updateDTO = new VoucherDTO();
        updateDTO.setTitle("Updated Tour");
        updateDTO.setDescription("Updated description");
        updateDTO.setPrice(2000.0);
        updateDTO.setTourType("SAFARI");
        updateDTO.setTransferType("JEEPS");
        updateDTO.setHotelType("FOUR_STARS");

        VoucherDTO updatedVoucher = new VoucherDTO();
        updatedVoucher.setId(voucherId);
        updatedVoucher.setTitle("Updated Tour");

        when(voucherService.update(eq(voucherId), any(VoucherDTO.class))).thenReturn(updatedVoucher);

        // When & Then
        mockMvc.perform(patch("/api/vouchers/{id}", voucherId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Voucher updated successfully"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"voucher:delete"})
    void deleteVoucher_ExistingVoucher_Success() throws Exception {
        // Given
        String voucherId = UUID.randomUUID().toString();

        doNothing().when(voucherService).delete(voucherId);

        // When & Then
        mockMvc.perform(delete("/api/vouchers/{id}", voucherId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Voucher deleted successfully"));

        verify(voucherService, times(1)).delete(voucherId);
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"voucher:update"})
    void changeHotStatus_ValidVoucher_Success() throws Exception {
        // Given
        String voucherId = UUID.randomUUID().toString();
        boolean newHotStatus = true;

        doNothing().when(voucherService).changeHotStatus(voucherId, newHotStatus);

        // When & Then
        mockMvc.perform(patch("/api/vouchers/{id}/status", voucherId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"hot\": true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Voucher hot status updated successfully"));

        verify(voucherService, times(1)).changeHotStatus(voucherId, newHotStatus);
    }

    @Test
    @WithMockUser(username = "test_user", authorities = {"voucher:read"})
    void searchWithFilters_ValidParams_ReturnsFiltered() throws Exception {
        // Given
        VoucherDTO voucher1 = new VoucherDTO();
        voucher1.setId(UUID.randomUUID().toString());
        voucher1.setTitle("Beach Tour");
        voucher1.setTourType("LEISURE");

        VoucherDTO voucher2 = new VoucherDTO();
        voucher2.setId(UUID.randomUUID().toString());
        voucher2.setTitle("Mountain Tour");
        voucher2.setTourType("ADVENTURE");

        Page<VoucherDTO> filteredPage = new PageImpl<>(List.of(voucher1));

        when(voucherService.searchWithFilters(
                eq("Beach"),
                eq("LEISURE"),
                any(),
                any(),
                any(),
                eq("REGISTERED"),
                any(Pageable.class)
        )).thenReturn(filteredPage);

        // When & Then
        mockMvc.perform(get("/api/vouchers")
                        .param("title", "Beach")
                        .param("tourType", "LEISURE")
                        .param("status", "REGISTERED")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vouchers[0].title").value("Beach Tour"))
                .andExpect(jsonPath("$.totalItems").value(1));
    }
}
