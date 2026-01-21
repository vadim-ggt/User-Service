package com.innowise.userservice.domain.service.impl;

import com.innowise.userservice.domain.service.UserService;
import com.innowise.userservice.web.dto.user.CreateUserDto;
import com.innowise.userservice.web.dto.user.GetUserDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public class UserServiceImpl implements UserService {
    @Override
    public GetUserDto createUser(CreateUserDto getUserDto) {
        return null;
    }

    @Override
    public GetUserDto getUserById(Long id) {
        return null;
    }

    @Override
    public GetUserDto getUserByEmail(String email) {
        return null;
    }

    @Override
    public Page<GetUserDto> getUserByFirstLettersOfSurname(String letter, Pageable pageable) {
        return null;
    }

    @Override
    public Page<GetUserDto> getAllUsers(Pageable pageable) {
        return null;
    }

    @Override
    public GetUserDto updateUser(Long id, CreateUserDto dto) {
        return null;
    }

    @Override
    public void deleteUser(Long id) {

    }

    @Override
    public Page<GetUserDto> getActiveUsers(Pageable pageable) {
        return null;
    }

    @Override
    public void setUserActiveStatus(Long userId, Boolean active) {

    }
}
