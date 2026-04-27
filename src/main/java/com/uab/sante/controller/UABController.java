// controller/UABController.java
package com.uab.sante.controller;

import com.uab.sante.dto.DashboardStatsDTO;
import com.uab.sante.dto.response.ConsultationResponseDTO;
import com.uab.sante.dto.response.DossierUABResponseDTO;
import com.uab.sante.dto.response.ValidationResponseDTO;
import com.uab.sante.entities.Consultation;
import com.uab.sante.entities.PrescriptionExamen;
import com.uab.sante.entities.PrescriptionMedicament;
import com.uab.sante.service.ConsultationService;
import com.uab.sante.service.UABService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
        System.out.println("=== UABController.getDashboard() ===");
        DashboardStatsDTO stats = uabService.getDashboardStats();
        System.out.println("Total dossiers: " + stats.getTotalDossiers());
        System.out.println("Nombre de structures: " + (stats.getStructures() != null ? stats.getStructures().size() : 0));
        return ResponseEntity.ok(stats);
    }

    /**
     * Liste de tous les dossiers (consultations + prescriptions)
     */
    @GetMapping("/dossiers")
    public ResponseEntity<List<DossierUABResponseDTO>> getAllDossiers(
            @RequestParam(required = false) String statut,
            @RequestParam(required = false) String numeroPolice) {

        System.out.println("=== UABController.getAllDossiers() ===");
        System.out.println("Statut: " + statut);
        System.out.println("Numéro police: " + numeroPolice);

        List<DossierUABResponseDTO> dossiers = uabService.getAllDossiers(statut, numeroPolice);
        System.out.println("Nombre de dossiers: " + dossiers.size());

        return ResponseEntity.ok(dossiers);
    }

    /**
     * ✅ Valider un dossier (consultation, médicament ou examen)
     */
    @PutMapping("/dossiers/{id}/valider")
    public ResponseEntity<ValidationResponseDTO> validerDossier(
            @PathVariable Long id,
            @RequestParam String type,
            @RequestParam(required = false) String commentaire) {

        System.out.println("=== VALIDATION DOSSIER ===");
        System.out.println("ID: " + id);
        System.out.println("Type: " + type);

        Object result = uabService.validerDossier(id, type, commentaire);

        Double montantRembourse = null;
        if (result instanceof Consultation) {
            montantRembourse = uabService.calculerMontantRemboursement(id);
        }

        return ResponseEntity.ok(ValidationResponseDTO.builder()
                .dossierId(id)
                .type(type)
                .valide(true)
                .message("Dossier validé avec succès")
                .montantRembourse(montantRembourse)
                .build());
    }

    /**
     * Valider une consultation (ancienne méthode - gardée pour compatibilité)
     */
    @PutMapping("/dossiers/{id}/valider-consultation")
    public ResponseEntity<ValidationResponseDTO> validerConsultation(
            @PathVariable Long id,
            @RequestParam(required = false) String commentaire) {

        System.out.println("=== VALIDATION CONSULTATION ===");
        System.out.println("ID: " + id);

        Consultation consultation = uabService.validerConsultation(id, commentaire);

        return ResponseEntity.ok(ValidationResponseDTO.builder()
                .dossierId(consultation.getId())
                .type("CONSULTATION")
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

        System.out.println("=== REJET DOSSIER ===");
        System.out.println("ID: " + id);
        System.out.println("Motif: " + motif);

        Consultation consultation = uabService.rejeterConsultation(id, motif);

        return ResponseEntity.ok(ValidationResponseDTO.builder()
                .dossierId(consultation.getId())
                .type("CONSULTATION")
                .valide(false)
                .message("Dossier rejeté")
                .motifRejet(motif)
                .build());
    }

    /**
     * Détail d'un dossier (consultation)
     */
    @GetMapping("/dossiers/{id}")
    public ResponseEntity<ConsultationResponseDTO> getDossierDetailSSSS(@PathVariable Long id) {
        System.out.println("=== DETAIL DOSSIER ===");
        System.out.println("ID: " + id);

        Consultation consultation = consultationService.getById(id);
        return ResponseEntity.ok(consultationService.toDTO(consultation));
    }

    // UABController.java - Ajouter cet endpoint

    /**
     * Récupérer les détails complets d'un dossier (avec prescriptions et médecin)
     */
    // UABController.java
    @GetMapping("/dossier/{id}")
  //  @PreAuthorize("hasRole('UAB_ADMIN', 'ADMIN_STRUCTURE')")
    public ResponseEntity<Map<String, Object>> getDossierDetail(
            @PathVariable Long id,
            @RequestParam(required = false) String type) {

        System.out.println("=== GET DOSSIER DETAIL ===");
        System.out.println("ID: " + id);
        System.out.println("Type reçu: " + type);

        Map<String, Object> dossier;

        // ✅ Utiliser le type pour chercher dans la bonne table
        if (type != null) {
            switch (type) {
                case "PRESCRIPTION_MEDICAMENT":
                    dossier = uabService.getDossierMedicamentDetail(id);
                    break;
                case "PRESCRIPTION_EXAMEN":
                    dossier = uabService.getDossierExamenDetail(id);
                    break;
                case "CONSULTATION":
                    dossier = uabService.getDossierConsultationDetail(id);
                    break;
                default:
                    dossier = uabService.getDossierDetail(id);
            }
        } else {
            dossier = uabService.getDossierDetail(id);
        }

        if (dossier == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dossier);
    }

    /**
     * Détail d'une prescription médicament
     */
    @GetMapping("/prescriptions/medicament/{id}")
    public ResponseEntity<PrescriptionMedicament> getPrescriptionMedicamentDetail(@PathVariable Long id) {
        System.out.println("=== DETAIL PRESCRIPTION MEDICAMENT ===");
        System.out.println("ID: " + id);

        PrescriptionMedicament prescription = uabService.getPrescriptionMedicamentById(id);
        return ResponseEntity.ok(prescription);
    }

    /**
     * Détail d'une prescription examen
     */
    @GetMapping("/prescriptions/examen/{id}")
    public ResponseEntity<PrescriptionExamen> getPrescriptionExamenDetail(@PathVariable Long id) {
        System.out.println("=== DETAIL PRESCRIPTION EXAMEN ===");
        System.out.println("ID: " + id);

        PrescriptionExamen examen = uabService.getPrescriptionExamenById(id);
        return ResponseEntity.ok(examen);
    }


// UABController.java - AJOUTER ce nouvel endpoint (garder l'ancien)

    @GetMapping("/dossiers/paginated")
    public ResponseEntity<Page<DossierUABResponseDTO>> getAllDossiersPaginated(
            @RequestParam(required = false) String statut,
            @RequestParam(required = false) String numeroPolice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        System.out.println("=== UABController.getAllDossiersPaginated() ===");
        System.out.println("Page: " + page + ", Size: " + size);

        Page<DossierUABResponseDTO> dossiers = uabService.getAllDossiersPaginated(statut, numeroPolice, page, size);
        return ResponseEntity.ok(dossiers);
    }


}
