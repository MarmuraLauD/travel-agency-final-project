package com.epam.finaltask.restcontroller;

import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.repository.UserRepository;
import com.epam.finaltask.service.UserService;
import com.epam.finaltask.service.security.RefreshTokenService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
@EnableMethodSecurity
public class UserRestController {

    private final UserService userService;

    @PreAuthorize("hasAuthority('user:create')")
    @PostMapping
    public ResponseEntity<UserDTO> create(@Valid @RequestBody UserDTO userDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.register(userDTO));
    }

    @PreAuthorize("hasAuthority('user:update') or authentication.principal.username == #username")
    @PatchMapping("/{username}")
    public ResponseEntity<UserDTO> updateUser(@Valid @PathVariable("username") String username,
                                              @Valid @RequestBody UserDTO userDTO) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(userService.updateUser(username, userDTO));
    }

    @PreAuthorize("hasAuthority('user:read')")
    @GetMapping("/username/{username}")
    public ResponseEntity<UserDTO> getUserByUsername(@PathVariable("username") @Size(min = 3) String username) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(userService.getUserByUsername(username));
    }

    @PreAuthorize("hasAuthority('user:update')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<UserDTO> changeAccountStatus(@Valid @PathVariable("id") String id, @RequestBody UserDTO userDTO) {
        userDTO.setId(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(userService.changeAccountStatus(userDTO));
    }

    @PreAuthorize("hasAuthority('user:read')")
    @GetMapping("/id/{id}")
    public ResponseEntity<UserDTO> getUserById(@Valid @PathVariable("id") String id) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(userService.getUserById(UUID.fromString(id)));
    }

    @PreAuthorize("hasAuthority('user:delete')")
    @DeleteMapping("/{id}")
    public ResponseEntity<UserDTO> deleteUser(@Valid @PathVariable("id") String id) {
        userService.deleteUserById(UUID.fromString(id));
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
