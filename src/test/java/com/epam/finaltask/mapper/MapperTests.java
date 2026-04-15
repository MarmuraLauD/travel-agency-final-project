package com.epam.finaltask.mapper;

import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.model.*;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MapperTests {

    private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);
    private final VoucherMapper voucherMapper = Mappers.getMapper(VoucherMapper.class);

    @Test
    void userMapper_toUserDTO_ConvertsCorrectly() {
        // Given
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("testuser");
        user.setPassword("hashedpassword");
        user.setPhoneNumber("+380123456789");
        user.setRole(Role.USER);
        user.setBalance(BigDecimal.valueOf(1000.50));
        user.setActive(true);
        user.setVouchers(new ArrayList<>());

        // When
        UserDTO userDTO = userMapper.toUserDTO(user);

        // Then
        assertNotNull(userDTO);
        assertEquals(user.getId().toString(), userDTO.getId());
        assertEquals("testuser", userDTO.getUsername());
        assertEquals("+380123456789", userDTO.getPhoneNumber());
        assertEquals("USER", userDTO.getRole());
        assertEquals(1000.50, userDTO.getBalance(), 0.01);
        assertTrue(userDTO.isActive());
        assertNotNull(userDTO.getVouchers());
    }

    @Test
    void userMapper_toUser_ConvertsCorrectly() {
        // Given
        UserDTO userDTO = new UserDTO();
        userDTO.setId(UUID.randomUUID().toString());
        userDTO.setUsername("newuser");
        userDTO.setPassword("rawpassword");
        userDTO.setPhoneNumber("+380987654321");
        userDTO.setRole("ADMIN");
        userDTO.setBalance(500.0);
        userDTO.setActive(false);
        userDTO.setVouchers(new ArrayList<>());

        // When
        User user = userMapper.toUser(userDTO);

        // Then
        assertNotNull(user);
        assertEquals(UUID.fromString(userDTO.getId()), user.getId());
        assertEquals("newuser", user.getUsername());
        assertEquals("+380987654321", user.getPhoneNumber());
        assertEquals(Role.ADMIN, user.getRole());
        assertEquals(BigDecimal.valueOf(500.0), user.getBalance());
        assertFalse(user.isActive());
        assertNotNull(user.getVouchers());
    }

    @Test
    void voucherMapper_toVoucherDTO_ConvertsCorrectly() {
        // Given
        UUID voucherId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        user.setUsername("voucherowner");

        Voucher voucher = new Voucher();
        voucher.setId(voucherId);
        voucher.setTitle("Paris Adventure");
        voucher.setDescription("Amazing tour to Paris with all amenities");
        voucher.setPrice(1500.0);
        voucher.setTourType(TourType.CULTURAL);
        voucher.setTransferType(TransferType.PLANE);
        voucher.setHotelType(HotelType.FIVE_STARS);
        voucher.setStatus(VoucherStatus.REGISTERED);
        voucher.setHot(true);
        voucher.setArrivalDate(LocalDate.of(2025, 6, 15));
        voucher.setEvictionDate(LocalDate.of(2025, 6, 22));
        voucher.setUser(user);

        // When
        VoucherDTO voucherDTO = voucherMapper.toVoucherDTO(voucher);

        // Then
        assertNotNull(voucherDTO);
        assertEquals(voucherId.toString(), voucherDTO.getId());
        assertEquals("Paris Adventure", voucherDTO.getTitle());
        assertEquals("Amazing tour to Paris with all amenities", voucherDTO.getDescription());
        assertEquals(1500.0, voucherDTO.getPrice(), 0.01);
        assertEquals("CULTURAL", voucherDTO.getTourType());
        assertEquals("PLANE", voucherDTO.getTransferType());
        assertEquals("FIVE_STARS", voucherDTO.getHotelType());
        assertEquals("REGISTERED", voucherDTO.getStatus());
        assertTrue(voucherDTO.getHot());
        assertEquals(LocalDate.of(2025, 6, 15), voucherDTO.getArrivalDate());
        assertEquals(LocalDate.of(2025, 6, 22), voucherDTO.getEvictionDate());
        assertEquals(userId.toString(), voucherDTO.getUserId());
    }

    @Test
    void voucherMapper_toVoucher_ConvertsCorrectly() {
        // Given
        VoucherDTO voucherDTO = new VoucherDTO();
        voucherDTO.setId(UUID.randomUUID().toString());
        voucherDTO.setTitle("Safari Experience");
        voucherDTO.setDescription("Unforgettable safari adventure in Africa");
        voucherDTO.setPrice(2500.0);
        voucherDTO.setTourType("SAFARI");
        voucherDTO.setTransferType("JEEPS");
        voucherDTO.setHotelType("FOUR_STARS");
        voucherDTO.setStatus("PENDING");
        voucherDTO.setHot(false);
        voucherDTO.setArrivalDate(LocalDate.of(2025, 8, 1));
        voucherDTO.setEvictionDate(LocalDate.of(2025, 8, 10));

        // When
        Voucher voucher = voucherMapper.toVoucher(voucherDTO);

        // Then
        assertNotNull(voucher);
        assertEquals(UUID.fromString(voucherDTO.getId()), voucher.getId());
        assertEquals("Safari Experience", voucher.getTitle());
        assertEquals("Unforgettable safari adventure in Africa", voucher.getDescription());
        assertEquals(2500.0, voucher.getPrice(), 0.01);
        assertEquals(TourType.SAFARI, voucher.getTourType());
        assertEquals(TransferType.JEEPS, voucher.getTransferType());
        assertEquals(HotelType.FOUR_STARS, voucher.getHotelType());
        assertEquals(VoucherStatus.PENDING, voucher.getStatus());
        assertFalse(voucher.isHot());
        assertEquals(LocalDate.of(2025, 8, 1), voucher.getArrivalDate());
        assertEquals(LocalDate.of(2025, 8, 10), voucher.getEvictionDate());
    }

    @Test
    void voucherMapper_toVoucherDTO_WithNullUser_HandlesCorrectly() {
        // Given
        Voucher voucher = new Voucher();
        voucher.setId(UUID.randomUUID());
        voucher.setTitle("Available Tour");
        voucher.setDescription("Not yet booked");
        voucher.setPrice(800.0);
        voucher.setTourType(TourType.LEISURE);
        voucher.setTransferType(TransferType.BUS);
        voucher.setHotelType(HotelType.THREE_STARS);
        voucher.setStatus(VoucherStatus.REGISTERED);
        voucher.setHot(false);
        voucher.setUser(null); // No user assigned yet

        // When
        VoucherDTO voucherDTO = voucherMapper.toVoucherDTO(voucher);

        // Then
        assertNotNull(voucherDTO);
        assertEquals("Available Tour", voucherDTO.getTitle());
        assertNull(voucherDTO.getUserId()); // Should handle null user gracefully
    }

    @Test
    void userMapper_toUserDTO_WithEmptyVouchers_HandlesCorrectly() {
        // Given
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("novouchers");
        user.setPassword("password");
        user.setPhoneNumber("+380111222333");
        user.setRole(Role.USER);
        user.setBalance(BigDecimal.ZERO);
        user.setActive(true);
        user.setVouchers(new ArrayList<>()); // Empty list

        // When
        UserDTO userDTO = userMapper.toUserDTO(user);

        // Then
        assertNotNull(userDTO);
        assertNotNull(userDTO.getVouchers());
        assertTrue(userDTO.getVouchers().isEmpty());
    }
}
