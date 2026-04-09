package com.uab.sante.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ConsultationResponseDTO {

    private Long id;
    private String numeroFeuille;

    // Patient
    private String numeroPolice;
    private String nomPatient;
    private String prenomPatient;

    // Consultation
    private LocalDate dateConsultation;
    private Double montantTotalHospitalier;
    private Double tauxCouverture;
    private Double montantPrisEnCharge;
    private Double montantTicketModerateur;
    private Double montantPayePatient;

    // Médical
    private String natureMaladie;
    private String diagnostic;
    private String actesMedicaux;
    private String medecinNom;

    // Statut
    private String statut;
    private Boolean validationUab;
    private Boolean prescriptionsValidees;

    private String structureNom;

    // Prescriptions
    private List<PrescriptionMedicamentResponseDTO> prescriptionsMedicaments;
    private List<PrescriptionExamenResponseDTO> prescriptionsExamens;

    @Data
    @Builder
    public static class PrescriptionMedicamentResponseDTO {
        private Long id;
        private String numeroOrdonnance;
        private Long consultationId;
        private String consultationNumeroFeuille;

        // ✅ Assurez-vous que ces champs existent
        private String patientNom;
        private String patientPrenom;
        private String patientPolice;

        private String medicamentNom;
        private String medicamentDosage;
        private String medicamentForme;
        private Integer quantitePrescitee;
        private Integer quantiteDelivree;
        private String instructions;
        private Boolean delivre;
        private Double prixUnitaire;
        private Double prixTotal;
        private Double montantTicketModerateur;
        private Double montantPrisEnCharge;
        private LocalDate dateDelivrance;
        private String pharmacieNom;
        private String pharmacienNom;
        private Double tauxCouverture;
    }

    @Data
    @Builder
    public static class PrescriptionExamenResponseDTO {
        private Long id;
        private String numeroBulletin;
        private String examenNom;
        private String codeActe;
        private Boolean realise;
        private Double prixTotal;
        private Double montantTicketModerateur;
        private List<ResultatExamenDTO> resultats;
        private String interpretation;
    }

    @Data
    @Builder
    public static class ResultatExamenDTO {
        private String parametre;
        private String valeur;
        private String unite;
        private Boolean anormal;
    }
}
