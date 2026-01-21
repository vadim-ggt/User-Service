package com.innowise.userservice.web.dto.card;


import lombok.Data;

@Data
public class GetCardDto {
    private Long id;
    private String number;
    private String holder;
    private String expirationDate;
    private Boolean active;
}
