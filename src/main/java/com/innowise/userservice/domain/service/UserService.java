package com.innowise.userservice.domain.service;


import com.innowise.userservice.web.dto.user.CreateUserDto;
import com.innowise.userservice.web.dto.user.GetUserDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface UserService {

    GetUserDto createUser(CreateUserDto getUserDto);

    GetUserDto getUserById(Long id);

    GetUserDto getUserByEmail(String email);

    Page<GetUserDto> getUserByFirstLettersOfSurname(String letter, Pageable pageable);

    Page<GetUserDto> getAllUsers(Pageable pageable);

    GetUserDto updateUser(Long id, CreateUserDto dto);

    void deleteUser(Long id);

    Page<GetUserDto> getActiveUsers(Pageable pageable);

    void setUserActiveStatus(Long userId, Boolean active);

}
