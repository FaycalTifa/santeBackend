package com.uab.sante.controller;

import com.uab.sante.dto.DashboardStatsDTO;
import com.uab.sante.dto.response.ConsultationResponseDTO;
import com.uab.sante.dto.response.DossierUABResponseDTO;
import com.uab.sante.dto.response.ValidationResponseDTO;
import com.uab.sante.entities.Consultation;
import com.uab.sante.service.ConsultationService;
import com.uab.sante.service.UABService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/uab")
@RequiredArgsConstructor
@PreAuthorize("hasRole('UAB_ADMIN')")
public class UABController {

    private final UABService uabService;
    private final ConsultationService consultationService;

    /**
     * Tableau de bord
     */
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardStatsDTO> getDashboard() {
        return ResponseEntity.ok(uabService.getDashboardStats());
    }

    /**
     * Liste de tous les dossiers (consultations + prescriptions)
     */
    @GetMapping("/dossiers")
    public ResponseEntity<List<DossierUABResponseDTO>> getAllDossiers(
            @RequestParam(required = false) String statut,
            @RequestParam(required = false) String numeroPolice) {

        List<DossierUABResponseDTO> dossiers = uabService.getAllDossiers(statut, numeroPolice);
        return ResponseEntity.ok(dossiers);
    }

    /**
     * Valider un dossier
     */
    @PutMapping("/dossiers/{id}/valider")
    public ResponseEntity<ValidationResponseDTO> validerDossier(
            @PathVariable Long id,
            @RequestParam(required = false) String commentaire) {

        Consultation consultation = uabService.validerConsultation(id, commentaire);

        return ResponseEntity.ok(ValidationResponseDTO.builder()
                .consultationId(consultation.getId())
                .valide(true)
                .message("Dossier validé avec succès")
                .montantRembourse(uabService.calculerMontantRemboursement(id))
                .build());
    }

    /**
     * Rejeter un dossier
     */
    @PutMapping("/dossiers/{id}/rejeter")
    public ResponseEntity<ValidationResponseDTO> rejeterDossier(
            @PathVariable Long id,
            @RequestParam String motif) {

        Consultation consultation = uabService.rejeterConsultation(id, motif);

        return ResponseEntity.ok(ValidationResponseDTO.builder()
                .consultationId(consultation.getId())
                .valide(false)
                .message("Dossier rejeté")
                .build());
    }

    /**
     * Détail d'un dossier
     */
    @GetMapping("/dossiers/{id}")
    public ResponseEntity<ConsultationResponseDTO> getDossierDetail(@PathVariable Long id) {
        Consultation consultation = consultationService.getById(id);
        return ResponseEntity.ok(consultationService.toDTO(consultation));
    }
}
