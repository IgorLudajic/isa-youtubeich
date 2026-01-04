package com.team44.isa_youtubeich.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.sql.Timestamp;

@Entity
@Table(name = "ACTIVATION_MAIL")
public class ActivationEmail implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter @Setter
    private Long id;

    @Column(name = "issued_at")
    @Getter @Setter
    private Timestamp issuedAt;

    @Column(name = "expires_at")
    @Getter @Setter
    private Timestamp expiresAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @Getter @Setter
    private User user;
}
