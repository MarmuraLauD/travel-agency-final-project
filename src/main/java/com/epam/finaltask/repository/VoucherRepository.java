package com.epam.finaltask.repository;

import java.util.List;
import java.util.UUID;

import com.epam.finaltask.model.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoucherRepository extends JpaRepository<Voucher, UUID> {
    List<Voucher> findAllByUserId(UUID userId);
    List<Voucher> findAllByTourType(TourType tourType);
    List<Voucher> findAllByTransferType(TransferType transferType);
    List<Voucher> findAllByPrice(Double price);
    List<Voucher> findAllByHotelType(HotelType hotelType);
    List<Voucher> findAllByVoucherStatus(VoucherStatus voucherStatus);
    List<Voucher> findAllByIsHot(Boolean isHot);
}
