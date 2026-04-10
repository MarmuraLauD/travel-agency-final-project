package com.epam.finaltask.restcontroller;

import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.model.HotelType;
import com.epam.finaltask.model.TourType;
import com.epam.finaltask.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @PreAuthorize("hasAuthority('voucher:update')")
    @PostMapping("/{id}/order")
    public ResponseEntity<VoucherDTO> order(@PathVariable String id, @RequestParam String userId){
        return ResponseEntity.status(HttpStatus.OK)
                .body(voucherService.order(id, userId));
    }

    @PreAuthorize("hasAuthority('voucher:read')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getVouchersByUserId(@PathVariable String userId) {
        Map<String, Object> response = new HashMap<>();
        response.put("results", voucherService.findAllByUserId(userId));
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAuthority('voucher:read')")
    @GetMapping
    public ResponseEntity<Map<String, Object>> getVouchersByParameter(
            @RequestParam(required = false) String tourType,
            @RequestParam(required = false) String transferType,
            @RequestParam(required = false) String price,
            @RequestParam(required = false) String hotelType) {

        List<VoucherDTO> results;

        if (tourType != null) {
            results = voucherService.findAllByTourType(TourType.valueOf(tourType));
        } else if (transferType != null) {
            results = voucherService.findAllByTransferType(transferType);
        } else if (price != null) {
            results = voucherService.findAllByPrice(Double.parseDouble(price));
        } else if (hotelType != null) {
            results = voucherService.findAllByHotelType(HotelType.valueOf(hotelType));
        } else {
            results = voucherService.findAll();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("results", results);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}