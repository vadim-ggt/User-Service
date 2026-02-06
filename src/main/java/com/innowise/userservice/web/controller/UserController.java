package com.innowise.userservice.web.controller;


import com.innowise.userservice.web.dto.user.FilterUserDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.innowise.userservice.domain.service.UserService;
import com.innowise.userservice.web.dto.user.CreateUserDto;
import com.innowise.userservice.web.dto.user.GetUserDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasRole('INTERNAL') or hasRole('ADMIN')")
    public ResponseEntity<GetUserDto> createUser(
            @RequestBody @Valid CreateUserDto createUserDto) {
        GetUserDto user = userService.createUser(createUserDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<GetUserDto>> getAllUsers(Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.getAllUsers(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityHelper.isOwner(#id)")
    public ResponseEntity<GetUserDto> getUserById(@PathVariable Long id) {
        GetUserDto user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<GetUserDto>> findUsersByFilters(
            @ModelAttribute FilterUserDto filter,
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                userService.findUsersByFilters(filter, pageable)
        );
    }

    @GetMapping("/by-email")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INTERNAL') or @securityHelper.isEmailOwner(#email)")
    public ResponseEntity<GetUserDto> getUserByEmail(
            @RequestParam String email
    ) {
        return ResponseEntity.ok(
                userService.getUserByEmail(email)
        );
    }

    @GetMapping("/by-surname")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<GetUserDto>> getUserBySurname(
            @RequestParam String surname,
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                userService.getUserByFirstLettersOfSurname(surname, pageable)
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityHelper.isOwner(#id)")
    public ResponseEntity<GetUserDto> updateUser(
            @PathVariable Long id,
            @RequestBody @Valid CreateUserDto dto
    ) {
        return ResponseEntity.ok(userService.updateUser(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityHelper.isOwner(#id)")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/active")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INTERNAL')")
    public ResponseEntity<Void> setUserActiveStatus(
            @PathVariable Long id,
            @RequestParam Boolean active
    ) {
        userService.setUserActiveStatus(id, active);
        return ResponseEntity.noContent().build();
    }

}
