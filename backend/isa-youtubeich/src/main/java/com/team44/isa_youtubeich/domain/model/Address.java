package com.team44.isa_youtubeich.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Table(name = "ADDRESS")
public class Address implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Getter @Setter
    private Long id;

    @Getter @Setter
    private String street;

    @Getter @Setter
    private String city;

    @Getter @Setter
    private String country;

    @OneToOne
    @JoinColumn(name = "id")
    @MapsId
    @Getter @Setter
    private User user;
}
