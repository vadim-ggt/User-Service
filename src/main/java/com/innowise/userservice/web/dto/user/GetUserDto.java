package com.innowise.userservice.web.dto.user;


import com.innowise.userservice.web.dto.card.GetCardDto;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data
public class GetUserDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String surname;
    private LocalDate birthDate;
    private String email;
    private Boolean active;
    private List<GetCardDto> cards;
}