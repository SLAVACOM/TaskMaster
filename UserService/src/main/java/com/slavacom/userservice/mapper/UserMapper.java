package com.slavacom.user_service.mapper;

import com.slavacom.user_service.dto.UserDto;
import com.slavacom.user_service.dto.UserInfoDto;
import com.slavacom.user_service.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface UserMapper {
	User toEntity(UserDto userDto);

	UserDto toUserDto(User user);

	@Mapping(source = "active", target = "active")
	@Mapping(source = "lastProfileId", target = "profileId")
	UserInfoDto toUserInfoDto(User user);
}