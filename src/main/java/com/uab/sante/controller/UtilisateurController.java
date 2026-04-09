// controller/UtilisateurController.java
package com.uab.sante.controller;

import com.uab.sante.dto.request.UtilisateurRequestDTO;
import com.uab.sante.dto.response.UtilisateurResponseDTO;
import com.uab.sante.entities.Role;  // ✅ Importer la bonne classe Role
import com.uab.sante.entities.Utilisateur;
import com.uab.sante.service.UtilisateurService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/utilisateurs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('UAB_ADMIN')")
public class UtilisateurController {

    private final UtilisateurService utilisateurService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public ResponseEntity<List<UtilisateurResponseDTO>> getAllUtilisateurs() {
        List<Utilisateur> utilisateurs = utilisateurService.getAll();
        return ResponseEntity.ok(utilisateurs.stream()
                .map(this::toDTO)
                .collect(Collectors.toList()));
    }

    @GetMapping("/roles")
    public ResponseEntity<List<Map<String, String>>> getAllRoles() {
        List<Role> roles = utilisateurService.getAllRoles();
        List<Map<String, String>> response = roles.stream()
                .map(role -> {
                    Map<String, String> map = new HashMap<>();
                    map.put("code", role.getCode());
                    map.put("nom", role.getNom());
                    return map;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UtilisateurResponseDTO> getUtilisateurById(@PathVariable Long id) {
        Utilisateur utilisateur = utilisateurService.getById(id);
        return ResponseEntity.ok(toDTO(utilisateur));
    }

    @PostMapping
    public ResponseEntity<UtilisateurResponseDTO> createUtilisateur(@Valid @RequestBody UtilisateurRequestDTO request) {
        Utilisateur utilisateur = Utilisateur.builder()
                .structure(request.getStructureId() != null ? utilisateurService.getStructureById(request.getStructureId()) : null)
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .telephone(request.getTelephone())
                .actif(true)
                .build();

        Utilisateur saved = utilisateurService.create(utilisateur, request.getRoles());
        return ResponseEntity.ok(toDTO(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UtilisateurResponseDTO> updateUtilisateur(
            @PathVariable Long id,
            @Valid @RequestBody UtilisateurRequestDTO request) {

        Utilisateur utilisateur = utilisateurService.getById(id);
        utilisateur.setStructure(request.getStructureId() != null ? utilisateurService.getStructureById(request.getStructureId()) : null);
        utilisateur.setNom(request.getNom());
        utilisateur.setPrenom(request.getPrenom());
        utilisateur.setEmail(request.getEmail());
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            utilisateur.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        utilisateur.setTelephone(request.getTelephone());

        Utilisateur updated = utilisateurService.update(utilisateur, request.getRoles());
        return ResponseEntity.ok(toDTO(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactiverUtilisateur(@PathVariable Long id) {
        utilisateurService.desactiver(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/activer")
    public ResponseEntity<Void> activerUtilisateur(@PathVariable Long id) {
        utilisateurService.activer(id);
        return ResponseEntity.ok().build();
    }

    private UtilisateurResponseDTO toDTO(Utilisateur utilisateur) {
        Map<String, String> roleLabels = getRoleLabels();

        return UtilisateurResponseDTO.builder()
                .id(utilisateur.getId())
                .structureId(utilisateur.getStructure() != null ? utilisateur.getStructure().getId() : null)
                .structureNom(utilisateur.getStructure() != null ? utilisateur.getStructure().getNom() : null)
                .nom(utilisateur.getNom())
                .prenom(utilisateur.getPrenom())
                .email(utilisateur.getEmail())
                .roles(utilisateur.getRoles().stream().map(Role::getCode).collect(Collectors.toList()))
                .rolesLabels(utilisateur.getRoles().stream()
                        .map(r -> roleLabels.getOrDefault(r.getCode(), r.getNom()))
                        .collect(Collectors.toList()))
                .telephone(utilisateur.getTelephone())
                .actif(utilisateur.getActif())
                .dernierAcces(utilisateur.getDernierAcces())
                .createdAt(utilisateur.getCreatedAt())
                .build();
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
