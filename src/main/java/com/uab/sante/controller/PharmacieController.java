package com.uab.sante.controller;

import com.uab.sante.dto.request.PharmacieDelivranceRequestDTO;
import com.uab.sante.dto.response.ConsultationResponseDTO;
import com.uab.sante.entities.PrescriptionMedicament;
import com.uab.sante.entities.Utilisateur;
import com.uab.sante.repository.PrescriptionMedicamentRepository;
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
    private final PrescriptionMedicamentRepository prescriptionMedicamentRepository;


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
    public ResponseEntity<List<ConsultationResponseDTO.PrescriptionMedicamentResponseDTO>> getPrescriptionsEnAttente(
            @AuthenticationPrincipal UserDetails userDetails) {

        Utilisateur pharmacien = utilisateurService.findByEmailOrThrow(userDetails.getUsername());
        List<PrescriptionMedicament> prescriptions = pharmacieService.getPrescriptionsEnAttente(pharmacien.getStructure().getId());

        return ResponseEntity.ok(prescriptions.stream()
                .map(pharmacieService::toDTO)
                .collect(Collectors.toList()));
    }

    /**
     * Rechercher des prescriptions par numéro de police
     */
    @GetMapping("/rechercheNonLivre/{numeroPolice}")
    public ResponseEntity<List<ConsultationResponseDTO.PrescriptionMedicamentResponseDTO>> rechercherParPolicedELIVRER(
            @PathVariable String numeroPolice,
            @AuthenticationPrincipal UserDetails userDetails) {

        // Vérifier l'authentification
        utilisateurService.findByEmailOrThrow(userDetails.getUsername());

        List<PrescriptionMedicament> prescriptions = pharmacieService.getPrescriptionsByPolice(numeroPolice);
        return ResponseEntity.ok(prescriptions.stream()
                .map(pharmacieService::toDTO)
                .collect(Collectors.toList()));
    }



    /**
     * Rechercher des prescriptions par numéro de police
     * ✅ Retourne TOUTES les prescriptions (délivrées et non délivrées)
     */
    @GetMapping("/recherche/{numeroPolice}")
    @PreAuthorize("hasAnyRole('PHARMACIEN', 'CAISSIER_PHARMACIE')")
    public ResponseEntity<List<ConsultationResponseDTO.PrescriptionMedicamentResponseDTO>> rechercherParPolice(
            @PathVariable String numeroPolice,
            @AuthenticationPrincipal UserDetails userDetails) {

        System.out.println("=== RECHERCHE PRESCRIPTIONS PAR POLICE ===");
        System.out.println("Police: " + numeroPolice);

        getCurrentUser(userDetails);

        // ✅ Appeler la méthode qui retourne TOUTES les prescriptions
        List<PrescriptionMedicament> prescriptions = pharmacieService.getPrescriptionsByPolice(numeroPolice);

        System.out.println("Nombre de prescriptions trouvées: " + prescriptions.size());
        prescriptions.forEach(p -> {
            System.out.println("  - ID: " + p.getId() + ", Délivré: " + p.getDelivre());
        });

        return ResponseEntity.ok(prescriptions.stream()
                .map(pharmacieService::toDTO)
                .collect(Collectors.toList()));
    }


    /**
     * Délivrer un médicament
     */
    @PostMapping("/delivrer")
    public ResponseEntity<ConsultationResponseDTO.PrescriptionMedicamentResponseDTO> delivrerMedicament(
            @Valid @RequestBody PharmacieDelivranceRequestDTO request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Utilisateur pharmacien = utilisateurService.findByEmailOrThrow(userDetails.getUsername());

        // ✅ Vérifier le rôle avec la nouvelle méthode hasRole()
        if (!pharmacien.hasRole("PHARMACIEN")) {
            throw new RuntimeException("Seul un pharmacien peut délivrer des médicaments. Rôles actuels: " +
                    pharmacien.getRoles().stream().map(r -> r.getCode()).collect(Collectors.joining(", ")));
        }

        PrescriptionMedicament prescription = pharmacieService.delivrerMedicament(request.getPrescriptionId(), request, pharmacien);
        return ResponseEntity.ok(pharmacieService.toDTO(prescription));
    }

    /**
     * Historique des délivrances
     */
    @GetMapping("/historique")
    public ResponseEntity<List<ConsultationResponseDTO.PrescriptionMedicamentResponseDTO>> getHistorique(
            @AuthenticationPrincipal UserDetails userDetails) {

        Utilisateur pharmacien = utilisateurService.findByEmailOrThrow(userDetails.getUsername());
        List<PrescriptionMedicament> prescriptions = pharmacieService.getHistoriqueDelivrances(pharmacien.getStructure().getId());

        return ResponseEntity.ok(prescriptions.stream()
                .map(pharmacieService::toDTO)
                .collect(Collectors.toList()));
    }

    /**
     * Détail d'une prescription
     */
    @GetMapping("/prescriptions/{id}")
    public ResponseEntity<ConsultationResponseDTO.PrescriptionMedicamentResponseDTO> getPrescriptionById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        utilisateurService.findByEmailOrThrow(userDetails.getUsername());
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

        System.out.println("=== GET TOUTES LES PRESCRIPTIONS ===");
        Utilisateur pharmacien = getCurrentUser(userDetails);

        List<PrescriptionMedicament> prescriptions = pharmacieService.getAllPrescriptions(pharmacien.getStructure().getId());

        return ResponseEntity.ok(prescriptions.stream()
                .map(pharmacieService::toDTO)
                .collect(Collectors.toList()));
    }
}
