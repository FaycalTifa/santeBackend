package com.uab.sante.controller;

import com.uab.sante.entities.Utilisateur;
import com.uab.sante.repository.UtilisateurRepository;
import com.uab.sante.service.StructureDashboardService;
import com.uab.sante.service.StructureService;
import com.uab.sante.service.UABService;
import com.uab.sante.service.UtilisateurService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/structure")
@RequiredArgsConstructor
public class StructureDashboardController {

    private final StructureDashboardService structureDashboardService;
    private final UtilisateurService utilisateurService;
    private final UABService uabService;
    private final StructureService structureService;
    private final UtilisateurRepository utilisateurRepository;

    @GetMapping("/dossiers")
    @PreAuthorize("hasAnyRole('ADMIN_STRUCTURE', 'CAISSIER_HOPITAL', 'MEDECIN', 'PHARMACIEN', 'BIOLOGISTE', 'CAISSIER_PHARMACIE', 'CAISSIER_LABORATOIRE', 'UAB_ADMIN')")
    public ResponseEntity<List<StructureDashboardService.DossierUnifieDTO>> getAllDossiersByStructure(
            @AuthenticationPrincipal UserDetails userDetails) {

        Utilisateur utilisateur = utilisateurService.findByEmailOrThrow(userDetails.getUsername());
        Long structureId = utilisateur.getStructure() != null ? utilisateur.getStructure().getId() : null;

        if (structureId == null) {
            return ResponseEntity.ok(List.of());
        }

        return ResponseEntity.ok(structureDashboardService.getAllDossiersByStructure(structureId));
    }

    // StructureDashboardController.java - Corriger cette méthode
    @GetMapping("/dossier/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_STRUCTURE', 'CAISSIER_HOPITAL', 'MEDECIN', 'PHARMACIEN', 'BIOLOGISTE', 'CAISSIER_PHARMACIE', 'CAISSIER_LABORATOIRE', 'UAB_ADMIN')")
    public ResponseEntity<StructureDashboardService.DossierUnifieDTO> getDossierById(
            @PathVariable Long id,
            @RequestParam(required = false) String type) {

        System.out.println("=== GET DOSSIER DETAIL STRUCTURE ===");
        System.out.println("ID: " + id + ", Type: " + type);

        try {
            // Récupérer l'ID de la structure depuis le contexte de sécurité
            Long structureId = getCurrentUserStructureId();
            System.out.println("Structure ID utilisateur: " + structureId);

            // ✅ CORRECTION: Utiliser structureDashboardService au lieu de structureService
            StructureDashboardService.DossierUnifieDTO dossier = structureDashboardService.getDossierById(structureId, id, type);

            if (dossier == null) {
                System.out.println("❌ Dossier non trouvé");
                return ResponseEntity.notFound().build();
            }

            System.out.println("✅ Dossier trouvé: Type=" + dossier.getType());
            return ResponseEntity.ok(dossier);

        } catch (Exception e) {
            System.out.println("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    // StructureDashboardController.java - Ajouter cette méthode après le constructeur

    private Long getCurrentUserStructureId() {
        System.out.println("=== getCurrentUserStructureId() ===");

        // Récupérer l'authentification depuis le contexte de sécurité
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            System.out.println("❌ Utilisateur non authentifié");
            throw new RuntimeException("Utilisateur non authentifié");
        }

        String email = authentication.getName();
        System.out.println("Email de l'utilisateur connecté: " + email);

        // Chercher l'utilisateur dans la base de données
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé: " + email));

        // Vérifier que l'utilisateur a une structure
        if (utilisateur.getStructure() == null) {
            System.out.println("❌ L'utilisateur n'est pas rattaché à une structure");
            throw new RuntimeException("L'utilisateur n'est pas rattaché à une structure");
        }

        Long structureId = utilisateur.getStructure().getId();
        System.out.println("✅ Structure ID trouvé: " + structureId);

        return structureId;
    }

    @PutMapping("/valider/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_STRUCTURE', 'UAB_ADMIN')")
    public ResponseEntity<?> validerDossier(
            @PathVariable Long id,
            @RequestParam String type,
            @RequestParam(required = false) String commentaire,
            @AuthenticationPrincipal UserDetails userDetails) {

        Object result = uabService.validerDossier(id, type, commentaire);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/rejeter/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_STRUCTURE', 'UAB_ADMIN')")
    public ResponseEntity<?> rejeterDossier(
            @PathVariable Long id,
            @RequestParam String type,
            @RequestParam String motif,
            @AuthenticationPrincipal UserDetails userDetails) {

        Object result = uabService.rejeterDossier(id, type, motif);
        return ResponseEntity.ok(result);
    }
}
