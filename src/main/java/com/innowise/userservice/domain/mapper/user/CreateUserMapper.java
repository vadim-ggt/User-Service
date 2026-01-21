package com.innowise.userservice.domain.mapper.user;

import com.innowise.userservice.domain.entity.User;
import com.innowise.userservice.domain.mapper.GenericMapper;
import com.innowise.userservice.web.dto.user.CreateUserDto;
import org.mapstruct.Mapper;

@Mapper(config = GenericMapper.class)
public interface CreateUserMapper extends
        GenericMapper<User, CreateUserDto> {
}
