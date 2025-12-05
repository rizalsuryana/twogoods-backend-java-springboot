package com.finpro.twogoods.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.finpro.twogoods.entity.UserRole;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor @NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {
    private String email;
    private UserRole role;
    private String name;
    private String profilePicture;
    private String location;
}
