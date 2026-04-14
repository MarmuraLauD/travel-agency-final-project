package com.epam.finaltask.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.model.HotelType;
import com.epam.finaltask.model.TourType;
import com.epam.finaltask.model.TransferType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface VoucherService {
    VoucherDTO create(VoucherDTO voucherDTO);
    VoucherDTO order(String id, String userId, LocalDate arrivalDate);
    VoucherDTO update(String id, VoucherDTO voucherDTO);
    void delete(String voucherId);
    void changeHotStatus(String id, boolean hot);
    List<VoucherDTO> findAllByUserId(UUID userId);
    void cancelOrder(UUID voucherId);
    Page<VoucherDTO> findAllByStatus(String status, Pageable pageable);


    Page<VoucherDTO> findAll(Pageable pageable);
}
