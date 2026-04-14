package com.epam.finaltask.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class VoucherDTO {

    private String id;

    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @Positive
    @NotNull
    private Double price;

    @NotBlank
    private String tourType;

    @NotBlank
    private String transferType;

    @NotBlank
    private String hotelType;

    @NotBlank
    private String status;

    @Future
    private LocalDate arrivalDate;

    @Future
    private LocalDate evictionDate;

    @JsonIgnore
    private UUID userId;

    @NotNull
    @JsonProperty("hot")
    private Boolean hot;
}
