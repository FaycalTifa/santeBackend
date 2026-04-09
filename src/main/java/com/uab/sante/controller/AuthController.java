package com.uab.sante.controller;


import com.uab.sante.dto.request.LoginRequestDTO;
import com.uab.sante.dto.response.LoginResponseDTO;
import com.uab.sante.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        // Le logout est géré par le frontend en supprimant le token
        return ResponseEntity.ok().build();
    }
}
