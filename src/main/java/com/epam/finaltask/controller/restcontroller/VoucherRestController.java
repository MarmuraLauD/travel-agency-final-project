package com.epam.finaltask.controller.restcontroller;

import com.epam.finaltask.dto.VoucherDTO;
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

import java.time.LocalDate;
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
                                                               @RequestBody VoucherDTO voucherDTO) {
        boolean hotStatus = (voucherDTO.getHot() != null) ? voucherDTO.getHot() : false;

        voucherService.changeHotStatus(id, hotStatus);

        Map<String, String> response = new HashMap<>();
        response.put("statusCode", "OK");
        response.put("statusMessage", "Voucher status changed");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{id}/order")
    public ResponseEntity<VoucherDTO> order(@PathVariable String id,
                                            @RequestParam String userId,
                                            @RequestParam LocalDate arrivalDate){
        return ResponseEntity.status(HttpStatus.OK)
                .body(voucherService.order(id, userId, arrivalDate));
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
            @RequestParam(required = false) String status) {

        Sort.Direction direction = sort[1].equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort[0]));

        Page<VoucherDTO> pageTours;

        if ("ALL".equals(status)) {
            pageTours = voucherService.findAll(pageable);
        } else {
            pageTours = voucherService.findAllByStatus("REGISTERED", pageable);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("vouchers", pageTours.getContent());
        response.put("currentPage", pageTours.getNumber());
        response.put("totalItems", pageTours.getTotalElements());
        response.put("totalPages", pageTours.getTotalPages());
        response.put("sortField", sort[0]);
        response.put("sortDir", sort[1]);

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable UUID id) {
        voucherService.cancelOrder(id);
        return ResponseEntity.ok().build();
    }
}