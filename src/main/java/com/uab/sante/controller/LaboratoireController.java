package com.uab.sante.controller;

import com.uab.sante.dto.request.LaboratoireRealisationRequestDTO;
import com.uab.sante.dto.request.PaiementLaboratoireRequestDTO;
import com.uab.sante.dto.response.PrescriptionExamenResponseDTO;
import com.uab.sante.entities.PrescriptionExamen;
import com.uab.sante.entities.Utilisateur;
import com.uab.sante.service.LaboratoireService;
import com.uab.sante.service.UtilisateurService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/laboratoire")
@RequiredArgsConstructor
public class LaboratoireController {

    private final LaboratoireService laboratoireService;
    private final UtilisateurService utilisateurService;

    // ==================== MÉTHODES UTILITAIRES ====================

    /**
     * Récupère l'utilisateur courant (avec fallback pour développement)
     */
    private Utilisateur getCurrentUser(UserDetails userDetails) {
        System.out.println("=== GET CURRENT USER ===");

        // Cas 1: UserDetails fourni directement
        if (userDetails != null) {
            String email = userDetails.getUsername();
            System.out.println("Email from UserDetails: " + email);
            return utilisateurService.findByEmailOrThrow(email);
        }

        // Cas 2: Essayer de récupérer depuis SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            String email = authentication.getName();
            System.out.println("Email from SecurityContext: " + email);
            return utilisateurService.findByEmailOrThrow(email);
        }

        // Cas 3: Fallback pour le développement
        System.out.println("⚠️ No authenticated user, using default user for development");
        return utilisateurService.findByEmailOrThrow("caisse@labo.ci");
    }

    // ==================== ENDPOINTS GÉNÉRAUX ====================

    /**
     * Récupérer les examens en attente pour le laboratoire connecté
     */
    @GetMapping("/examens-attente")
    @PreAuthorize("hasAnyRole('CAISSIER_LABORATOIRE', 'BIOLOGISTE')")
    public ResponseEntity<List<PrescriptionExamenResponseDTO>> getExamensEnAttente(
            @AuthenticationPrincipal UserDetails userDetails) {

        System.out.println("=== GET EXAMENS EN ATTENTE ===");
        Utilisateur utilisateur = getCurrentUser(userDetails);
        System.out.println("Utilisateur: " + utilisateur.getEmail());
        System.out.println("Structure ID: " + utilisateur.getStructure().getId());

        List<PrescriptionExamen> examens = laboratoireService.getExamensEnAttente(utilisateur.getStructure().getId());
        return ResponseEntity.ok(examens.stream()
                .map(laboratoireService::toDTO)
                .collect(Collectors.toList()));
    }

    /**
     * Rechercher des examens par numéro de police
     */
    @GetMapping("/recherche/{numeroPolice}")
    @PreAuthorize("hasAnyRole('CAISSIER_LABORATOIRE', 'BIOLOGISTE')")
    public ResponseEntity<List<PrescriptionExamenResponseDTO>> rechercherParPolice(
            @PathVariable String numeroPolice,
            @AuthenticationPrincipal UserDetails userDetails) {

        System.out.println("=== RECHERCHE EXAMENS PAR POLICE ===");
        System.out.println("Police: " + numeroPolice);

        getCurrentUser(userDetails);

        List<PrescriptionExamen> examens = laboratoireService.getExamensByPolice(numeroPolice);
        return ResponseEntity.ok(examens.stream()
                .map(laboratoireService::toDTO)
                .collect(Collectors.toList()));
    }

    

    /**
     * Détail d'une prescription d'examen
     */
    @GetMapping("/examens/{id}")
    @PreAuthorize("hasAnyRole('CAISSIER_LABORATOIRE', 'BIOLOGISTE')")
    public ResponseEntity<PrescriptionExamenResponseDTO> getExamenById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        System.out.println("=== GET EXAMEN BY ID ===");
        System.out.println("ID: " + id);

        getCurrentUser(userDetails);

        PrescriptionExamen examen = laboratoireService.getPrescriptionExamenById(id);
        return ResponseEntity.ok(laboratoireService.toDTO(examen));
    }

    /**
     * Historique des examens réalisés
     */
    @GetMapping("/historique")
    @PreAuthorize("hasAnyRole('CAISSIER_LABORATOIRE', 'BIOLOGISTE')")
    public ResponseEntity<List<PrescriptionExamenResponseDTO>> getHistorique(
            @AuthenticationPrincipal UserDetails userDetails) {

        System.out.println("=== GET HISTORIQUE ===");
        Utilisateur utilisateur = getCurrentUser(userDetails);
        System.out.println("Utilisateur: " + utilisateur.getEmail());

        List<PrescriptionExamen> examens = laboratoireService.getExamensRealises(utilisateur.getStructure().getId());
        return ResponseEntity.ok(examens.stream()
                .map(laboratoireService::toDTO)
                .collect(Collectors.toList()));
    }

    // ==================== ENDPOINTS POUR CAISSIER LABORATOIRE ====================

    /**
     * Récupérer les examens en attente de paiement pour un laboratoire
     */
    @GetMapping("/caissier/examens-attente-paiement")
    @PreAuthorize("hasRole('CAISSIER_LABORATOIRE')")
    public ResponseEntity<List<PrescriptionExamenResponseDTO>> getExamensEnAttentePaiement(
            @AuthenticationPrincipal UserDetails userDetails) {

        System.out.println("=== GET EXAMENS EN ATTENTE PAIEMENT ===");

        Utilisateur caissier = getCurrentUser(userDetails);
        Long laboratoireId = caissier.getStructure().getId();

        List<PrescriptionExamen> examens = laboratoireService.getExamensEnAttentePaiement(laboratoireId);

        return ResponseEntity.ok(examens.stream()
                .map(laboratoireService::toDTO)
                .collect(Collectors.toList()));
    }

    /**
     * Enregistrer le paiement d'un examen (Caisse laboratoire)
     */
    @PostMapping("/caissier/paiement")
    @PreAuthorize("hasRole('CAISSIER_LABORATOIRE')")
    public ResponseEntity<PrescriptionExamenResponseDTO> enregistrerPaiement(
            @Valid @RequestBody PaiementLaboratoireRequestDTO request,
            @AuthenticationPrincipal UserDetails userDetails) {

        System.out.println("=== ENREGISTREMENT PAIEMENT EXAMEN ===");
        System.out.println("Prescription ID: " + request.getPrescriptionId());

        Utilisateur caissier = getCurrentUser(userDetails);

        // ✅ Vérification supplémentaire du rôle
        if (!caissier.hasRole("CAISSIER_LABORATOIRE")) {
            throw new RuntimeException("Seul un caissier de laboratoire peut enregistrer un paiement. Rôle actuel: " +
                    caissier.getRoles().stream().map(r -> r.getCode()).collect(Collectors.joining(", ")));
        }

        PrescriptionExamen examen = laboratoireService.enregistrerPaiement(request, caissier);
        return ResponseEntity.ok(laboratoireService.toDTO(examen));
    }

    /**
     * Récupérer le taux de couverture d'un examen
     */
    @GetMapping("/caissier/taux/{prescriptionId}")
    @PreAuthorize("hasRole('CAISSIER_LABORATOIRE')")
    public ResponseEntity<Double> getTauxCouverture(@PathVariable Long prescriptionId) {
        Double taux = laboratoireService.getTauxCouverture(prescriptionId);
        return ResponseEntity.ok(taux);
    }

    // ==================== ENDPOINTS POUR BIOLOGISTE ====================

    /**
     * Récupérer les examens payés en attente de réalisation
     */
    @GetMapping("/biologiste/examens-payes-attente")
    @PreAuthorize("hasRole('BIOLOGISTE')")
    public ResponseEntity<List<PrescriptionExamenResponseDTO>> getExamensPayesEnAttenteRealisation(
            @AuthenticationPrincipal UserDetails userDetails) {

        System.out.println("=== GET EXAMENS PAYES EN ATTENTE REALISATION ===");

        Utilisateur biologiste = getCurrentUser(userDetails);
        Long laboratoireId = biologiste.getStructure().getId();

        List<PrescriptionExamen> examens = laboratoireService.getExamensPayesEnAttenteRealisation(laboratoireId);

        return ResponseEntity.ok(examens.stream()
                .map(laboratoireService::toDTO)
                .collect(Collectors.toList()));
    }

    /**
     * Réaliser un examen (biologiste)
     */
    @PostMapping("/biologiste/realiser")
    @PreAuthorize("hasRole('BIOLOGISTE')")
    public ResponseEntity<PrescriptionExamenResponseDTO> realiserExamen(
            @Valid @RequestBody LaboratoireRealisationRequestDTO request,
            @AuthenticationPrincipal UserDetails userDetails) {

        System.out.println("=== RÉALISATION EXAMEN ===");
        System.out.println("Prescription ID: " + request.getPrescriptionId());

        Utilisateur biologiste = getCurrentUser(userDetails);

        // ✅ Vérification supplémentaire du rôle
        if (!biologiste.hasRole("BIOLOGISTE")) {
            throw new RuntimeException("Seul un biologiste peut réaliser un examen. Rôle actuel: " +
                    biologiste.getRoles().stream().map(r -> r.getCode()).collect(Collectors.joining(", ")));
        }

        PrescriptionExamen examen = laboratoireService.realiserExamen(request.getPrescriptionId(), request, biologiste);
        return ResponseEntity.ok(laboratoireService.toDTO(examen));
    }

    // controller/LaboratoireController.java - Ajouter cet endpoint

    // controller/LaboratoireController.java
    /**
     * Rechercher des examens par CODEINTE, police, code risque et codeMemb (optionnel)
     */
    @GetMapping("/recherche-complete")
    @PreAuthorize("hasAnyRole('CAISSIER_LABORATOIRE', 'BIOLOGISTE')")
    public ResponseEntity<List<PrescriptionExamenResponseDTO>> rechercherParCriteres(
            @RequestParam(required = false) String numPolice,
            @RequestParam(required = false) String codeInte,
            @RequestParam(required = false) String codeRisq,
            @RequestParam(required = false) String codeMemb,  // ✅ Ajouter codeMemb optionnel
            @AuthenticationPrincipal UserDetails userDetails) {

        System.out.println("=== RECHERCHE EXAMENS PAR CRITÈRES ===");
        System.out.println("numPolice: " + numPolice);
        System.out.println("codeInte: " + codeInte);
        System.out.println("codeRisq: " + codeRisq);
        System.out.println("codeMemb: " + codeMemb);

        getCurrentUser(userDetails);

        List<PrescriptionExamen> examens = laboratoireService.rechercherParCriteres(numPolice, codeInte, codeRisq, codeMemb);

        return ResponseEntity.ok(examens.stream()
                .map(laboratoireService::toDTO)
                .collect(Collectors.toList()));
    }

    // controller/LaboratoireController.java - Ajouter ces endpoints

    @GetMapping("/caissier/examens-valides")
    @PreAuthorize("hasRole('CAISSIER_LABORATOIRE')")
    public ResponseEntity<List<PrescriptionExamenResponseDTO>> getExamensValidesNonPayes(
            @AuthenticationPrincipal UserDetails userDetails) {

        System.out.println("=== GET EXAMENS VALIDES NON PAYES ===");
        Utilisateur caissier = getCurrentUser(userDetails);
        Long laboratoireId = caissier.getStructure().getId();

        List<PrescriptionExamen> examens = laboratoireService.getExamensValidesNonPayes(laboratoireId);
        return ResponseEntity.ok(examens.stream()
                .map(laboratoireService::toDTO)
                .collect(Collectors.toList()));
    }
}
