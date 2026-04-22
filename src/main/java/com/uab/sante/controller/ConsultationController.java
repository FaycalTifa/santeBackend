package com.uab.sante.controller;

import com.uab.sante.dto.request.ConsultationCaisseRequestDTO;
import com.uab.sante.dto.request.ConsultationPrescriptionRequestDTO;
import com.uab.sante.dto.response.ConsultationResponseDTO;
import com.uab.sante.dto.response.PrescriptionExamenResponseDTO;
import com.uab.sante.entities.Consultation;
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

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/consultations")
@RequiredArgsConstructor
public class ConsultationController {

    private final ConsultationService consultationService;
    private final UtilisateurService utilisateurService;

    /**
     * Récupérer l'utilisateur courant avec gestion d'erreur améliorée
     */
    private Utilisateur getCurrentUser(UserDetails userDetails) {
        if (userDetails == null) {
            System.err.println("❌ UserDetails is null - Utilisateur non authentifié");
            throw new RuntimeException("Utilisateur non authentifié. Veuillez vous reconnecter.");
        }

        String email = userDetails.getUsername();
        System.out.println("✅ Utilisateur authentifié: " + email);

        return utilisateurService.findByEmailOrThrow(email);
    }

    /**
     * ÉTAPE 1: Création d'une consultation par la caisse
     */
    @PostMapping("/caisse")
    @PreAuthorize("hasRole('CAISSIER_HOPITAL')")
    public ResponseEntity<ConsultationResponseDTO> createByCaisse(
            @Valid @RequestBody ConsultationCaisseRequestDTO request,
            @AuthenticationPrincipal UserDetails userDetails) {

        System.out.println("======== CREATE BY CAISSE ========");
        System.out.println("Police: " + request.getNumeroPolice());
        System.out.println("Taux ID: " + request.getTauxId());

        Utilisateur caissier = getCurrentUser(userDetails);
        System.out.println("Caissier: " + caissier.getEmail());

        if (!caissier.hasRole("CAISSIER_HOPITAL")) {
            throw new RuntimeException("Seul un caissier d'hôpital peut créer une consultation");
        }

        Consultation consultation = consultationService.createByCaisse(request, userDetails);
        return ResponseEntity.ok(consultationService.toDTO(consultation));
    }

    /**
     * Récupérer toutes les consultations (pour UAB)
     */
    @GetMapping("/uab")
    @PreAuthorize("hasRole('UAB_ADMIN')")
    public ResponseEntity<List<ConsultationResponseDTO>> getAllForUAB(
            @RequestParam(required = false) String statut,
            @RequestParam(required = false) String numeroPolice) {

        List<Consultation> consultations = consultationService.getAllForUAB(statut, numeroPolice);
        return ResponseEntity.ok(consultations.stream()
                .map(consultationService::toDTO)
                .collect(Collectors.toList()));
    }

    /**
     * Récupérer une consultation par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ConsultationResponseDTO> getById(@PathVariable Long id) {
        Consultation consultation = consultationService.getById(id);
        return ResponseEntity.ok(consultationService.toDTO(consultation));
    }

    /**
     * Récupérer les consultations par police
     */
    @GetMapping("/police/{numeroPolice}")
    public ResponseEntity<List<ConsultationResponseDTO>> getByPolice(@PathVariable String numeroPolice) {
        List<Consultation> consultations = consultationService.getByNumeroPolice(numeroPolice);
        return ResponseEntity.ok(consultations.stream()
                .map(consultationService::toDTO)
                .collect(Collectors.toList()));
    }

    /**
     * Valider une consultation (UAB)
     */
    @PutMapping("/{id}/valider")
    @PreAuthorize("hasRole('UAB_ADMIN')")
    public ResponseEntity<ConsultationResponseDTO> valider(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        System.out.println("======== VALIDATION CONSULTATION ========");
        System.out.println("ID: " + id);

        Utilisateur uabAdmin = getCurrentUser(userDetails);
        System.out.println("Admin UAB: " + uabAdmin.getEmail());

        Consultation consultation = consultationService.valider(id, userDetails);
        return ResponseEntity.ok(consultationService.toDTO(consultation));
    }

    /**
     * Rejeter une consultation (UAB)
     */
    @PutMapping("/{id}/rejeter")
    @PreAuthorize("hasRole('UAB_ADMIN')")
    public ResponseEntity<ConsultationResponseDTO> rejeter(
            @PathVariable Long id,
            @RequestParam String motif,
            @AuthenticationPrincipal UserDetails userDetails) {

        getCurrentUser(userDetails);
        Consultation consultation = consultationService.rejeter(id, motif, userDetails);
        return ResponseEntity.ok(consultationService.toDTO(consultation));
    }

    /**
     * ÉTAPE 2: Médecin - Ajouter prescriptions
     */
    @PutMapping("/{id}/prescriptions")
    @PreAuthorize("hasRole('MEDECIN')")
    public ResponseEntity<ConsultationResponseDTO> addPrescriptions(
            @PathVariable Long id,
            @Valid @RequestBody ConsultationPrescriptionRequestDTO request,
            @AuthenticationPrincipal UserDetails userDetails) {

        System.out.println("======== ADD PRESCRIPTIONS ========");
        System.out.println("Consultation ID: " + id);

        Utilisateur medecin = getCurrentUser(userDetails);
        System.out.println("Médecin: " + medecin.getEmail());

        Consultation consultation = consultationService.addPrescriptions(id, request, userDetails);
        return ResponseEntity.ok(consultationService.toDTO(consultation));
    }

    // controller/ConsultationController.java - Modifier l'endpoint

    /**
     * Médecin: Récupérer les consultations en attente avec filtres
     */
    // controller/MedecinController.java
    @GetMapping("/medecin/attente")
    @PreAuthorize("hasRole('MEDECIN')")
    public ResponseEntity<List<ConsultationResponseDTO>> getConsultationsEnAttente(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String numPolice,
            @RequestParam(required = false) String codeInte,
            @RequestParam(required = false) String codeRisq,
            @RequestParam(required = false) String codeMemb) {  // ✅ Ajouter codeMemb optionnel

        System.out.println("=== GET CONSULTATIONS EN ATTENTE CONTROLLER ===");
        System.out.println("numPolice: " + numPolice);
        System.out.println("codeInte: " + codeInte);
        System.out.println("codeRisq: " + codeRisq);
        System.out.println("codeMemb: " + codeMemb);

        List<Consultation> consultations = consultationService.getConsultationsEnAttentePrescription(
                userDetails, numPolice, codeInte, codeRisq, codeMemb);

        return ResponseEntity.ok(consultations.stream()
                .map(consultationService::toDTO)
                .collect(Collectors.toList()));
    }

    /**
     * Récupérer les examens réalisés pour une consultation (pour interprétation)
     */
    @GetMapping("/{consultationId}/examens-realises")
    @PreAuthorize("hasRole('MEDECIN')")
    public ResponseEntity<List<PrescriptionExamenResponseDTO>> getExamensRealisesPourInterpretation(
            @PathVariable Long consultationId,
            @AuthenticationPrincipal UserDetails userDetails) {

        System.out.println("=== GET EXAMENS REALISES POUR INTERPRETATION ===");
        System.out.println("Consultation ID: " + consultationId);

        getCurrentUser(userDetails);

        List<PrescriptionExamen> examens = consultationService.getExamensRealisesByConsultation(consultationId);
        return ResponseEntity.ok(examens.stream()
                .map(consultationService::toPrescriptionExamenDTO)
                .collect(Collectors.toList()));
    }

    /**
     * Ajouter une interprétation à un examen
     */
    @PostMapping("/examens/{examenId}/interpretation")
    @PreAuthorize("hasRole('MEDECIN')")
    public ResponseEntity<PrescriptionExamenResponseDTO> ajouterInterpretation(
            @PathVariable Long examenId,
            @RequestBody String interpretation,
            @AuthenticationPrincipal UserDetails userDetails) {

        System.out.println("=== AJOUTER INTERPRETATION ===");
        System.out.println("Examen ID: " + examenId);

        Utilisateur medecin = getCurrentUser(userDetails);
        System.out.println("Médecin: " + medecin.getEmail());

        PrescriptionExamen examen = consultationService.ajouterInterpretation(examenId, interpretation, userDetails);
        return ResponseEntity.ok(consultationService.toPrescriptionExamenDTO(examen));
    }

    /**
     * Médecin: Récupérer les examens par numéro de police
     * (retourne les examens réalisés avec leurs résultats)
     */
    @GetMapping("/medecin/examens/police/{numeroPolice}")
    @PreAuthorize("hasRole('MEDECIN')")
    public ResponseEntity<List<PrescriptionExamenResponseDTO>> getExamensParPolice(
            @PathVariable String numeroPolice,
            @AuthenticationPrincipal UserDetails userDetails) {

        System.out.println("=== GET EXAMENS PAR POLICE ===");
        System.out.println("Police: " + numeroPolice);
        System.out.println("Médecin: " + userDetails.getUsername());

        getCurrentUser(userDetails);

        // Récupérer les examens réalisés
        List<PrescriptionExamen> examens = consultationService.getExamensRealisesByPolice(numeroPolice);

        System.out.println("Nombre d'examens trouvés: " + examens.size());
        examens.forEach(e -> {
            System.out.println("  - Examen ID: " + e.getId() +
                    ", Réalisé: " + e.getRealise() +
                    ", Interprété: " + (e.getInterpretation() != null));
        });

        return ResponseEntity.ok(examens.stream()
                .map(consultationService::toPrescriptionExamenDTO)
                .collect(Collectors.toList()));
    }

    // ConsultationController.java - Ajoutez cet endpoint
    @GetMapping("/laboratoire/police/{numeroPolice}")
    @PreAuthorize("hasRole('MEDECIN')")
    public ResponseEntity<List<PrescriptionExamenResponseDTO>> getExamensParPoliceLaboratoire(
            @PathVariable String numeroPolice,
            @AuthenticationPrincipal UserDetails userDetails) {

        System.out.println("=== GET EXAMENS PAR POLICE (LABORATOIRE) ===");
        System.out.println("Police: " + numeroPolice);

        getCurrentUser(userDetails);

        List<PrescriptionExamen> examens = consultationService.getExamensRealisesByPolice(numeroPolice);

        return ResponseEntity.ok(examens.stream()
                .map(consultationService::toPrescriptionExamenDTO)
                .collect(Collectors.toList()));
    }

    /**
     * Médecin: Récupérer tous les examens réalisés
     */
    @GetMapping("/medecin/examens-realises")
    @PreAuthorize("hasRole('MEDECIN')")
    public ResponseEntity<List<PrescriptionExamenResponseDTO>> getAllExamensRealises(
            @AuthenticationPrincipal UserDetails userDetails) {

        System.out.println("=== GET TOUS LES EXAMENS REALISES ===");
        System.out.println("Médecin: " + userDetails.getUsername());

        getCurrentUser(userDetails);

        List<PrescriptionExamen> examens = consultationService.getAllExamensRealises();

        return ResponseEntity.ok(examens.stream()
                .map(consultationService::toPrescriptionExamenDTO)
                .collect(Collectors.toList()));
    }



    /**
     * Médecin: Récupérer les demandes d'examens en attente de validation UAB
     */
    @GetMapping("/medecin/demandes-attente")
    @PreAuthorize("hasRole('MEDECIN')")
    public ResponseEntity<List<PrescriptionExamenResponseDTO>> getDemandesEnAttente(
            @AuthenticationPrincipal UserDetails userDetails) {

        System.out.println("=== GET DEMANDES EN ATTENTE ===");

        Utilisateur medecin = getCurrentUser(userDetails);
        System.out.println("Médecin: " + medecin.getEmail());

        List<PrescriptionExamen> examens = consultationService.getDemandesExamenEnAttente(medecin.getId());

        return ResponseEntity.ok(examens.stream()
                .map(consultationService::toPrescriptionExamenDTO)
                .collect(Collectors.toList()));
    }

    /**
     * Médecin: Récupérer les demandes par consultation
     */
    @GetMapping("/medecin/demandes/consultation/{consultationId}")
    @PreAuthorize("hasRole('MEDECIN')")
    public ResponseEntity<List<PrescriptionExamenResponseDTO>> getDemandesByConsultation(
            @PathVariable Long consultationId,
            @AuthenticationPrincipal UserDetails userDetails) {

        System.out.println("=== GET DEMANDES BY CONSULTATION ===");
        System.out.println("Consultation ID: " + consultationId);

        getCurrentUser(userDetails);

        List<PrescriptionExamen> examens = consultationService.getDemandesExamenByConsultation(consultationId);

        return ResponseEntity.ok(examens.stream()
                .map(consultationService::toPrescriptionExamenDTO)
                .collect(Collectors.toList()));
    }
}
