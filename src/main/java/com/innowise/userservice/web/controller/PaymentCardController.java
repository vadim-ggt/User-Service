package com.innowise.userservice.web.controller;

import com.innowise.userservice.domain.service.PaymentCardService;
import com.innowise.userservice.web.dto.card.FilterCardDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.innowise.userservice.web.dto.card.CreateCardDto;
import com.innowise.userservice.web.dto.card.GetCardDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class PaymentCardController {
    private final PaymentCardService paymentCardService;

    @PostMapping("/user/{userId}")
    public ResponseEntity<GetCardDto> createCard(
            @PathVariable Long userId,
            @RequestBody @Valid CreateCardDto createCardDto
    ) {
        GetCardDto card = paymentCardService.createCard(userId, createCardDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(card);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GetCardDto> getCardById(@PathVariable Long id) {
        GetCardDto card = paymentCardService.findCardById(id);
        return ResponseEntity.ok(card);
    }

    @GetMapping
    public ResponseEntity<Page<GetCardDto>> getAllCards(Pageable pageable) {
        return ResponseEntity.ok(paymentCardService.getAllCards(pageable));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<GetCardDto>> getCardsByUserId(
            @PathVariable Long userId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(paymentCardService.getCardsByUserId(userId, pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<GetCardDto>> findCardsByFilter(
            @ModelAttribute FilterCardDto filter,
            Pageable pageable
    ) {
        return ResponseEntity.ok(paymentCardService.findCardsByFilter(filter, pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GetCardDto> updateCard(
            @PathVariable Long id,
            @RequestBody @Valid CreateCardDto createCardDto
    ) {
        return ResponseEntity.ok(paymentCardService.updateCard(id, createCardDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        paymentCardService.deleteCardById(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<Void> setCardActiveStatus(
            @PathVariable Long id,
            @RequestParam Boolean active
    ) {
        paymentCardService.setCardActiveStatus(id, active);
        return ResponseEntity.noContent().build();
    }
}
