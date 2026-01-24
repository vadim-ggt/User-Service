package com.innowise.userservice.web.dto.card;


import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class GetCardDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String number;
    private String holder;
    private String expirationDate;
    private Boolean active;
}
