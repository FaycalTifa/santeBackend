package com.uab.sante.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping
    public Map<String, String> test() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        System.out.println("--------------TEST CONTROLLER ---------------------");
        response.put("message", "Le backend fonctionne !");
        return response;
    }

    /**
     * Endpoint de test pour vérifier l'authentification et les rôles
     */
    @GetMapping("/test-auth")
    public ResponseEntity<?> testAuth(@AuthenticationPrincipal UserDetails userDetails) {
        System.out.println("=== TEST AUTH ===");
        System.out.println("UserDetails: " + userDetails);

        if (userDetails == null) {
            System.out.println("❌ UserDetails is null");
            return ResponseEntity.status(401).body(Map.of(
                    "authenticated", false,
                    "message", "Non authentifié"
            ));
        }

        System.out.println("Email: " + userDetails.getUsername());
        System.out.println("Authorities: " + userDetails.getAuthorities());

        return ResponseEntity.ok(Map.of(
                "authenticated", true,
                "email", userDetails.getUsername(),
                "authorities", userDetails.getAuthorities().stream()
                        .map(auth -> auth.getAuthority())
                        .collect(Collectors.toList())
        ));
    }
}
