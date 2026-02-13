package com.team44.isa_youtubeich.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserResponseDto {
    private Long id;
    private String username;
    private String email;
    private String name;
    private String surname;
    private String street;
    private String city;
    private String country;
    private List<String> roles;
}

