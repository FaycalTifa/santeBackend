package com.uab.sante.controller;

import com.uab.sante.dto.StructureDashboardDTO;
import com.uab.sante.entities.Utilisateur;
import com.uab.sante.service.StructureDashboardService;
import com.uab.sante.service.UtilisateurService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/structure/dashboard")
@RequiredArgsConstructor
public class StructureDashboardController {

    private final StructureDashboardService structureDashboardService;
    private final UtilisateurService utilisateurService;

    /**
     * Récupérer le dashboard pour la structure de l'utilisateur connecté
     */
    @GetMapping
    public ResponseEntity<StructureDashboardDTO> getDashboard(
            @AuthenticationPrincipal UserDetails userDetails) {

        Utilisateur utilisateur = utilisateurService.findByEmailOrThrow(userDetails.getUsername());

        if (utilisateur.getStructure() == null) {
            throw new RuntimeException("L'utilisateur n'est pas rattaché à une structure");
        }

        StructureDashboardDTO dashboard = structureDashboardService.getDashboardByStructure(utilisateur.getStructure().getId());
        return ResponseEntity.ok(dashboard);
    }
}
