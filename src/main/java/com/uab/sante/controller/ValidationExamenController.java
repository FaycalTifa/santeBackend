// controller/ValidationExamenController.java
package com.uab.sante.controller;

import com.uab.sante.dto.response.PrescriptionExamenResponseDTO;
import com.uab.sante.entities.Assure;
import com.uab.sante.entities.Consultation;
import com.uab.sante.entities.PrescriptionExamen;
import com.uab.sante.entities.Utilisateur;
import com.uab.sante.service.ValidationExamenService;
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
@RequestMapping("/api/uab/examens")
@RequiredArgsConstructor
@PreAuthorize("hasRole('UAB_ADMIN')")
public class ValidationExamenController {

    private final ValidationExamenService validationExamenService;
    private final UtilisateurService utilisateurService;

    private Utilisateur getCurrentUser(UserDetails userDetails) {
        if (userDetails == null) {
            throw new RuntimeException("Utilisateur non authentifié");
        }
        return utilisateurService.findByEmailOrThrow(userDetails.getUsername());
    }

    @GetMapping("/attente-validation")
    public ResponseEntity<List<PrescriptionExamenResponseDTO>> getExamensEnAttenteValidation() {
        List<PrescriptionExamen> examens = validationExamenService.getExamensEnAttenteValidation();
        return ResponseEntity.ok(examens.stream()
                .map(this::toDTO)
                .collect(Collectors.toList()));
    }

    @PutMapping("/{id}/valider")
    public ResponseEntity<PrescriptionExamenResponseDTO> validerExamen(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        Utilisateur uabAdmin = getCurrentUser(userDetails);
        PrescriptionExamen examen = validationExamenService.validerExamen(id, uabAdmin);
        return ResponseEntity.ok(toDTO(examen));
    }

    @PutMapping("/{id}/rejeter")
    public ResponseEntity<PrescriptionExamenResponseDTO> rejeterExamen(
            @PathVariable Long id,
            @RequestParam String motif,
            @AuthenticationPrincipal UserDetails userDetails) {

        Utilisateur uabAdmin = getCurrentUser(userDetails);
        PrescriptionExamen examen = validationExamenService.rejeterExamen(id, motif, uabAdmin);
        return ResponseEntity.ok(toDTO(examen));
    }

    @GetMapping("/valides-non-payes")
    public ResponseEntity<List<PrescriptionExamenResponseDTO>> getExamensValidesNonPayes() {
        List<PrescriptionExamen> examens = validationExamenService.getExamensValidesNonPayes();
        return ResponseEntity.ok(examens.stream()
                .map(this::toDTO)
                .collect(Collectors.toList()));
    }

    @GetMapping("/payes-non-realises")
    public ResponseEntity<List<PrescriptionExamenResponseDTO>> getExamensPayesNonRealises() {
        List<PrescriptionExamen> examens = validationExamenService.getExamensPayesNonRealises();
        return ResponseEntity.ok(examens.stream()
                .map(this::toDTO)
                .collect(Collectors.toList()));
    }

    // controller/ValidationExamenController.java - Corriger le toDTO()
    private PrescriptionExamenResponseDTO toDTO(PrescriptionExamen prescription) {
        Consultation consultation = prescription.getConsultation();
        Assure assure = consultation.getAssure();

        return PrescriptionExamenResponseDTO.builder()
                .id(prescription.getId())
                .numeroBulletin(prescription.getNumeroBulletin())
                .consultationId(consultation.getId())
                .consultationNumeroFeuille(consultation.getNumeroFeuille())
                .patientNom(assure.getNom())
                .patientPrenom(assure.getPrenom())
                .patientPolice(assure.getNumeroPolice())
                .examenNom(prescription.getExamenNom())
                .codeActe(prescription.getCodeActe())
                .instructions(prescription.getInstructions())
                .realise(prescription.getRealise())
                .paye(prescription.getPaye())
                .prixTotal(prescription.getPrixTotal())
                .montantTicketModerateur(prescription.getMontantTicketModerateur())
                .montantPrisEnCharge(prescription.getMontantPrisEnCharge())
                .dateRealisation(prescription.getDateRealisation())
                .datePaiement(prescription.getDatePaiement())
                .laboratoireNom(prescription.getLaboratoire() != null ? prescription.getLaboratoire().getNom() : null)
                .biologisteNom(prescription.getBiologiste() != null ?
                        prescription.getBiologiste().getPrenom() + " " + prescription.getBiologiste().getNom() : null)
                .interpretation(prescription.getInterpretation())
                // ✅ AJOUTER CES LIGNES
                .validationUab(prescription.getValidationUab())
                .motifRejet(prescription.getMotifRejet())
                .datePrescription(prescription.getDatePrescription())
                .medecinNom(consultation.getMedecin() != null ?
                        consultation.getMedecin().getPrenom() + " " + consultation.getMedecin().getNom() : null)
                .build();
    }
}
