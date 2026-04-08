package com.epam.finaltask.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "refresh_token")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String token;

    private Instant expiryDate;

    @OneToOne
    @JoinTable(name = "refresh_user",
            joinColumns = @JoinColumn(name = "refresh_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private User user;
}
