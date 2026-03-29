package com.innowise.userservice.domain.service.impl;

import com.innowise.userservice.domain.dao.UserRepository;
import com.innowise.userservice.domain.entity.User;
import com.innowise.userservice.domain.exeption.UserAlreadyExistsException;
import com.innowise.userservice.domain.exeption.UserNotFoundException;
import com.innowise.userservice.domain.mapper.user.CreateUserMapper;
import com.innowise.userservice.domain.mapper.user.GetUserMapper;
import com.innowise.userservice.domain.service.UserService;
import com.innowise.userservice.domain.specification.UserSpecification;
import com.innowise.userservice.web.dto.user.CreateUserDto;
import com.innowise.userservice.web.dto.user.FilterUserDto;
import com.innowise.userservice.web.dto.user.GetUserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final CreateUserMapper createUserMapper;
    private final GetUserMapper getUserMapper;


    @Override
    @Transactional
    public GetUserDto createUser(CreateUserDto createUserDto) {
        if (userRepository.findByEmail(createUserDto.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException(createUserDto.getEmail());
        }

        User user =  createUserMapper.toEntity(createUserDto);

        if (user.getUserId() == null) {
            user.setUserId(UUID.randomUUID());
        }

        User savedUser = userRepository.save(user);
        return getUserMapper.toDto(savedUser);
    }

    @Override
    @Cacheable(value = "users", key = "#id")
    public GetUserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return getUserMapper.toDto(user);
    }

    @Override
    public GetUserDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
        return getUserMapper.toDto(user);
    }

    @Override
    public Page<GetUserDto> getUserByFirstLettersOfSurname(String letter, Pageable pageable) {
        Page<User> users = userRepository.findBySurnameStartsWith(letter, pageable);
        return users.map(getUserMapper::toDto);
    }

    @Override
    public Page<GetUserDto> getAllUsers(Pageable pageable) {
        Page<User> users = userRepository.findAll(pageable);
        return users.map(getUserMapper::toDto);
    }

    @Override
    @Transactional
    @CachePut(value = "users", key = "#id")
    public GetUserDto updateUser(Long id, CreateUserDto dto) {
        User existUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        createUserMapper.merge(existUser, dto);
        User updateUser = userRepository.save(existUser);
        return getUserMapper.toDto(updateUser);
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#id")
    public void deleteUser(Long id) {
        User existUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    userRepository.delete(existUser);
    }

    @Override
    public Page<GetUserDto> findUsersByFilters(FilterUserDto filter, Pageable pageable) {

        Specification<User> spec = Specification
                .allOf(
                        filter.getName() != null && !filter.getName().isBlank()
                                ? UserSpecification.hasName(filter.getName())
                                : null,
                        filter.getSurname() != null && !filter.getSurname().isBlank()
                                ? UserSpecification.hasSurname(filter.getSurname())
                                : null,
                        filter.getActive() != null
                                ? UserSpecification.isActive(filter.getActive())
                                : null
                );

        return userRepository.findAll(spec, pageable)
                .map(getUserMapper::toDto);
    }


    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#userId")
    public void setUserActiveStatus(Long userId, Boolean active) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }

        userRepository.setUserActiveStatus(userId, active);
    }

    @Override
    public GetUserDto getUserByUserId(UUID userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        return getUserMapper.toDto(user);
    }

}
