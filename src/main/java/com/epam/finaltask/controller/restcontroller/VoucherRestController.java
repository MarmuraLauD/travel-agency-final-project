package com.epam.finaltask.controller.restcontroller;

import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.model.HotelType;
import com.epam.finaltask.model.TourType;
import com.epam.finaltask.model.TransferType;
import com.epam.finaltask.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/vouchers")
@RequiredArgsConstructor
public class VoucherRestController {

    private final VoucherService voucherService;

    @PreAuthorize("hasAuthority('voucher:create')")
    @PostMapping
    public ResponseEntity<Map<String, String>> createVoucher(@RequestBody VoucherDTO voucherDTO){
        voucherService.create(voucherDTO);
        Map<String, String> response = new HashMap<>();
        response.put("statusCode", "OK");
        response.put("statusMessage", "Voucher is successfully created");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasAuthority('voucher:update')")
    @PatchMapping("/{id}")
    public ResponseEntity<Map<String, String>> updateVoucher(@PathVariable String id,
                                                             @RequestBody VoucherDTO voucherDTO){
        voucherDTO.setId(id);
        voucherService.update(id, voucherDTO);
        Map<String, String> response = new HashMap<>();
        response.put("statusCode", "OK");
        response.put("statusMessage", "Voucher is successfully updated");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PreAuthorize("hasAuthority('voucher:delete')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteVoucher(@PathVariable String id){
        voucherService.delete(id);
        Map<String, String> response = new HashMap<>();
        response.put("statusCode", "OK");
        response.put("statusMessage", "Voucher with Id " + id + " has been deleted");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PreAuthorize("hasAuthority('voucher:update')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<Map<String, String>> changeHotStatus(@PathVariable String id,
                                                               @RequestBody VoucherDTO voucherDTO){
        voucherDTO.setId(id);
        voucherService.changeHotStatus(id, voucherDTO);
        Map<String, String> response = new HashMap<>();
        response.put("statusCode", "OK");
        response.put("statusMessage", "Voucher status is successfully changed");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{id}/order")
    public ResponseEntity<VoucherDTO> order(@PathVariable String id, @RequestParam String userId){
        return ResponseEntity.status(HttpStatus.OK)
                .body(voucherService.order(id, userId));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getVouchersByUserId(@PathVariable String userId) {
        Map<String, Object> response = new HashMap<>();
        response.put("results", voucherService.findAllByUserId(UUID.fromString(userId)));
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAuthority('voucher:read')")
    @GetMapping
    public ResponseEntity<Map<String, Object>> getVouchersByParameter(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String[] sort,
            @RequestParam(required = false) String tourType,
            @RequestParam(required = false) String transferType,
            @RequestParam(required = false) String price,
            @RequestParam(required = false) String hotelType) {

        String sortField = sort[0];
        String sortDirection = sort[1];
        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc")
                ? Sort.Direction.DESC : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

        Page<VoucherDTO> pageTours;

        if (tourType != null) {
            pageTours = voucherService.findAllByTourType(TourType.valueOf(tourType), pageable);
        } else if (transferType != null) {
            pageTours = voucherService.findAllByTransferType(TransferType.valueOf(transferType), pageable);
        } else if (price != null) {
            pageTours = voucherService.findAllByPrice(Double.parseDouble(price), pageable);
        } else if (hotelType != null) {
            pageTours = voucherService.findAllByHotelType(HotelType.valueOf(hotelType), pageable);
        } else {
            pageTours = voucherService.findAll(pageable);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("vouchers", pageTours.getContent());
        response.put("currentPage", pageTours.getNumber());
        response.put("totalItems", pageTours.getTotalElements());
        response.put("totalPages", pageTours.getTotalPages());

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable UUID id) {
        voucherService.cancelOrder(id);
        return ResponseEntity.ok().build();
    }
}