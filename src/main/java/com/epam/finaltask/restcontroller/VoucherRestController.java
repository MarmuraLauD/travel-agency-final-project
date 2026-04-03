package com.epam.finaltask.restcontroller;

import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.model.HotelType;
import com.epam.finaltask.model.TourType;
import com.epam.finaltask.model.TransferType;
import com.epam.finaltask.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vouchers")
@RequiredArgsConstructor
public class VoucherRestController {

    private final VoucherService voucherService;

    @PostMapping("/")
    public ResponseEntity<VoucherDTO>  createVoucher(@RequestBody VoucherDTO voucherDTO){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(voucherService.create(voucherDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VoucherDTO> updateVoucher(@PathVariable("id") String id,
                                                    @RequestBody VoucherDTO voucherDTO){
        voucherDTO.setId(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(voucherService.update(id, voucherDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVoucher(@PathVariable("id") String id){
        voucherService.delete(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PatchMapping("/{id}/hotStatus")
    public ResponseEntity<VoucherDTO> changeHotStatus(@PathVariable("id") String id,
                                                      @RequestBody VoucherDTO voucherDTO){
        voucherDTO.setId(id);
        return ResponseEntity.status(HttpStatus.OK).body(voucherService.changeHotStatus(id, voucherDTO));
    }

    @PostMapping("/{id}/order")
    public ResponseEntity<VoucherDTO> order(@PathVariable("id") String id, @RequestParam String userId){
        return  ResponseEntity.status(HttpStatus.OK)
                .body(voucherService.order(id, userId));
    }

    @GetMapping
    public ResponseEntity<List<VoucherDTO>> getVouchersByParameter(@RequestParam(required = false) String userId,
                                                       @RequestParam(required = false) String tourType,
                                                       @RequestParam(required = false) String transferType,
                                                       @RequestParam(required = false) String price,
                                                       @RequestParam(required = false) String hotelType){
        if(userId != null){
            return ResponseEntity.status(HttpStatus.OK)
                    .body(voucherService.findAllByUserId(userId));
        }else if(tourType != null){
            return ResponseEntity.status(HttpStatus.OK)
                    .body(voucherService.findAllByTourType(TourType.valueOf(tourType)));
        }else if(transferType != null){
            return ResponseEntity.status(HttpStatus.OK)
                    .body(voucherService.findAllByTransferType(transferType));
        }else if(price != null){
            return ResponseEntity.status(HttpStatus.OK)
                    .body(voucherService.findAllByPrice(Double.parseDouble(price)));
        }else if(hotelType != null){
            return ResponseEntity.status(HttpStatus.OK)
                    .body(voucherService.findAllByHotelType(HotelType.valueOf(hotelType)));
        } else {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(voucherService.findAll());
        }
    }
}
