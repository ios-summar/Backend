package com.summar.summar.mapper;

import com.summar.summar.domain.User;
import com.summar.summar.dto.JoinRequestDto;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2022-10-21T15:56:26+0900",
    comments = "version: 1.3.1.Final, compiler: javac, environment: Java 11.0.13 (Oracle Corporation)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public User toEntity(JoinRequestDto dto) {
        if ( dto == null ) {
            return null;
        }

        User user = new User();

        user.setUserId( dto.getUserId() );
        user.setUserPwd( dto.getUserPwd() );
        user.setUserName( dto.getUserName() );
        user.setUserNickname( dto.getUserNickname() );
        user.setUserHpNo( dto.getUserHpNo() );

        return user;
    }

    @Override
    public JoinRequestDto toDto(User entity) {
        if ( entity == null ) {
            return null;
        }

        JoinRequestDto joinRequestDto = new JoinRequestDto();

        joinRequestDto.setUserId( entity.getUserId() );
        joinRequestDto.setUserPwd( entity.getUserPwd() );
        joinRequestDto.setUserName( entity.getUserName() );
        joinRequestDto.setUserNickname( entity.getUserNickname() );
        joinRequestDto.setUserHpNo( entity.getUserHpNo() );

        return joinRequestDto;
    }

    @Override
    public List<User> toEntity(List<JoinRequestDto> dtoList) {
        if ( dtoList == null ) {
            return null;
        }

        List<User> list = new ArrayList<User>( dtoList.size() );
        for ( JoinRequestDto joinRequestDto : dtoList ) {
            list.add( toEntity( joinRequestDto ) );
        }

        return list;
    }

    @Override
    public List<JoinRequestDto> toDto(List<User> entityList) {
        if ( entityList == null ) {
            return null;
        }

        List<JoinRequestDto> list = new ArrayList<JoinRequestDto>( entityList.size() );
        for ( User user : entityList ) {
            list.add( toDto( user ) );
        }

        return list;
    }

    @Override
    public void updateFromDto(JoinRequestDto dto, User entity) {
        if ( dto == null ) {
            return;
        }

        if ( dto.getUserId() != null ) {
            entity.setUserId( dto.getUserId() );
        }
        if ( dto.getUserPwd() != null ) {
            entity.setUserPwd( dto.getUserPwd() );
        }
        if ( dto.getUserName() != null ) {
            entity.setUserName( dto.getUserName() );
        }
        if ( dto.getUserNickname() != null ) {
            entity.setUserNickname( dto.getUserNickname() );
        }
        if ( dto.getUserHpNo() != null ) {
            entity.setUserHpNo( dto.getUserHpNo() );
        }
    }
}
