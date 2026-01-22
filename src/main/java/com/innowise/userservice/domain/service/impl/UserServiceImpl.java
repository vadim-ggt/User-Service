package com.innowise.userservice.domain.service.impl;

import com.innowise.userservice.domain.dao.UserRepository;
import com.innowise.userservice.domain.entity.User;
import com.innowise.userservice.domain.mapper.user.CreateUserMapper;
import com.innowise.userservice.domain.mapper.user.GetUserMapper;
import com.innowise.userservice.domain.service.UserService;
import com.innowise.userservice.domain.specification.UserSpecification;
import com.innowise.userservice.web.dto.user.CreateUserDto;
import com.innowise.userservice.web.dto.user.FilterUserDto;
import com.innowise.userservice.web.dto.user.GetUserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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
        return userRepository.findById(id)
                .map(getUserMapper::toDto)
                .orElse(null);
    }

    @Override
    public GetUserDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElse(null);
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
    public GetUserDto updateUser(Long id, CreateUserDto dto) {
        User existUser = userRepository.findUserById(id);

        createUserMapper.merge(existUser, dto);
        User updateUser = userRepository.save(existUser);
        return getUserMapper.toDto(updateUser);
    }

    @Override
    public void deleteUser(Long id) {
    User existUser = userRepository.findUserById(id);
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
    public void setUserActiveStatus(Long userId, Boolean active) {
        userRepository.setUserActiveStatus(userId, active);
    }

}
