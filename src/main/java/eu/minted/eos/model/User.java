package eu.minted.eos.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Username is required")
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.ORDINAL)
    private Role role;

    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email address")
    @Column(nullable = false, unique = true, length = 50)
    private String email;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Product> products = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Order> orders = new HashSet<>();

    // UserDetails methods

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // If a single role is used
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));

        // If multiple roles or permissions were used:
        // return roles.stream()
        //             .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
        //             .collect(Collectors.toList());
    }


    @Override
    public boolean isAccountNonExpired() {
        return true; // Customize this logic based on your requirements
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Customize this logic based on your requirements
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Customize this logic based on your requirements
    }

    @Override
    public boolean isEnabled() {
        return true; // Customize this logic based on your requirements
    }

    // Optionally, you can exclude sensitive fields like password from toString()
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
