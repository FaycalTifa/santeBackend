// controller/PharmacieController.java
package com.uab.sante.controller;

import com.uab.sante.dto.request.PharmacieDelivranceRequestDTO;
import com.uab.sante.dto.response.ConsultationResponseDTO;
import com.uab.sante.entities.PrescriptionMedicament;
import com.uab.sante.entities.Utilisateur;
import com.uab.sante.service.PharmacieService;
import com.uab.sante.service.UtilisateurService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pharmacie")
@RequiredArgsConstructor
public class PharmacieController {

    private final PharmacieService pharmacieService;
    private final UtilisateurService utilisateurService;

    private Utilisateur getCurrentUser(UserDetails userDetails) {
        if (userDetails == null) {
            throw new RuntimeException("Utilisateur non authentifié");
        }
        String email = userDetails.getUsername();
        return utilisateurService.findByEmailOrThrow(email);
    }

    /**
     * Récupérer les prescriptions en attente
     */
    @GetMapping("/prescriptions-attente")
    @PreAuthorize("hasAnyRole('PHARMACIEN', 'CAISSIER_PHARMACIE')")
    public ResponseEntity<List<ConsultationResponseDTO.PrescriptionMedicamentResponseDTO>> getPrescriptionsEnAttente(
            @AuthenticationPrincipal UserDetails userDetails) {

        Utilisateur pharmacien = getCurrentUser(userDetails);
        List<PrescriptionMedicament> prescriptions = pharmacieService.getPrescriptionsEnAttente(pharmacien.getStructure().getId());

        return ResponseEntity.ok(prescriptions.stream()
                .map(pharmacieService::toDTO)
                .collect(Collectors.toList()));
    }

    // controller/PharmacieController.java
    /**
     * ✅ ENDPOINT : Rechercher des prescriptions par CODEINTE, police, code risque et codeMemb (optionnel)
     */
    @GetMapping("/recherche-complete")
    @PreAuthorize("hasAnyRole('PHARMACIEN', 'CAISSIER_PHARMACIE')")
    public ResponseEntity<List<ConsultationResponseDTO.PrescriptionMedicamentResponseDTO>> rechercherParCriteres(
            @RequestParam(required = false) String numPolice,
            @RequestParam(required = false) String codeInte,
            @RequestParam(required = false) String codeRisq,
            @RequestParam(required = false) String codeMemb,  // ✅ Ajouter codeMemb optionnel
            @AuthenticationPrincipal UserDetails userDetails) {

        System.out.println("=== RECHERCHE PRESCRIPTIONS PAR CRITÈRES ===");
        System.out.println("numPolice: " + numPolice);
        System.out.println("codeInte: " + codeInte);
        System.out.println("codeRisq: " + codeRisq);
        System.out.println("codeMemb: " + codeMemb);

        getCurrentUser(userDetails);

        List<PrescriptionMedicament> prescriptions = pharmacieService.rechercherParCriteres(numPolice, codeInte, codeRisq, codeMemb);

        return ResponseEntity.ok(prescriptions.stream()
                .map(pharmacieService::toDTO)
                .collect(Collectors.toList()));
    }

    /**
     * Rechercher des prescriptions par numéro de police (ancien endpoint)
     */
    @GetMapping("/recherche/{numeroPolice}")
    @PreAuthorize("hasAnyRole('PHARMACIEN', 'CAISSIER_PHARMACIE')")
    public ResponseEntity<List<ConsultationResponseDTO.PrescriptionMedicamentResponseDTO>> rechercherParPolice(
            @PathVariable String numeroPolice,
            @AuthenticationPrincipal UserDetails userDetails) {

        System.out.println("=== RECHERCHE PRESCRIPTIONS PAR POLICE ===");
        System.out.println("Police: " + numeroPolice);

        getCurrentUser(userDetails);

        List<PrescriptionMedicament> prescriptions = pharmacieService.getPrescriptionsByPolice(numeroPolice);

        return ResponseEntity.ok(prescriptions.stream()
                .map(pharmacieService::toDTO)
                .collect(Collectors.toList()));
    }

    /**
     * Délivrer un médicament
     */
    @PostMapping("/delivrer")
    @PreAuthorize("hasRole('PHARMACIEN')")
    public ResponseEntity<ConsultationResponseDTO.PrescriptionMedicamentResponseDTO> delivrerMedicament(
            @Valid @RequestBody PharmacieDelivranceRequestDTO request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Utilisateur pharmacien = getCurrentUser(userDetails);

        if (!pharmacien.hasRole("PHARMACIEN")) {
            throw new RuntimeException("Seul un pharmacien peut délivrer des médicaments");
        }

        PrescriptionMedicament prescription = pharmacieService.delivrerMedicament(request.getPrescriptionId(), request, pharmacien);
        return ResponseEntity.ok(pharmacieService.toDTO(prescription));
    }

    /**
     * Historique des délivrances
     */
    @GetMapping("/historique")
    @PreAuthorize("hasAnyRole('PHARMACIEN', 'CAISSIER_PHARMACIE')")
    public ResponseEntity<List<ConsultationResponseDTO.PrescriptionMedicamentResponseDTO>> getHistorique(
            @AuthenticationPrincipal UserDetails userDetails) {

        Utilisateur pharmacien = getCurrentUser(userDetails);
        List<PrescriptionMedicament> prescriptions = pharmacieService.getHistoriqueDelivrances(pharmacien.getStructure().getId());

        return ResponseEntity.ok(prescriptions.stream()
                .map(pharmacieService::toDTO)
                .collect(Collectors.toList()));
    }

    /**
     * Détail d'une prescription
     */
    @GetMapping("/prescriptions/{id}")
    @PreAuthorize("hasAnyRole('PHARMACIEN', 'CAISSIER_PHARMACIE')")
    public ResponseEntity<ConsultationResponseDTO.PrescriptionMedicamentResponseDTO> getPrescriptionById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        getCurrentUser(userDetails);
        PrescriptionMedicament prescription = pharmacieService.getPrescriptionById(id);
        return ResponseEntity.ok(pharmacieService.toDTO(prescription));
    }

    /**
     * Récupérer TOUTES les prescriptions (délivrées et non délivrées)
     */
    @GetMapping("/toutes-prescriptions")
    @PreAuthorize("hasAnyRole('PHARMACIEN', 'CAISSIER_PHARMACIE')")
    public ResponseEntity<List<ConsultationResponseDTO.PrescriptionMedicamentResponseDTO>> getAllPrescriptions(
            @AuthenticationPrincipal UserDetails userDetails) {

        Utilisateur pharmacien = getCurrentUser(userDetails);
        List<PrescriptionMedicament> prescriptions = pharmacieService.getAllPrescriptions(pharmacien.getStructure().getId());

        return ResponseEntity.ok(prescriptions.stream()
                .map(pharmacieService::toDTO)
                .collect(Collectors.toList()));
    }
}
