package com.epam.finaltask.service;

import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.exception.InsufficientFundsException;
import com.epam.finaltask.exception.InvalidDateException;
import com.epam.finaltask.exception.InvalidVoucherStatusException;
import com.epam.finaltask.mapper.VoucherMapper;
import com.epam.finaltask.model.*;
import com.epam.finaltask.repository.UserRepository;
import com.epam.finaltask.repository.VoucherRepository;
import com.epam.finaltask.specification.VoucherSpecification;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VoucherServiceImplTest {

    @Mock
    private VoucherRepository voucherRepository;

    @Mock
    private VoucherMapper voucherMapper;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private VoucherServiceImpl voucherService;

    @Test
    void create_ValidVoucher_Success() {
        // Given
        VoucherDTO voucherDTO = new VoucherDTO();
        voucherDTO.setTitle("Paris Tour");
        voucherDTO.setPrice(1000.0);

        Voucher voucher = new Voucher();
        voucher.setTitle("Paris Tour");
        voucher.setPrice(1000.0);

        Voucher savedVoucher = new Voucher();
        savedVoucher.setId(UUID.randomUUID());
        savedVoucher.setTitle("Paris Tour");
        savedVoucher.setStatus(VoucherStatus.REGISTERED);
        savedVoucher.setHot(false);

        VoucherDTO expectedDTO = new VoucherDTO();
        expectedDTO.setId(savedVoucher.getId().toString());
        expectedDTO.setTitle("Paris Tour");

        when(voucherMapper.toVoucher(voucherDTO)).thenReturn(voucher);
        when(voucherRepository.save(any(Voucher.class))).thenReturn(savedVoucher);
        when(voucherMapper.toVoucherDTO(savedVoucher)).thenReturn(expectedDTO);

        // When
        VoucherDTO result = voucherService.create(voucherDTO);

        // Then
        assertNotNull(result);
        assertEquals("Paris Tour", result.getTitle());
        verify(voucherRepository, times(1)).save(any(Voucher.class));
    }

    @Test
    void order_ValidData_AddToCart() {
        // Given
        String voucherId = UUID.randomUUID().toString();
        String userId = UUID.randomUUID().toString();
        LocalDate arrivalDate = LocalDate.now().plusDays(5);

        User user = new User();
        user.setId(UUID.fromString(userId));
        user.setVouchers(new ArrayList<>());

        Voucher voucher = new Voucher();
        voucher.setId(UUID.fromString(voucherId));
        voucher.setStatus(VoucherStatus.REGISTERED);
        voucher.setPrice(500.0);

        VoucherDTO expectedDTO = new VoucherDTO();
        expectedDTO.setId(voucherId);
        expectedDTO.setStatus("PENDING");

        when(userRepository.findById(UUID.fromString(userId))).thenReturn(Optional.of(user));
        when(voucherRepository.findById(UUID.fromString(voucherId))).thenReturn(Optional.of(voucher));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(voucherRepository.save(any(Voucher.class))).thenReturn(voucher);
        when(voucherMapper.toVoucherDTO(any(Voucher.class))).thenReturn(expectedDTO);

        // When
        VoucherDTO result = voucherService.order(voucherId, userId, arrivalDate);

        // Then
        assertNotNull(result);
        assertEquals("PENDING", result.getStatus());
        verify(voucherRepository, times(1)).save(any(Voucher.class));
    }

    @Test
    void order_PastDate_ThrowsInvalidDateException() {
        // Given
        String voucherId = UUID.randomUUID().toString();
        String userId = UUID.randomUUID().toString();
        LocalDate pastDate = LocalDate.now().minusDays(1);

        // When & Then
        assertThrows(InvalidDateException.class, () ->
                voucherService.order(voucherId, userId, pastDate)
        );
    }

    @Test
    void order_NullDate_ThrowsInvalidDateException() {
        // Given
        String voucherId = UUID.randomUUID().toString();
        String userId = UUID.randomUUID().toString();

        // When & Then
        assertThrows(InvalidDateException.class, () ->
                voucherService.order(voucherId, userId, null)
        );
    }

    @Test
    void order_UnavailableVoucher_ThrowsInvalidVoucherStatusException() {
        // Given
        String voucherId = UUID.randomUUID().toString();
        String userId = UUID.randomUUID().toString();
        LocalDate arrivalDate = LocalDate.now().plusDays(5);

        User user = new User();
        user.setId(UUID.fromString(userId));

        Voucher voucher = new Voucher();
        voucher.setId(UUID.fromString(voucherId));
        voucher.setStatus(VoucherStatus.CONFIRMED);

        when(userRepository.findById(UUID.fromString(userId))).thenReturn(Optional.of(user));
        when(voucherRepository.findById(UUID.fromString(voucherId))).thenReturn(Optional.of(voucher));

        // When & Then
        assertThrows(InvalidVoucherStatusException.class, () ->
                voucherService.order(voucherId, userId, arrivalDate)
        );
    }

    @Test
    void confirmOrder_SufficientFunds_Success() {
        // Given
        UUID voucherId = UUID.randomUUID();

        User user = new User();
        user.setBalance(new BigDecimal("1000.00"));

        Voucher voucher = new Voucher();
        voucher.setId(voucherId);
        voucher.setStatus(VoucherStatus.PENDING);
        voucher.setPrice(500.0);
        voucher.setUser(user);

        VoucherDTO expectedDTO = new VoucherDTO();
        expectedDTO.setStatus("CONFIRMED");

        when(voucherRepository.findById(voucherId)).thenReturn(Optional.of(voucher));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(voucherRepository.save(any(Voucher.class))).thenReturn(voucher);
        when(voucherMapper.toVoucherDTO(any(Voucher.class))).thenReturn(expectedDTO);

        // When
        VoucherDTO result = voucherService.confirmOrder(voucherId);

        // Then
        assertNotNull(result);
        assertEquals("CONFIRMED", result.getStatus());
        verify(voucherRepository, times(1)).save(any(Voucher.class));
    }

    @Test
    void confirmOrder_InsufficientFunds_ThrowsException() {
        // Given
        UUID voucherId = UUID.randomUUID();

        User user = new User();
        user.setBalance(new BigDecimal("100.00"));

        Voucher voucher = new Voucher();
        voucher.setId(voucherId);
        voucher.setStatus(VoucherStatus.PENDING);
        voucher.setPrice(500.0);
        voucher.setUser(user);

        when(voucherRepository.findById(voucherId)).thenReturn(Optional.of(voucher));

        // When & Then
        assertThrows(InsufficientFundsException.class, () ->
                voucherService.confirmOrder(voucherId)
        );
    }

    @Test
    void confirmOrder_NotPending_ThrowsInvalidVoucherStatusException() {
        // Given
        UUID voucherId = UUID.randomUUID();

        Voucher voucher = new Voucher();
        voucher.setId(voucherId);
        voucher.setStatus(VoucherStatus.REGISTERED);

        when(voucherRepository.findById(voucherId)).thenReturn(Optional.of(voucher));

        // When & Then
        assertThrows(InvalidVoucherStatusException.class, () ->
                voucherService.confirmOrder(voucherId)
        );
    }

    @Test
    void update_ExistingVoucher_Success() {
        // Given
        String voucherId = UUID.randomUUID().toString();

        VoucherDTO voucherDTO = new VoucherDTO();
        voucherDTO.setTitle("Updated Title");
        voucherDTO.setPrice(1500.0);
        voucherDTO.setTourType("LEISURE");
        voucherDTO.setTransferType("PLANE");
        voucherDTO.setHotelType("FIVE_STARS");

        Voucher existingVoucher = new Voucher();
        existingVoucher.setId(UUID.fromString(voucherId));
        existingVoucher.setTitle("Old Title");

        Voucher updatedVoucher = new Voucher();
        updatedVoucher.setId(UUID.fromString(voucherId));
        updatedVoucher.setTitle("Updated Title");

        VoucherDTO expectedDTO = new VoucherDTO();
        expectedDTO.setId(voucherId);
        expectedDTO.setTitle("Updated Title");

        when(voucherRepository.findById(UUID.fromString(voucherId))).thenReturn(Optional.of(existingVoucher));
        when(voucherRepository.save(any(Voucher.class))).thenReturn(updatedVoucher);
        when(voucherMapper.toVoucherDTO(updatedVoucher)).thenReturn(expectedDTO);

        // When
        VoucherDTO result = voucherService.update(voucherId, voucherDTO);

        // Then
        assertNotNull(result);
        assertEquals("Updated Title", result.getTitle());
        verify(voucherRepository, times(1)).save(any(Voucher.class));
    }

    @Test
    void delete_ExistingVoucher_Success() {
        // Given
        String voucherId = UUID.randomUUID().toString();

        doNothing().when(voucherRepository).deleteById(UUID.fromString(voucherId));

        // When
        voucherService.delete(voucherId);

        // Then
        verify(voucherRepository, times(1)).deleteById(UUID.fromString(voucherId));
    }

    @Test
    void cancelOrder_PendingVoucher_Success() {
        // Given
        UUID voucherId = UUID.randomUUID();

        User user = new User();
        List<Voucher> vouchers = new ArrayList<>();

        Voucher voucher = new Voucher();
        voucher.setId(voucherId);
        voucher.setStatus(VoucherStatus.PENDING);
        voucher.setUser(user);
        vouchers.add(voucher);
        user.setVouchers(vouchers);

        when(voucherRepository.findById(voucherId)).thenReturn(Optional.of(voucher));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(voucherRepository.save(any(Voucher.class))).thenReturn(voucher);

        // When
        voucherService.cancelOrder(voucherId);

        // Then
        verify(voucherRepository, times(1)).save(any(Voucher.class));
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void cancelOrder_NotPending_ThrowsException() {
        // Given
        UUID voucherId = UUID.randomUUID();

        Voucher voucher = new Voucher();
        voucher.setId(voucherId);
        voucher.setStatus(VoucherStatus.CONFIRMED);

        when(voucherRepository.findById(voucherId)).thenReturn(Optional.of(voucher));

        // When & Then
        assertThrows(InvalidVoucherStatusException.class, () ->
                voucherService.cancelOrder(voucherId)
        );
    }

    @Test
    void changeHotStatus_ValidVoucher_Success() {
        // Given
        String voucherId = UUID.randomUUID().toString();
        boolean newHotStatus = true;

        Voucher voucher = new Voucher();
        voucher.setId(UUID.fromString(voucherId));
        voucher.setHot(false);

        when(voucherRepository.findById(UUID.fromString(voucherId))).thenReturn(Optional.of(voucher));
        when(voucherRepository.save(any(Voucher.class))).thenReturn(voucher);

        // When
        voucherService.changeHotStatus(voucherId, newHotStatus);

        // Then
        verify(voucherRepository, times(1)).save(any(Voucher.class));
    }

    @Test
    void searchWithFilters_ValidFilters_ReturnsFiltered() {
        // Given
        String title = "Paris";
        String tourType = "LEISURE";
        Pageable pageable = PageRequest.of(0, 10);

        Voucher voucher = new Voucher();
        voucher.setTitle("Paris Tour");
        voucher.setTourType(TourType.LEISURE);

        Page<Voucher> voucherPage = new PageImpl<>(List.of(voucher));
        VoucherDTO voucherDTO = new VoucherDTO();
        voucherDTO.setTitle("Paris Tour");

        when(voucherRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(voucherPage);
        when(voucherMapper.toVoucherDTO(any(Voucher.class))).thenReturn(voucherDTO);

        // When
        Page<VoucherDTO> result = voucherService.searchWithFilters(title, tourType, null, null, null, "REGISTERED", pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Paris Tour", result.getContent().get(0).getTitle());
    }

    @Test
    void findAllByUserId_ValidUser_ReturnsVouchers() {
        // Given
        UUID userId = UUID.randomUUID();

        Voucher voucher1 = new Voucher();
        voucher1.setTitle("Voucher 1");

        Voucher voucher2 = new Voucher();
        voucher2.setTitle("Voucher 2");

        VoucherDTO dto1 = new VoucherDTO();
        dto1.setTitle("Voucher 1");

        VoucherDTO dto2 = new VoucherDTO();
        dto2.setTitle("Voucher 2");

        when(voucherRepository.findAllByUserId(any(UUID.class))).thenReturn(List.of(voucher1, voucher2));
        when(voucherMapper.toVoucherDTO(voucher1)).thenReturn(dto1);
        when(voucherMapper.toVoucherDTO(voucher2)).thenReturn(dto2);

        // When
        List<VoucherDTO> result = voucherService.findAllByUserId(userId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(voucherRepository, times(1)).findAllByUserId(any(UUID.class));
    }
}
