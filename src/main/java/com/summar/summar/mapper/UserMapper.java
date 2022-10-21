package com.summar.summar.mapper;


import com.summar.summar.domain.User;
import com.summar.summar.dto.JoinRequestDto;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link User} and its DTO {@link JoinRequestDto}.
 */
@Mapper(componentModel = "spring", uses ={})
public interface UserMapper extends EntityMapper<JoinRequestDto, User> {
}
