// controller/MedecinController.java
package com.uab.sante.controller;

import com.uab.sante.dto.response.PrescriptionExamenResponseDTO;
import com.uab.sante.entities.PrescriptionExamen;
import com.uab.sante.entities.Utilisateur;
import com.uab.sante.service.ConsultationService;
import com.uab.sante.service.UtilisateurService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/medecin")
@RequiredArgsConstructor
public class MedecinController {

    private final ConsultationService consultationService;
    private final UtilisateurService utilisateurService;

    /**
     * Récupérer TOUS les examens ayant fait l'objet d'une demande de validation UAB
     * (EN_ATTENTE, OUI, NON) - pour suivi par le médecin
     */
    // controller/MedecinController.java
    // controller/MedecinController.java
    @GetMapping("/demandes-validation")
    @PreAuthorize("hasRole('MEDECIN')")
    public ResponseEntity<List<PrescriptionExamenResponseDTO>> getDemandesValidation(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String numPolice,
            @RequestParam(required = false) String codeInte,
            @RequestParam(required = false) String codeRisq,
            @RequestParam(required = false) String codeMemb) {  // ✅ Ajouter codeMemb

        System.out.println("=== GET DEMANDES VALIDATION UAB ===");
        System.out.println("numPolice: " + numPolice);
        System.out.println("codeInte: " + codeInte);
        System.out.println("codeRisq: " + codeRisq);
        System.out.println("codeMemb: " + codeMemb);

        Utilisateur medecin = utilisateurService.findByEmailOrThrow(userDetails.getUsername());

        List<PrescriptionExamen> examens = consultationService.getDemandesValidationWithFilters(
                medecin.getId(), numPolice, codeInte, codeRisq, codeMemb);  // ✅ Passer codeMemb

        return ResponseEntity.ok(examens.stream()
                .map(consultationService::toPrescriptionExamenDTO)
                .collect(Collectors.toList()));
    }
}
