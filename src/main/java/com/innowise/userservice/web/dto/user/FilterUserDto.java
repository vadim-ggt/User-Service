package com.innowise.userservice.web.dto.user;

import lombok.Data;

@Data
public class FilterUserDto {
    private String name;
    private String surname;
    private Boolean active;
}