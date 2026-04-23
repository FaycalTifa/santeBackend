package com.uab.sante.controller;

import com.uab.sante.entities.Utilisateur;
import com.uab.sante.service.StructureDashboardService;
import com.uab.sante.service.UABService;
import com.uab.sante.service.UtilisateurService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    @GetMapping("/dossier/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_STRUCTURE', 'CAISSIER_HOPITAL', 'MEDECIN', 'PHARMACIEN', 'BIOLOGISTE', 'CAISSIER_PHARMACIE', 'CAISSIER_LABORATOIRE', 'UAB_ADMIN')")
    public ResponseEntity<StructureDashboardService.DossierUnifieDTO> getDossierById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        Utilisateur utilisateur = utilisateurService.findByEmailOrThrow(userDetails.getUsername());
        Long structureId = utilisateur.getStructure() != null ? utilisateur.getStructure().getId() : null;

        if (structureId == null) {
            return ResponseEntity.notFound().build();
        }

        StructureDashboardService.DossierUnifieDTO dossier =
                structureDashboardService.getDossierById(structureId, id);

        if (dossier == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(dossier);
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
