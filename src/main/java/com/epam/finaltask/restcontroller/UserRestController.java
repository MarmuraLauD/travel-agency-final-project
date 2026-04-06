package com.epam.finaltask.restcontroller;

import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
public class UserRestController {

    private final UserService userService;

    @PostMapping("/")
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserDTO userDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.register(userDTO));
    }

    @PatchMapping("/{username}")
    public ResponseEntity<UserDTO> updateUser(@Valid @PathVariable("username") String username,
                                              @Valid @RequestBody UserDTO userDTO) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(userService.updateUser(username, userDTO));
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<UserDTO> getUserByUsername(@PathVariable("username") @Size(min = 3) String username) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(userService.getUserByUsername(username));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<UserDTO> changeAccountStatus(@Valid @PathVariable("id") String id, @RequestBody UserDTO userDTO) {
        userDTO.setId(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(userService.changeAccountStatus(userDTO));
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<UserDTO> getUserById(@Valid @PathVariable("id") String id) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(userService.getUserById(UUID.fromString(id)));
    }

}
