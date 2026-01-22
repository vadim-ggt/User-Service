package com.innowise.userservice.domain.service.impl;

import com.innowise.userservice.domain.dao.UserRepository;
import com.innowise.userservice.domain.entity.User;
import com.innowise.userservice.domain.mapper.user.CreateUserMapper;
import com.innowise.userservice.domain.mapper.user.GetUserMapper;
import com.innowise.userservice.domain.service.UserService;
import com.innowise.userservice.web.dto.user.CreateUserDto;
import com.innowise.userservice.web.dto.user.GetUserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final CreateUserMapper createUserMapper;
    private final GetUserMapper getUserMapper;

    @Override
    @Transactional
    public GetUserDto createUser(CreateUserDto getUserDto) {
        User user =  createUserMapper.toEntity(getUserDto);
        User savedUser = userRepository.save(user);
        return getUserMapper.toDto(savedUser);
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
