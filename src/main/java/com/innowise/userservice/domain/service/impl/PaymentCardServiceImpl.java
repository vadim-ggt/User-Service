package com.innowise.userservice.domain.service.impl;

import com.innowise.userservice.domain.service.PaymentCardService;
import com.innowise.userservice.web.dto.card.CreateCardDto;
import com.innowise.userservice.web.dto.card.FilterCardDto;
import com.innowise.userservice.web.dto.card.GetCardDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentCardServiceImpl implements PaymentCardService {


    @Override
    public GetCardDto createCard(Long userId, CreateCardDto createCardDto) {
        return null;
    }

    @Override
    public GetCardDto findCardById(Long id) {
        return null;
    }

    @Override
    public Page<GetCardDto> getAllCards(Pageable pageable) {
        return null;
    }

    @Override
    public void deleteCardById(Long id) {

    }

    @Override
    public Page<GetCardDto> findCardsByFilter(FilterCardDto filter, Pageable pageable) {
        return null;
    }

    @Override
    public void setCardActiveStatus(Long cardId, Boolean active) {

    }
}
