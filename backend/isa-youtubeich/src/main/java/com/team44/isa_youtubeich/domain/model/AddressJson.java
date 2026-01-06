package com.team44.isa_youtubeich.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AddressJson implements Serializable {
    private String street;
    private String city;
    private String country;
}

