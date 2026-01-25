package com.innowise.userservice.unit;

import com.innowise.userservice.domain.dao.UserRepository;
import com.innowise.userservice.domain.entity.User;
import com.innowise.userservice.domain.exeption.UserAlreadyExistsException;
import com.innowise.userservice.domain.exeption.UserNotFoundException;
import com.innowise.userservice.domain.mapper.user.CreateUserMapper;
import com.innowise.userservice.domain.mapper.user.GetUserMapper;
import com.innowise.userservice.domain.service.impl.UserServiceImpl;
import com.innowise.userservice.web.dto.user.CreateUserDto;
import com.innowise.userservice.web.dto.user.GetUserDto;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private CreateUserMapper createUserMapper;
    @Mock
    private GetUserMapper getUserMapper;

    @InjectMocks
    private UserServiceImpl userService; //yt gj

    @Test
    void createUser_success() {
        CreateUserDto dto = new CreateUserDto();
        dto.setEmail("test@mail.com");

        User entity = new User();
        User saved = new User();
        saved.setId(1L);

        GetUserDto expected = new GetUserDto();
        expected.setId(1L);

        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());
        when(createUserMapper.toEntity(dto)).thenReturn(entity);
        when(userRepository.save(entity)).thenReturn(saved);
        when(getUserMapper.toDto(saved)).thenReturn(expected);

        GetUserDto result = userService.createUser(dto);

        assertEquals(1L, result.getId());
        verify(userRepository).save(entity);
    }


    @Test
    void createUser_alreadyExists() {
        CreateUserDto dto = new CreateUserDto();
        dto.setEmail("test@mail.com");

        when(userRepository.findByEmail(dto.getEmail()))
                .thenReturn(Optional.of(new User()));

        assertThrows(UserAlreadyExistsException.class,
                () -> userService.createUser(dto));

        verify(userRepository, never()).save(any());
    }

    @Test
    void getUserById_success() {
        User user = new User();
        user.setId(1L);

        GetUserDto dto = new GetUserDto();
        dto.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(getUserMapper.toDto(user)).thenReturn(dto);

        GetUserDto result = userService.getUserById(1L);

        assertEquals(1L, result.getId());
    }

    @Test
    void getUserById_notFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userService.getUserById(1L));
    }

    @Test
    void getUserByEmail_success() {
        User user = new User();
        GetUserDto dto = new GetUserDto();

        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(user));
        when(getUserMapper.toDto(user)).thenReturn(dto);

        assertNotNull(userService.getUserByEmail("a@b.com"));
    }

    @Test
    void getUserByEmail_notFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userService.getUserByEmail("x@y.com"));
    }

    @Test
    void getAllUsers_success() {
        Pageable pageable = PageRequest.of(0, 10);
        User user = new User();

        when(userRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(user)));

        when(getUserMapper.toDto(user)).thenReturn(new GetUserDto());

        Page<GetUserDto> result = userService.getAllUsers(pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void updateUser_success() {
        User user = new User();
        user.setId(1L);

        CreateUserDto dto = new CreateUserDto();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(getUserMapper.toDto(user)).thenReturn(new GetUserDto());

        userService.updateUser(1L, dto);

        verify(createUserMapper).merge(user, dto);
        verify(userRepository).save(user);
    }

    @Test
    void updateUser_notFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userService.updateUser(1L, new CreateUserDto()));
    }

    @Test
    void deleteUser_success() {
        User user = new User();
        user.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deleteUser(1L);

        verify(userRepository).delete(user);
    }

    @Test
    void deleteUser_notFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userService.deleteUser(1L));
    }

    @Test
    void setUserActiveStatus_success() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.setUserActiveStatus(1L, false);

        verify(userRepository).setUserActiveStatus(1L, false);
    }

    @Test
    void setUserActiveStatus_notFound() {
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThrows(UserNotFoundException.class,
                () -> userService.setUserActiveStatus(1L, false));
    }

}
