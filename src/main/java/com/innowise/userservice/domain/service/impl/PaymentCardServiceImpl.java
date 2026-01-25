package com.innowise.userservice.domain.service.impl;

import com.innowise.userservice.domain.dao.PaymentCardRepository;
import com.innowise.userservice.domain.dao.UserRepository;
import com.innowise.userservice.domain.entity.PaymentCard;
import com.innowise.userservice.domain.entity.User;
import com.innowise.userservice.domain.exeption.CardAlreadyExistsException;
import com.innowise.userservice.domain.exeption.CardLimitExceededException;
import com.innowise.userservice.domain.exeption.CardNotFoundException;
import com.innowise.userservice.domain.exeption.UserNotFoundException;
import com.innowise.userservice.domain.mapper.card.CreateCardMapper;
import com.innowise.userservice.domain.mapper.card.GetCardMapper;
import com.innowise.userservice.domain.service.PaymentCardService;
import com.innowise.userservice.domain.specification.PaymentCardSpecification;
import com.innowise.userservice.web.dto.card.CreateCardDto;
import com.innowise.userservice.web.dto.card.FilterCardDto;
import com.innowise.userservice.web.dto.card.GetCardDto;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentCardServiceImpl implements PaymentCardService {
    private final PaymentCardRepository paymentCardRepository;
    private final CreateCardMapper createCardMapper;
    private final GetCardMapper getCardMapper;
    private final UserRepository userRepository;
    private final CacheManager cacheManager;

    private void evictUserCache(Long userId) {
        if (cacheManager.getCache("users") != null) {
            cacheManager.getCache("users").evict(userId);
        }
    }


    @Override
    @Transactional
    public GetCardDto createCard(Long userId, CreateCardDto createCardDto) {
        if (paymentCardRepository.existsByNumber(createCardDto.getNumber())) {
            throw new CardAlreadyExistsException(createCardDto.getNumber());
        }

        long cardCount = paymentCardRepository.countByUserId(userId);
        if (cardCount >= 5) {
            throw new CardLimitExceededException(userId);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        PaymentCard paymentCard = createCardMapper.toEntity(createCardDto);
        paymentCard.setUser(user);

        PaymentCard savedCard = paymentCardRepository.save(paymentCard);
        evictUserCache(userId);
        return getCardMapper.toDto(savedCard);
    }

    @Override
    public GetCardDto findCardById(Long id) {
        PaymentCard card = paymentCardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException(id));
        return getCardMapper.toDto(card);
    }

    @Override
    public Page<GetCardDto> getAllCards(Pageable pageable) {
        Page<PaymentCard> paymentCards = paymentCardRepository.findAll(pageable);
        return paymentCards.map(getCardMapper::toDto);
    }

    @Override
    public Page<GetCardDto> getCardsByUserId(Long userId, Pageable pageable) {
        Page<PaymentCard> paymentCards = paymentCardRepository.findByUserId(userId, pageable);
        return paymentCards.map(getCardMapper::toDto);
    }

    @Override
    @Transactional
    public void deleteCardById(Long id) {
        PaymentCard deleteCard = paymentCardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException(id));
        Long userId = deleteCard.getUser().getId();
        paymentCardRepository.delete(deleteCard);
        evictUserCache(userId);
    }

    @Override
    @Transactional
    public GetCardDto updateCard(Long id, CreateCardDto createCardDto) {
        PaymentCard existCard = paymentCardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException(id));
    createCardMapper.merge(existCard, createCardDto);
    PaymentCard updateCard = paymentCardRepository.save(existCard);
    evictUserCache(updateCard.getUser().getId());
    return getCardMapper.toDto(updateCard);
    }

    @Override
    public Page<GetCardDto> findCardsByFilter(FilterCardDto filter, Pageable pageable) {

        Specification<PaymentCard> spec = Specification
                .allOf(
                        filter.getUserId() != null
                                ? PaymentCardSpecification.hasUserId(filter.getUserId())
                                : null,
                        filter.getUserName() != null && !filter.getUserName().isBlank()
                                ? PaymentCardSpecification.hasUserName(filter.getUserName())
                                : null,
                        filter.getUserSurname() != null && !filter.getUserSurname().isBlank()
                                ? PaymentCardSpecification.hasUserSurname(filter.getUserSurname())
                                : null,
                        filter.getActive() != null
                                ? PaymentCardSpecification.isActive(filter.getActive())
                                : null
                );

        return paymentCardRepository.findAll(spec, pageable)
                .map(getCardMapper::toDto);
    }

    @Override
    @Transactional
    public void setCardActiveStatus(Long cardId, Boolean active) {
        PaymentCard card = paymentCardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));
        paymentCardRepository.setCardActiveStatus(cardId, active);
        evictUserCache(card.getUser().getId());
    }


}
