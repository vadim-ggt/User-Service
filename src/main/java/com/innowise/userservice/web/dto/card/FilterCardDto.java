package com.innowise.userservice.web.dto.card;

import lombok.Data;

@Data
public class FilterCardDto { // чтобы не менять сервисные методы
    private Long userId;
    private String userName;
    private String userSurname;
    private Boolean active;
}