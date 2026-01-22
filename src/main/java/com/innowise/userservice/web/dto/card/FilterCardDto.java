package com.innowise.userservice.web.dto.card;

import lombok.Data;

@Data
public class FilterCardDto {
    private Long userId;
    private String userName;
    private String userSurname;
    private Boolean active;
}