package com.innowise.userservice.domain.service;


import com.innowise.userservice.web.dto.card.CreateCardDto;
import com.innowise.userservice.web.dto.card.FilterCardDto;
import com.innowise.userservice.web.dto.card.GetCardDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentCardService {

    GetCardDto createCard(Long userId, CreateCardDto createCardDto);

    GetCardDto findCardById(Long id);

    Page<GetCardDto> getAllCards(Pageable pageable);

    void deleteCardById(Long id);

    Page<GetCardDto> findCardsByFilter(FilterCardDto filter, Pageable pageable);

    void setCardActiveStatus(Long cardId, Boolean active);

}
