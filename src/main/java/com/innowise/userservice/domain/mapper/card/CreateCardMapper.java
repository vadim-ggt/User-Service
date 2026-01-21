package com.innowise.userservice.domain.mapper.card;

import com.innowise.userservice.domain.entity.PaymentCard;
import com.innowise.userservice.domain.mapper.GenericMapper;
import com.innowise.userservice.web.dto.card.CreateCardDto;
import org.mapstruct.Mapper;

@Mapper(config = GenericMapper.class)
public interface CreateCardMapper
        extends GenericMapper<PaymentCard, CreateCardDto> {
}
