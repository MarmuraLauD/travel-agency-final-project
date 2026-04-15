package com.epam.finaltask.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class VoucherDTO {

    private String id;

    @NotBlank(message = "Title cannot be empty")
    @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
    private String title;

    @NotBlank(message = "Description cannot be empty")
    @Size(min = 10, max = 1000, message = "Description must be between 10 and 1000 characters")
    private String description;

    @Positive(message = "Price must be positive")
    @NotNull(message = "Price cannot be empty")
    @DecimalMin(value = "0.01", message = "Minimum price is 0.01")
    @DecimalMax(value = "1000000.00", message = "Maximum price is 1,000,000")
    private Double price;

    @NotBlank(message = "Tour type cannot be empty")
    private String tourType;

    @NotBlank(message = "Transfer type cannot be empty")
    private String transferType;

    @NotBlank(message = "Hotel type cannot be empty")
    private String hotelType;

    @NotBlank(message = "Status cannot be empty")
    private String status;

    @FutureOrPresent(message = "Arrival date cannot be in the past")
    private LocalDate arrivalDate;

    @Future(message = "Eviction date must be in the future")
    private LocalDate evictionDate;

    @JsonIgnore
    private UUID userId;

    @NotNull(message = "Hot field cannot be empty")
    @JsonProperty("hot")
    private Boolean hot;
}
