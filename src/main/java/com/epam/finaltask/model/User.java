package com.epam.finaltask.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Table(name = "users")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String username;

    private String password;

	@Enumerated(EnumType.STRING)
    private Role role;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
    private List<Voucher> vouchers;

    private String phoneNumber;

    private BigDecimal balance;

    private boolean accountStatus;
    
}