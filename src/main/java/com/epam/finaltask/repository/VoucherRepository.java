package com.epam.finaltask.repository;

import java.util.List;
import java.util.UUID;

import com.epam.finaltask.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface VoucherRepository extends JpaRepository<Voucher, UUID>, JpaSpecificationExecutor<Voucher> {
    List<Voucher> findAllByUserId(UUID userId);
    Page<Voucher> findAllByTourType(TourType tourType, Pageable pageable);
    Page<Voucher> findAllByTransferType(TransferType transferType, Pageable pageable);
    Page<Voucher> findAllByPrice(Double price, Pageable pageable);
    Page<Voucher> findAllByHotelType(HotelType hotelType, Pageable pageable);
    Page<Voucher> findAllByStatus(VoucherStatus voucherStatus, Pageable pageable);
    Page<Voucher> findAllByHotTrue(Pageable pageable);
    Page<Voucher> findAllByStatusAndHot(VoucherStatus status, boolean hot, Pageable pageable);
    Page<Voucher> findAllByStatusAndTitleContainingIgnoreCase(VoucherStatus status, String title, Pageable pageable);
}
