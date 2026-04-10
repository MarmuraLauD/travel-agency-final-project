package com.epam.finaltask.service;

import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.exception.InsufficientFundsException;
import com.epam.finaltask.mapper.VoucherMapper;
import com.epam.finaltask.model.*;
import com.epam.finaltask.repository.UserRepository;
import com.epam.finaltask.repository.VoucherRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
    public VoucherDTO order(String id, String userId) {
        log.info("User ID: {} is attempting to buy Voucher ID: {}", userId, id);
        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        Voucher voucher = voucherRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new EntityNotFoundException("Voucher not found"));
        BigDecimal price = BigDecimal.valueOf(voucher.getPrice());
        BigDecimal balance = user.getBalance();
        if (balance.compareTo(price) < 0) {
            throw new InsufficientFundsException(price.subtract(balance).doubleValue());
        }
        user.setBalance(balance.subtract(price));
        voucher.setStatus(VoucherStatus.PAID);
        if ((user.hasVoucher())) {
            user.addVoucher(voucher);
        } else {
            user.setVouchers(new ArrayList<>(List.of(voucher)));
        }
        userRepository.save(user);
        voucher.setUser(user);
        log.info("Voucher ID: {} successfully purchased by User ID: {}", id, userId);
        return voucherMapper.toVoucherDTO(voucherRepository.save(voucher));
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
    public VoucherDTO changeHotStatus(String id, VoucherDTO voucherDTO) {
        log.info("Attempting to change hot status of Voucher with ID: {}.", id);
        Voucher voucher = voucherRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new EntityNotFoundException("Voucher not found"));
         voucher.setHot(voucherDTO.getIsHot());
         log.info("Changed hot status of Voucher with ID: {}.", id);
        return voucherMapper.toVoucherDTO(voucherRepository.save(voucher));
    }

    @Override
    public List<VoucherDTO> findAllByUserId(String userId) {
        return voucherRepository.findAllByUserId(UUID.fromString(userId))
                .stream()
                .map(voucherMapper::toVoucherDTO)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public List<VoucherDTO> findAllByTourType(TourType tourType) {
        return voucherRepository.findAllByTourType(tourType)
                .stream()
                .map(voucherMapper::toVoucherDTO)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public List<VoucherDTO> findAllByTransferType(String transferType) {
        return voucherRepository.findAllByTransferType(TransferType.valueOf(transferType))
                .stream()
                .map(voucherMapper::toVoucherDTO)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public List<VoucherDTO> findAllByPrice(Double price) {
        return voucherRepository.findAllByPrice(price)
                .stream()
                .map(voucherMapper::toVoucherDTO)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public List<VoucherDTO> findAllByHotelType(HotelType hotelType) {
        return voucherRepository.findAllByHotelType(hotelType)
                .stream()
                .map(voucherMapper::toVoucherDTO)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public List<VoucherDTO> findAll() {
        return voucherRepository.findAll()
                .stream()
                .map(voucherMapper::toVoucherDTO)
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
