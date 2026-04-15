package com.epam.finaltask.service;

import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.exception.InsufficientFundsException;
import com.epam.finaltask.mapper.VoucherMapper;
import com.epam.finaltask.model.*;
import com.epam.finaltask.repository.UserRepository;
import com.epam.finaltask.repository.VoucherRepository;
import com.epam.finaltask.specification.VoucherSpecification;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VoucherServiceImpl implements VoucherService {

    private final VoucherRepository voucherRepository;
    private final VoucherMapper voucherMapper;
    private final UserRepository userRepository;


    @Override
    public VoucherDTO create(VoucherDTO voucherDTO) {
        log.info("Attempt to create Voucher.");
        Voucher voucher = voucherMapper.toVoucher(voucherDTO);
        voucher.setStatus(VoucherStatus.REGISTERED);
        voucher.setHot(false);
        log.info("Created Voucher.");
        return voucherMapper.toVoucherDTO(voucherRepository.save(voucher));
    }

    @Override
    @Transactional
    public VoucherDTO order(String id, String userId, LocalDate arrivalDate) {
        log.info("User ID: {} is attempting to add Voucher ID: {} to cart", userId, id);

        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        Voucher voucher = voucherRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new EntityNotFoundException("Voucher not found"));

        voucher.setStatus(VoucherStatus.PENDING);
        voucher.setArrivalDate(arrivalDate);
        voucher.setEvictionDate(arrivalDate.plusDays(7));

        voucher.setUser(user);

        if (user.getVouchers() == null) {
            user.setVouchers(new ArrayList<>());
        }
        user.addVoucher(voucher);

        userRepository.save(user);
        Voucher savedVoucher = voucherRepository.save(voucher);

        log.info("Voucher ID: {} successfully added to cart by User ID: {}", id, userId);
        return voucherMapper.toVoucherDTO(savedVoucher);
    }

    @Override
    @Transactional
    public VoucherDTO confirmOrder(UUID voucherId) {
        log.info("Attempting to confirm Voucher ID: {}", voucherId);

        Voucher voucher = voucherRepository.findById(voucherId)
                .orElseThrow(() -> new EntityNotFoundException("Voucher not found"));

        if (voucher.getStatus() != VoucherStatus.PENDING) {
            throw new RuntimeException("Only pending vouchers can be confirmed");
        }

        User user = voucher.getUser();
        if (user == null) {
            throw new RuntimeException("Voucher has no associated user");
        }

        BigDecimal price = BigDecimal.valueOf(voucher.getPrice());
        BigDecimal balance = user.getBalance();

        if (balance.compareTo(price) < 0) {
            throw new InsufficientFundsException(price.subtract(balance).doubleValue());
        }

        user.setBalance(balance.subtract(price));
        voucher.setStatus(VoucherStatus.CONFIRMED);

        userRepository.save(user);
        Voucher savedVoucher = voucherRepository.save(voucher);

        log.info("Voucher ID: {} successfully confirmed and paid", voucherId);
        return voucherMapper.toVoucherDTO(savedVoucher);
    }


    @Override
    @Transactional
    public VoucherDTO update(String id, VoucherDTO voucherDTO) {
        log.info("Attempting to update Voucher with ID: {}.", id);
        Voucher voucher = voucherRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new EntityNotFoundException("Voucher not found"));
        voucher.setTitle(voucherDTO.getTitle());
        voucher.setDescription(voucherDTO.getDescription());
        voucher.setPrice(voucherDTO.getPrice());
        voucher.setTourType(TourType.valueOf(voucherDTO.getTourType()));
        voucher.setTransferType(TransferType.valueOf(voucherDTO.getTransferType()));
        voucher.setHotelType(HotelType.valueOf(voucherDTO.getHotelType()));
        voucher.setArrivalDate(voucherDTO.getArrivalDate());
        voucher.setEvictionDate(voucherDTO.getEvictionDate());
        log.info("Updated Voucher with ID: {}.", id);
        return voucherMapper.toVoucherDTO(voucherRepository.save(voucher));
    }

    @Override
    @Transactional
    public void delete(String voucherId) {
        log.info("Attempting to delete Voucher with ID: {}.", voucherId);
        voucherRepository.deleteById(UUID.fromString(voucherId));
        log.info("Deleted Voucher with ID: {}.", voucherId);
    }

    @Override
    @Transactional
    public void cancelOrder(UUID voucherId) {
        Voucher voucher = voucherRepository.findById(voucherId)
                .orElseThrow(() -> new EntityNotFoundException("Voucher not found"));

        if (voucher.getStatus() != VoucherStatus.PENDING) {
            throw new RuntimeException("Only pending vouchers can be canceled");
        }

        User user = voucher.getUser();
        if (user != null) {
            user.getVouchers().remove(voucher);
            userRepository.save(user);
        }

        voucher.setUser(null);
        voucher.setStatus(VoucherStatus.REGISTERED);
        voucher.setArrivalDate(null);
        voucher.setEvictionDate(null);
        voucherRepository.save(voucher);
    }

    @Override
    @Transactional
    public void changeHotStatus(String id, boolean hot) {
        log.info("Attempting to change hot status of Voucher with ID: {}.", id);
        Voucher voucher = voucherRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new EntityNotFoundException("Voucher not found"));
         voucher.setHot(hot);
         log.info("Changed hot status of Voucher with ID: {}.", id);
        voucherMapper.toVoucherDTO(voucherRepository.save(voucher));
    }

    @Override
    public List<VoucherDTO> findAllByUserId(UUID userId) {
        return voucherRepository.findAllByUserId(UUID.fromString(userId.toString()))
                .stream()
                .map(voucherMapper::toVoucherDTO)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public Page<VoucherDTO> findAllByStatus(String status, Pageable pageable) {
        if (status == null || status.equalsIgnoreCase("REGISTERED")) {
            return voucherRepository.findAllByStatus(VoucherStatus.REGISTERED, pageable)
                    .map(voucherMapper::toVoucherDTO);
        }
        return voucherRepository.findAll(pageable).map(voucherMapper::toVoucherDTO);
    }

    @Override
    public Page<VoucherDTO> findAllByHot(Pageable pageable) {
        return voucherRepository.findAllByHotTrue(pageable).map(voucherMapper::toVoucherDTO);
    }

    @Override
    public Page<VoucherDTO> findAll(Pageable pageable) {
        return voucherRepository.findAll(pageable)
                .map(voucherMapper::toVoucherDTO);
    }

    @Override
    public Page<VoucherDTO> findAllByStatusAndHot(String status, boolean hot, Pageable pageable) {
        VoucherStatus voucherStatus = VoucherStatus.valueOf(status.toUpperCase());
        return voucherRepository.findAllByStatusAndHot(voucherStatus, hot, pageable)
                .map(voucherMapper::toVoucherDTO);
    }

    @Override
    public Page<VoucherDTO> searchByTitle(String title, String status, Pageable pageable) {
        log.info("Searching vouchers by title: {}, status: {}", title, status);
        VoucherStatus voucherStatus = VoucherStatus.valueOf(status.toUpperCase());

        if (title == null || title.trim().isEmpty()) {
            return voucherRepository.findAllByStatus(voucherStatus, pageable)
                    .map(voucherMapper::toVoucherDTO);
        }

        return voucherRepository.findAllByStatusAndTitleContainingIgnoreCase(voucherStatus, title.trim(), pageable)
                .map(voucherMapper::toVoucherDTO);
    }

    @Override
    public Page<VoucherDTO> searchWithFilters(String title, String tourType, String transferType,
                                               String hotelType, Boolean hot, String status, Pageable pageable) {
        log.info("Searching vouchers with filters - title: {}, tourType: {}, transferType: {}, hotelType: {}, hot: {}, status: {}",
                title, tourType, transferType, hotelType, hot, status);

        Specification<Voucher> spec = VoucherSpecification.filterVouchers(
                title, tourType, transferType, hotelType, hot, status);

        return voucherRepository.findAll(spec, pageable)
                .map(voucherMapper::toVoucherDTO);
    }
}
