package com.epam.finaltask.service;

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
    VoucherDTO order(String id, String userId);
    VoucherDTO update(String id, VoucherDTO voucherDTO);
    void delete(String voucherId);
    VoucherDTO changeHotStatus(String id, VoucherDTO voucherDTO);
    List<VoucherDTO> findAllByUserId(UUID userId);

    Page<VoucherDTO> findAllByTourType(TourType tourType, Pageable pageable);
    Page<VoucherDTO> findAllByTransferType(TransferType transferType, Pageable pageable);
    Page<VoucherDTO> findAllByPrice(Double price, Pageable pageable);
    Page<VoucherDTO> findAllByHotelType(HotelType hotelType, Pageable pageable);

    Page<VoucherDTO> findAll(Pageable pageable);
}
