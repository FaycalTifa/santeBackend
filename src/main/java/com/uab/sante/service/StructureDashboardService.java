package com.uab.sante.service;

import com.uab.sante.entities.*;
import com.uab.sante.repository.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StructureDashboardService {

    private final ConsultationRepository consultationRepository;
    private final PrescriptionMedicamentRepository prescriptionMedicamentRepository;
    private final PrescriptionExamenRepository prescriptionExamenRepository;
    private final StructureRepository structureRepository;
    private final UtilisateurRepository utilisateurRepository;

    // DTO pour les prescriptions médicaments
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PrescriptionMedicamentDTO {
        private Long id;
        private String medicamentNom;
        private String medicamentDosage;
        private String medicamentForme;
        private Integer quantitePrescitee;
        private Integer quantiteDelivree;
        private String instructions;
        private Boolean delivre;
        private Double prixTotal;
        private Double montantPrisEnCharge;
        private String codeInte;
        private String codeRisq;
    }

    // DTO pour les prescriptions examens
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PrescriptionExamenDTO {
        private Long id;
        private String examenNom;
        private String codeActe;
        private String instructions;
        private Boolean realise;
        private Boolean paye;
        private Double prixTotal;
        private Double montantPrisEnCharge;
        private LocalDate datePaiement;
        private String codeInte;
        private String codeRisq;
    }

    // DTO pour les dossiers unifiés
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DossierUnifieDTO {
        private Long id;
        private String numero;
        private String type;
        private String patientNom;
        private String patientPrenom;
        private String patientPolice;
        private Double montantTotal;
        private Double montantPrisEnCharge;
        private Double montantTicketModerateur;
        private String statut;
        private Boolean validationUab;
        private LocalDate dateCreation;
        private String codeInte;
        private String codeRisq;
        private String motifRejet;
        private String structureNom;
        private Long structureId;
        private String origine;
        private String medecinNom;
        private String natureMaladie;
        private String diagnostic;
        private String actesMedicaux;
        private List<PrescriptionMedicamentDTO> prescriptionsMedicaments;
        private List<PrescriptionExamenDTO> prescriptionsExamens;
        private String examenNom;
        private String examenCode;
        private LocalDate datePaiement;
        private String instructions;
        private Boolean realise;
        private Boolean paye;
        private String medicamentNom;
        private String medicamentDosage;
        private String medicamentForme;
        private Integer quantite;
        private Integer quantiteDelivree;
        private Boolean delivre;
    }

    public List<DossierUnifieDTO> getAllDossiersByStructure(Long structureId) {
        List<DossierUnifieDTO> allDossiers = new ArrayList<>();

        // 1. CONSULTATIONS
        List<Consultation> consultations = consultationRepository.findByStructureId(structureId);
        System.out.println("=== CHARGEMENT DES CONSULTATIONS ===");
        System.out.println("Nombre de consultations: " + consultations.size());

        for (Consultation c : consultations) {
            System.out.println("Traitement consultation ID: " + c.getId());

            // Récupérer les prescriptions médicaments
            List<PrescriptionMedicamentDTO> medocsDTO = new ArrayList<>();
            if (c.getPrescriptionsMedicaments() != null && !c.getPrescriptionsMedicaments().isEmpty()) {
                System.out.println("  - Nombre de médicaments: " + c.getPrescriptionsMedicaments().size());
                for (PrescriptionMedicament pm : c.getPrescriptionsMedicaments()) {
                    PrescriptionMedicamentDTO medDTO = PrescriptionMedicamentDTO.builder()
                            .id(pm.getId())
                            .medicamentNom(pm.getMedicamentNom())
                            .medicamentDosage(pm.getMedicamentDosage())
                            .medicamentForme(pm.getMedicamentForme())
                            .quantitePrescitee(pm.getQuantitePrescitee())
                            .quantiteDelivree(pm.getQuantiteDelivree())
                            .instructions(pm.getInstructions())
                            .delivre(pm.getDelivre())
                            .prixTotal(pm.getPrixTotal())
                            .montantPrisEnCharge(pm.getMontantPrisEnCharge())
                            .codeInte(c.getCodeInte())
                            .codeRisq(c.getCodeRisq())
                            .build();
                    medocsDTO.add(medDTO);
                    System.out.println("    - Médicament: " + pm.getMedicamentNom());
                }
            } else {
                System.out.println("  - Aucune prescription médicament trouvée");
            }

            // Récupérer les prescriptions examens
            List<PrescriptionExamenDTO> examensDTO = new ArrayList<>();
            if (c.getPrescriptionsExamens() != null && !c.getPrescriptionsExamens().isEmpty()) {
                System.out.println("  - Nombre d'examens: " + c.getPrescriptionsExamens().size());
                for (PrescriptionExamen pe : c.getPrescriptionsExamens()) {
                    PrescriptionExamenDTO examDTO = PrescriptionExamenDTO.builder()
                            .id(pe.getId())
                            .examenNom(pe.getExamenNom())
                            .codeActe(pe.getCodeActe())
                            .instructions(pe.getInstructions())
                            .realise(pe.getRealise())
                            .paye(pe.getPaye())
                            .prixTotal(pe.getPrixTotal())
                            .montantPrisEnCharge(pe.getMontantPrisEnCharge())
                            .datePaiement(pe.getDatePaiement())
                            .codeInte(c.getCodeInte())
                            .codeRisq(c.getCodeRisq())
                            .build();
                    examensDTO.add(examDTO);
                    System.out.println("    - Examen: " + pe.getExamenNom());
                }
            } else {
                System.out.println("  - Aucune prescription examen trouvée");
            }

            DossierUnifieDTO d = DossierUnifieDTO.builder()
                    .id(c.getId())
                    .numero(c.getNumeroFeuille())
                    .type("CONSULTATION")
                    .patientNom(c.getAssure().getNom())
                    .patientPrenom(c.getAssure().getPrenom())
                    .patientPolice(c.getAssure().getNumeroPolice())
                    .montantTotal(c.getMontantTotalHospitalier())
                    .montantPrisEnCharge(c.getMontantPrisEnCharge())
                    .montantTicketModerateur(c.getMontantTicketModerateur())
                    .statut(c.getStatut())
                    .validationUab(c.getValidationUabBool())
                    .dateCreation(c.getDateConsultation())
                    .codeInte(c.getCodeInte())
                    .codeRisq(c.getCodeRisq())
                    .motifRejet(c.getMotifRejet())
                    .structureNom(c.getStructure() != null ? c.getStructure().getNom() : null)
                    .structureId(c.getStructure() != null ? c.getStructure().getId() : null)
                    .origine("HOPITAL")
                    .medecinNom(c.getMedecin() != null ? c.getMedecin().getPrenom() + " " + c.getMedecin().getNom() : null)
                    .natureMaladie(c.getNatureMaladie())
                    .diagnostic(c.getDiagnostic())
                    .actesMedicaux(c.getActesMedicaux())
                    .prescriptionsMedicaments(medocsDTO)
                    .prescriptionsExamens(examensDTO)
                    .build();
            allDossiers.add(d);
        }

        // 2. PRESCRIPTIONS MÉDICAMENTS (PHARMACIE)
        List<PrescriptionMedicament> medicaments = prescriptionMedicamentRepository.findByPharmacieId(structureId);
        System.out.println("=== PRESCRIPTIONS MÉDICAMENTS ===");
        System.out.println("Nombre: " + medicaments.size());

        for (PrescriptionMedicament pm : medicaments) {
            DossierUnifieDTO d = DossierUnifieDTO.builder()
                    .id(pm.getId())
                    .numero(pm.getNumeroOrdonnance())
                    .type("PRESCRIPTION_MEDICAMENT")
                    .patientNom(pm.getConsultation().getAssure().getNom())
                    .patientPrenom(pm.getConsultation().getAssure().getPrenom())
                    .patientPolice(pm.getConsultation().getAssure().getNumeroPolice())
                    .montantTotal(pm.getPrixTotal())
                    .montantPrisEnCharge(pm.getMontantPrisEnCharge())
                    .montantTicketModerateur(pm.getMontantTicketModerateur())
                    .statut(pm.getDelivre() ? "DELIVRE" : "EN_ATTENTE")
                    .validationUab(pm.getValidationUabBool())
                    .dateCreation(pm.getDatePrescription())
                    .codeInte(pm.getConsultation().getCodeInte())
                    .codeRisq(pm.getConsultation().getCodeRisq())
                    .motifRejet(pm.getMotifRejet())
                    .structureNom(pm.getPharmacie() != null ? pm.getPharmacie().getNom() : null)
                    .structureId(pm.getPharmacie() != null ? pm.getPharmacie().getId() : null)
                    .origine("PHARMACIE")
                    .medicamentNom(pm.getMedicamentNom())
                    .medicamentDosage(pm.getMedicamentDosage())
                    .medicamentForme(pm.getMedicamentForme())
                    .quantite(pm.getQuantitePrescitee())
                    .quantiteDelivree(pm.getQuantiteDelivree())
                    .delivre(pm.getDelivre())
                    .instructions(pm.getInstructions())
                    .build();
            allDossiers.add(d);
        }

        // 3. PRESCRIPTIONS EXAMENS (LABORATOIRE)
        List<PrescriptionExamen> examens = prescriptionExamenRepository.findByLaboratoireId(structureId);
        System.out.println("=== PRESCRIPTIONS EXAMENS ===");
        System.out.println("Nombre: " + examens.size());

        for (PrescriptionExamen pe : examens) {
            DossierUnifieDTO d = DossierUnifieDTO.builder()
                    .id(pe.getId())
                    .numero(pe.getNumeroBulletin())
                    .type("PRESCRIPTION_EXAMEN")
                    .patientNom(pe.getConsultation().getAssure().getNom())
                    .patientPrenom(pe.getConsultation().getAssure().getPrenom())
                    .patientPolice(pe.getConsultation().getAssure().getNumeroPolice())
                    .montantTotal(pe.getPrixTotal())
                    .montantPrisEnCharge(pe.getMontantPrisEnCharge())
                    .montantTicketModerateur(pe.getMontantTicketModerateur())
                    .statut(pe.getPaye() ? "PAYE" : "EN_ATTENTE")
                    .validationUab(pe.getValidationUabBool())
                    .dateCreation(pe.getDatePrescription())
                    .codeInte(pe.getConsultation().getCodeInte())
                    .codeRisq(pe.getConsultation().getCodeRisq())
                    .motifRejet(pe.getMotifRejet())
                    .structureNom(pe.getLaboratoire() != null ? pe.getLaboratoire().getNom() : null)
                    .structureId(pe.getLaboratoire() != null ? pe.getLaboratoire().getId() : null)
                    .origine("LABORATOIRE")
                    .examenNom(pe.getExamenNom())
                    .examenCode(pe.getCodeActe())
                    .datePaiement(pe.getDatePaiement())
                    .instructions(pe.getInstructions())
                    .realise(pe.getRealise())
                    .paye(pe.getPaye())
                    .build();
            allDossiers.add(d);
        }

        allDossiers.sort((a, b) -> b.getDateCreation().compareTo(a.getDateCreation()));

        System.out.println("=== TOTAL DOSSIERS: " + allDossiers.size());

        return allDossiers;
    }

    public DossierUnifieDTO getDossierById(Long structureId, Long dossierId) {
        List<DossierUnifieDTO> allDossiers = getAllDossiersByStructure(structureId);
        return allDossiers.stream()
                .filter(d -> d.getId().equals(dossierId))
                .findFirst()
                .orElse(null);
    }


    // StructureDashboardService.java - Ajouter ces méthodes après les autres méthodes

    /**
     * Convertit une Consultation en DossierUnifieDTO
     */
    private DossierUnifieDTO convertConsultationToDTO(Consultation c) {
        System.out.println("=== Conversion Consultation en DTO ===");
        System.out.println("Consultation ID: " + c.getId());

        // Récupérer les prescriptions médicaments
        List<PrescriptionMedicamentDTO> medocsDTO = new ArrayList<>();
        if (c.getPrescriptionsMedicaments() != null && !c.getPrescriptionsMedicaments().isEmpty()) {
            for (PrescriptionMedicament pm : c.getPrescriptionsMedicaments()) {
                PrescriptionMedicamentDTO medDTO = PrescriptionMedicamentDTO.builder()
                        .id(pm.getId())
                        .medicamentNom(pm.getMedicamentNom())
                        .medicamentDosage(pm.getMedicamentDosage())
                        .medicamentForme(pm.getMedicamentForme())
                        .quantitePrescitee(pm.getQuantitePrescitee())
                        .quantiteDelivree(pm.getQuantiteDelivree())
                        .instructions(pm.getInstructions())
                        .delivre(pm.getDelivre())
                        .prixTotal(pm.getPrixTotal())
                        .montantPrisEnCharge(pm.getMontantPrisEnCharge())
                        .codeInte(c.getCodeInte())
                        .codeRisq(c.getCodeRisq())
                        .build();
                medocsDTO.add(medDTO);
            }
        }

        // Récupérer les prescriptions examens
        List<PrescriptionExamenDTO> examensDTO = new ArrayList<>();
        if (c.getPrescriptionsExamens() != null && !c.getPrescriptionsExamens().isEmpty()) {
            for (PrescriptionExamen pe : c.getPrescriptionsExamens()) {
                PrescriptionExamenDTO examDTO = PrescriptionExamenDTO.builder()
                        .id(pe.getId())
                        .examenNom(pe.getExamenNom())
                        .codeActe(pe.getCodeActe())
                        .instructions(pe.getInstructions())
                        .realise(pe.getRealise())
                        .paye(pe.getPaye())
                        .prixTotal(pe.getPrixTotal())
                        .montantPrisEnCharge(pe.getMontantPrisEnCharge())
                        .datePaiement(pe.getDatePaiement())
                        .codeInte(c.getCodeInte())
                        .codeRisq(c.getCodeRisq())
                        .build();
                examensDTO.add(examDTO);
            }
        }

        String medecinNom = null;
        if (c.getMedecin() != null) {
            medecinNom = c.getMedecin().getPrenom() + " " + c.getMedecin().getNom();
        }

        return DossierUnifieDTO.builder()
                .id(c.getId())
                .numero(c.getNumeroFeuille())
                .type("CONSULTATION")
                .patientNom(c.getAssure().getNom())
                .patientPrenom(c.getAssure().getPrenom())
                .patientPolice(c.getAssure().getNumeroPolice())
                .montantTotal(c.getMontantTotalHospitalier())
                .montantPrisEnCharge(c.getMontantPrisEnCharge())
                .montantTicketModerateur(c.getMontantTicketModerateur())
                .statut(c.getStatut())
                .validationUab(c.getValidationUabBool())
                .dateCreation(c.getDateConsultation())
                .codeInte(c.getCodeInte())
                .codeRisq(c.getCodeRisq())
                .motifRejet(c.getMotifRejet())
                .structureNom(c.getStructure() != null ? c.getStructure().getNom() : null)
                .structureId(c.getStructure() != null ? c.getStructure().getId() : null)
                .origine("HOPITAL")
                .medecinNom(medecinNom)
                .natureMaladie(c.getNatureMaladie())
                .diagnostic(c.getDiagnostic())
                .actesMedicaux(c.getActesMedicaux())
                .prescriptionsMedicaments(medocsDTO)
                .prescriptionsExamens(examensDTO)
                .build();
    }

    /**
     * Récupère les détails COMPLETS d'un médicament par son ID
     */
    public DossierUnifieDTO getMedicamentDetail(Long structureId, Long medicamentId) {
        System.out.println("=== StructureDashboardService.getMedicamentDetail() ===");
        System.out.println("Structure ID: " + structureId + ", Médicament ID: " + medicamentId);

        Optional<PrescriptionMedicament> pmOpt = prescriptionMedicamentRepository.findById(medicamentId);
        if (pmOpt.isEmpty()) {
            System.out.println("❌ Médicament non trouvé pour l'ID: " + medicamentId);
            return null;
        }

        PrescriptionMedicament pm = pmOpt.get();

        // Vérifier que le médicament appartient bien à cette structure (pharmacie)
        if (pm.getPharmacie() == null || !pm.getPharmacie().getId().equals(structureId)) {
            System.out.println("❌ Le médicament n'appartient pas à la structure " + structureId);
            return null;
        }

        Consultation c = pm.getConsultation();
        System.out.println("✅ Médicament trouvé: " + pm.getMedicamentNom());

        return DossierUnifieDTO.builder()
                .id(pm.getId())
                .numero(pm.getNumeroOrdonnance())
                .type("PRESCRIPTION_MEDICAMENT")
                .patientNom(c.getAssure().getNom())
                .patientPrenom(c.getAssure().getPrenom())
                .patientPolice(c.getAssure().getNumeroPolice())
                .montantTotal(pm.getPrixTotal())
                .montantPrisEnCharge(pm.getMontantPrisEnCharge())
                .montantTicketModerateur(pm.getMontantTicketModerateur())
                .statut(pm.getDelivre() ? "DELIVRE" : "EN_ATTENTE")
                .validationUab(pm.getValidationUabBool())
                .dateCreation(pm.getDatePrescription())
                .codeInte(c.getCodeInte())
                .codeRisq(c.getCodeRisq())
                .motifRejet(pm.getMotifRejet())
                .structureNom(pm.getPharmacie() != null ? pm.getPharmacie().getNom() : null)
                .structureId(pm.getPharmacie() != null ? pm.getPharmacie().getId() : null)
                .origine("PHARMACIE")
                .medicamentNom(pm.getMedicamentNom())
                .medicamentDosage(pm.getMedicamentDosage())
                .medicamentForme(pm.getMedicamentForme())
                .quantite(pm.getQuantitePrescitee())
                .quantiteDelivree(pm.getQuantiteDelivree())
                .delivre(pm.getDelivre())
                .instructions(pm.getInstructions())
                .medecinNom(c.getMedecin() != null ? c.getMedecin().getPrenom() + " " + c.getMedecin().getNom() : null)
                .build();
    }

    /**
     * Récupère les détails COMPLETS d'un examen par son ID
     */
    public DossierUnifieDTO getExamenDetail(Long structureId, Long examenId) {
        System.out.println("=== StructureDashboardService.getExamenDetail() ===");
        System.out.println("Structure ID: " + structureId + ", Examen ID: " + examenId);

        Optional<PrescriptionExamen> peOpt = prescriptionExamenRepository.findById(examenId);
        if (peOpt.isEmpty()) {
            System.out.println("❌ Examen non trouvé");
            return null;
        }

        PrescriptionExamen pe = peOpt.get();

        // Vérifier que l'examen appartient bien à cette structure (laboratoire)
        if (pe.getLaboratoire() == null || !pe.getLaboratoire().getId().equals(structureId)) {
            System.out.println("❌ L'examen n'appartient pas à cette structure");
            return null;
        }

        Consultation c = pe.getConsultation();

        return DossierUnifieDTO.builder()
                .id(pe.getId())
                .numero(pe.getNumeroBulletin())
                .type("PRESCRIPTION_EXAMEN")
                .patientNom(c.getAssure().getNom())
                .patientPrenom(c.getAssure().getPrenom())
                .patientPolice(c.getAssure().getNumeroPolice())
                .montantTotal(pe.getPrixTotal())
                .montantPrisEnCharge(pe.getMontantPrisEnCharge())
                .montantTicketModerateur(pe.getMontantTicketModerateur())
                .statut(pe.getPaye() ? "PAYE" : "EN_ATTENTE")
                .validationUab(pe.getValidationUabBool())
                .dateCreation(pe.getDatePrescription())
                .codeInte(c.getCodeInte())
                .codeRisq(c.getCodeRisq())
                .motifRejet(pe.getMotifRejet())
                .structureNom(pe.getLaboratoire() != null ? pe.getLaboratoire().getNom() : null)
                .structureId(pe.getLaboratoire() != null ? pe.getLaboratoire().getId() : null)
                .origine("LABORATOIRE")
                .examenNom(pe.getExamenNom())
                .examenCode(pe.getCodeActe())
                .datePaiement(pe.getDatePaiement())
                .instructions(pe.getInstructions())
                .realise(pe.getRealise())
                .paye(pe.getPaye())
                .medecinNom(c.getMedecin() != null ? c.getMedecin().getPrenom() + " " + c.getMedecin().getNom() : null)
                .build();
    }

    /**
     * Récupère les détails COMPLETS d'une consultation par son ID
     */
    public DossierUnifieDTO getConsultationDetail(Long structureId, Long consultationId) {
        System.out.println("=== StructureDashboardService.getConsultationDetail() ===");
        System.out.println("Structure ID: " + structureId + ", Consultation ID: " + consultationId);

        Optional<Consultation> cOpt = consultationRepository.findById(consultationId);
        if (cOpt.isEmpty()) {
            System.out.println("❌ Consultation non trouvée");
            return null;
        }

        Consultation c = cOpt.get();

        // Vérifier que la consultation appartient bien à cette structure (hôpital)
        if (c.getStructure() == null || !c.getStructure().getId().equals(structureId)) {
            System.out.println("❌ La consultation n'appartient pas à cette structure");
            return null;
        }

        return convertConsultationToDTO(c);
    }

    // Modifier la méthode getDossierById pour accepter le type
    public DossierUnifieDTO getDossierById(Long structureId, Long dossierId, String type) {
        System.out.println("=== StructureDashboardService.getDossierById() ===");
        System.out.println("Structure ID: " + structureId + ", Dossier ID: " + dossierId + ", Type: " + type);

        // Si le type est spécifié, utiliser la méthode appropriée
        if (type != null) {
            switch (type) {
                case "PRESCRIPTION_MEDICAMENT":
                    return getMedicamentDetail(structureId, dossierId);
                case "PRESCRIPTION_EXAMEN":
                    return getExamenDetail(structureId, dossierId);
                case "CONSULTATION":
                    return getConsultationDetail(structureId, dossierId);
                default:
                    // Fallback: chercher dans la liste complète
                    List<DossierUnifieDTO> allDossiers = getAllDossiersByStructure(structureId);
                    return allDossiers.stream()
                            .filter(d -> d.getId().equals(dossierId))
                            .findFirst()
                            .orElse(null);
            }
        }

        // Fallback si pas de type
        List<DossierUnifieDTO> allDossiers = getAllDossiersByStructure(structureId);
        return allDossiers.stream()
                .filter(d -> d.getId().equals(dossierId))
                .findFirst()
                .orElse(null);
    }

// StructureDashboardController.java - Ajouter cette méthode

    private Long getCurrentUserStructureId() {
        // Récupérer l'utilisateur connecté depuis le contexte de sécurité
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Utilisateur non authentifié");
        }

        // Récupérer l'utilisateur depuis la base de données
        String email = authentication.getName();
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Récupérer la structure de l'utilisateur
        if (utilisateur.getStructure() == null) {
            throw new RuntimeException("L'utilisateur n'est pas rattaché à une structure");
        }

        Long structureId = utilisateur.getStructure().getId();
        System.out.println("Structure ID de l'utilisateur connecté: " + structureId);

        return structureId;
    }

}
