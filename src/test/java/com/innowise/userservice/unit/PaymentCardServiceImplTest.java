package com.innowise.userservice.unit;

import com.innowise.userservice.domain.dao.PaymentCardRepository;
import com.innowise.userservice.domain.dao.UserRepository;
import com.innowise.userservice.domain.entity.PaymentCard;
import com.innowise.userservice.domain.entity.User;
import com.innowise.userservice.domain.exeption.*;
import com.innowise.userservice.domain.mapper.card.CreateCardMapper;
import com.innowise.userservice.domain.mapper.card.GetCardMapper;
import com.innowise.userservice.domain.service.impl.PaymentCardServiceImpl;
import com.innowise.userservice.web.dto.card.CreateCardDto;
import com.innowise.userservice.web.dto.card.FilterCardDto;
import com.innowise.userservice.web.dto.card.GetCardDto;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Optional;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentCardServiceImplTest {
    @Mock
    private PaymentCardRepository paymentCardRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CreateCardMapper createCardMapper;
    @Mock
    private GetCardMapper getCardMapper;
    @Mock
    private CacheManager cacheManager;
    @Mock
    private Cache cache;

    @InjectMocks
    private PaymentCardServiceImpl cardService;

    @Test
    void createCard_success() {
        CreateCardDto dto = new CreateCardDto();
        dto.setNumber("123");

        User user = new User();
        user.setId(1L);

        PaymentCard card = new PaymentCard();
        PaymentCard saved = new PaymentCard();
        saved.setUser(user);

        when(paymentCardRepository.existsByNumber("123")).thenReturn(false);
        when(paymentCardRepository.countByUserId(1L)).thenReturn(0L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(createCardMapper.toEntity(dto)).thenReturn(card);
        when(paymentCardRepository.save(card)).thenReturn(saved);
        when(getCardMapper.toDto(saved)).thenReturn(new GetCardDto());

        when(cacheManager.getCache("users")).thenReturn(cache);

        cardService.createCard(1L, dto);

        verify(cache).evict(1L);
        verifyNoMoreInteractions(cache);
    }



    @Test
    void createCard_alreadyExists() {
        when(paymentCardRepository.existsByNumber("123")).thenReturn(true);

        CreateCardDto dto = new CreateCardDto();
        dto.setNumber("123");

        assertThrows(CardAlreadyExistsException.class,
                () -> cardService.createCard(1L, dto));
    }

    @Test
    void createCard_limitExceeded() {
        when(paymentCardRepository.existsByNumber(any())).thenReturn(false);
        when(paymentCardRepository.countByUserId(1L)).thenReturn(5L);

        assertThrows(CardLimitExceededException.class,
                () -> cardService.createCard(1L, new CreateCardDto()));
    }


    @Test
    void findCardById_success() {
        PaymentCard card = new PaymentCard();
        card.setId(1L);

        when(paymentCardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(getCardMapper.toDto(card)).thenReturn(new GetCardDto());

        GetCardDto result = cardService.findCardById(1L);

        assertNotNull(result);
        verify(paymentCardRepository).findById(1L);
        verify(getCardMapper).toDto(card);
    }

    @Test
    void findCardById_notFound() {
        when(paymentCardRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class,
                () -> cardService.findCardById(1L));

        verify(getCardMapper, never()).toDto(any());
    }

    @Test
    void getAllCards_success() {
        Pageable pageable = PageRequest.of(0, 10);
        PaymentCard card = new PaymentCard();

        when(paymentCardRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(card)));

        when(getCardMapper.toDto(card)).thenReturn(new GetCardDto());

        Page<GetCardDto> result = cardService.getAllCards(pageable);

        assertEquals(1, result.getTotalElements());
        verify(paymentCardRepository).findAll(pageable);
        verify(getCardMapper).toDto(card);
    }

    @Test
    void getCardsByUserId_success() {
        Pageable pageable = PageRequest.of(0, 10);
        PaymentCard card = new PaymentCard();

        when(paymentCardRepository.findByUserId(1L, pageable))
                .thenReturn(new PageImpl<>(List.of(card)));

        when(getCardMapper.toDto(card)).thenReturn(new GetCardDto());

        Page<GetCardDto> result = cardService.getCardsByUserId(1L, pageable);

        assertEquals(1, result.getTotalElements());
        verify(paymentCardRepository).findByUserId(1L, pageable);
    }

    @Test
    void deleteCardById_success() {
        User user = new User();
        user.setId(1L);

        PaymentCard card = new PaymentCard();
        card.setUser(user);

        when(paymentCardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(cacheManager.getCache("users")).thenReturn(cache);

        cardService.deleteCardById(1L);

        verify(paymentCardRepository).delete(card);
        verify(cache).evict(1L);
    }

    @Test
    void deleteCardById_notFound() {
        when(paymentCardRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class,
                () -> cardService.deleteCardById(1L));
    }

    @Test
    void updateCard_success() {
        User user = new User();
        user.setId(1L);

        PaymentCard card = new PaymentCard();
        card.setUser(user);

        when(paymentCardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(paymentCardRepository.save(card)).thenReturn(card);
        when(getCardMapper.toDto(card)).thenReturn(new GetCardDto());
        when(cacheManager.getCache("users")).thenReturn(cache);

        GetCardDto result = cardService.updateCard(1L, new CreateCardDto());

        assertNotNull(result);
        verify(createCardMapper).merge(eq(card), any(CreateCardDto.class));
        verify(paymentCardRepository).save(card);
        verify(cache).evict(1L);
    }

    @Test
    void updateCard_notFound() {
        when(paymentCardRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class,
                () -> cardService.updateCard(1L, new CreateCardDto()));
    }

    @Test
    void findCardsByFilter_success() {
        FilterCardDto filter = new FilterCardDto();
        Pageable pageable = PageRequest.of(0, 10);

        PaymentCard card = new PaymentCard();
        Page<PaymentCard> page = new PageImpl<>(List.of(card));

        when(paymentCardRepository.findAll(
                ArgumentMatchers.<Specification<PaymentCard>>any(),
                eq(pageable)
        )).thenReturn(page);

        when(getCardMapper.toDto(card)).thenReturn(new GetCardDto());

        Page<GetCardDto> result = cardService.findCardsByFilter(filter, pageable);

        assertEquals(1, result.getTotalElements());
        verify(paymentCardRepository).findAll(ArgumentMatchers.<Specification<PaymentCard>>any(), eq(pageable));
    }

    @Test
    void setCardActiveStatus_success() {
        User user = new User();
        user.setId(1L);

        PaymentCard card = new PaymentCard();
        card.setUser(user);

        when(paymentCardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(cacheManager.getCache("users")).thenReturn(cache);

        cardService.setCardActiveStatus(1L, false);

        verify(paymentCardRepository).setCardActiveStatus(1L, false);
        verify(cache).evict(1L);
    }

    @Test
    void setCardActiveStatus_notFound() {
        when(paymentCardRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class,
                () -> cardService.setCardActiveStatus(1L, false));
    }

}
