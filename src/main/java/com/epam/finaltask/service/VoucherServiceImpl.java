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
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VoucherServiceImpl implements VoucherService {

    private final VoucherRepository voucherRepository;
    private final VoucherMapper voucherMapper;
    private final UserRepository userRepository;


    @Override
    public VoucherDTO create(VoucherDTO voucherDTO) {
        Voucher voucher = voucherMapper.toVoucher(voucherDTO);
        voucher.setStatus(VoucherStatus.REGISTERED);
        voucher.setHot(false);
        return voucherMapper.toVoucherDTO(voucherRepository.save(voucher));
    }

    @Override
    @Transactional
    public VoucherDTO order(String id, String userId) {
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
        return voucherMapper.toVoucherDTO(voucherRepository.save(voucher));
    }

    @Override
    @Transactional
    public VoucherDTO update(String id, VoucherDTO voucherDTO) {
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
        return voucherMapper.toVoucherDTO(voucherRepository.save(voucher));
    }

    @Override
    @Transactional
    public void delete(String voucherId) {
        voucherRepository.deleteById(UUID.fromString(voucherId));
    }

    @Override
    @Transactional
    public VoucherDTO changeHotStatus(String id, VoucherDTO voucherDTO) {
        Voucher voucher = voucherRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new EntityNotFoundException("Voucher not found"));
         voucher.setHot(voucherDTO.getIsHot());
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
