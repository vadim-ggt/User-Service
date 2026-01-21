package com.innowise.userservice.web.dto.card;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateCardDto {

    @NotBlank(message = "Card number is required")
    @Size(max = 255, message = "Card number cannot be longer than 255 characters")
    private String number;

    @NotBlank(message = "Card holder is required")
    @Size(max = 255, message = "Card holder cannot be longer than 255 characters")
    private String holder;

    @NotBlank(message = "Expiration date is required")
    @Pattern(
            regexp = "^(0[1-9]|1[0-2])/([0-9]{2})$",
            message = "Invalid format. Must be MM/YY"
    )
    private String expirationDate;
}
