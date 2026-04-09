package com.uab.sante.service;

import com.uab.sante.configSecurity.JwtService;
import com.uab.sante.dto.request.LoginRequestDTO;
import com.uab.sante.dto.response.LoginResponseDTO;
import com.uab.sante.entities.Role;
import com.uab.sante.entities.Utilisateur;
import com.uab.sante.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UtilisateurRepository utilisateurRepository;
    private final JwtService jwtService;

    public LoginResponseDTO login(LoginRequestDTO request) {
        System.out.println("=== LOGIN ATTEMPT ===");
        System.out.println("Email: " + request.getEmail());
        System.out.println("Password: " + request.getPassword());

        try {
            // ✅ Authentification
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            System.out.println("✅ Authentication successful");
            System.out.println("Principal: " + authentication.getPrincipal());
            System.out.println("Authorities: " + authentication.getAuthorities());

            // ✅ Récupérer l'utilisateur depuis la base de données
            Utilisateur utilisateur = utilisateurRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé après authentification"));

            // Mettre à jour la dernière connexion
            utilisateur.setDernierAcces(LocalDateTime.now());
            utilisateurRepository.save(utilisateur);

            // Générer le token JWT
            String token = jwtService.generateToken(utilisateur);
            System.out.println("✅ Token généré: " + token.substring(0, 50) + "...");

            Map<String, String> roleLabels = getRoleLabels();

            List<String> roleCodes = utilisateur.getRoles().stream()
                    .map(Role::getCode)
                    .collect(Collectors.toList());

            List<String> roleLabelsList = utilisateur.getRoles().stream()
                    .map(r -> roleLabels.getOrDefault(r.getCode(), r.getNom()))
                    .collect(Collectors.toList());

            System.out.println("Rôles retournés: " + roleCodes);

            return LoginResponseDTO.builder()
                    .token(token)
                    .type("Bearer")
                    .id(utilisateur.getId())
                    .email(utilisateur.getEmail())
                    .nom(utilisateur.getNom())
                    .prenom(utilisateur.getPrenom())
                    .roles(roleCodes)
                    .rolesLabels(roleLabelsList)
                    .structureId(utilisateur.getStructure() != null ? utilisateur.getStructure().getId() : null)
                    .structureNom(utilisateur.getStructure() != null ? utilisateur.getStructure().getNom() : null)
                    .build();

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'authentification: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private Map<String, String> getRoleLabels() {
        Map<String, String> labels = new HashMap<>();
        labels.put("ADMIN_STRUCTURE", "Admin Structure");
        labels.put("CAISSIER_HOPITAL", "Caissier Hôpital");
        labels.put("MEDECIN", "Médecin");
        labels.put("CAISSIER_PHARMACIE", "Caissier Pharmacie");
        labels.put("PHARMACIEN", "Pharmacien");
        labels.put("CAISSIER_LABORATOIRE", "Caissier Laboratoire");
        labels.put("BIOLOGISTE", "Biologiste");
        labels.put("UAB_ADMIN", "Admin UAB");
        return labels;
    }
}
