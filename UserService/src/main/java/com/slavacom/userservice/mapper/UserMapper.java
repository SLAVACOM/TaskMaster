package com.slavacom.userservice.mapper;

import com.slavacom.userservice.dto.UserDto;
import com.slavacom.userservice.dto.UserInfoDto;
import com.slavacom.userservice.dto.UserListDto;
import com.slavacom.userservice.dto.ProfileDetailDto;
import com.slavacom.userservice.entity.User;
import com.slavacom.userservice.entity.Profile;
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

	@Mapping(source = "lastProfileId", target = "lastProfileId")
	@Mapping(target = "lastOrganizationId", ignore = true)
	UserListDto toUserListDto(User user);

	ProfileDetailDto toProfileDetailDto(Profile profile);
}