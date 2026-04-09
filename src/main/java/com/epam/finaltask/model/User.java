package com.epam.finaltask.model;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Table(name = "users")
public class User implements UserDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String username;

    private String password;

	@Enumerated(EnumType.STRING)
    private Role role;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "users")
    private List<Voucher> vouchers;

    private String phoneNumber;

    private BigDecimal balance;

    @OneToOne(mappedBy = "users")

    private RefreshToken refreshToken;

    private boolean active;

    public void addVoucher(Voucher voucher) {
        vouchers.add(voucher);
    }

    public boolean hasVoucher() {
        return !vouchers.isEmpty();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return role.getAuthorities();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}