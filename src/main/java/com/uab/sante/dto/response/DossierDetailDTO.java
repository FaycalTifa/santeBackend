// dto/response/DossierDetailDTO.java
package com.uab.sante.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class DossierDetailDTO {
    private Long id;
    private String numero;
    private String type;
    private String patientNom;
    private String patientPrenom;
    private String patientPolice;
    private String structureNom;
    private Long structureId;
    private Double montantTotal;
    private Double montantPrisEnCharge;
    private Double montantTicketModerateur;
    private String statut;
    private Boolean validationUab;
    private LocalDate dateCreation;
    private String origine;
    private String codeInte;
    private String codeRisq;
    private String motifRejet;

    // ✅ Champs supplémentaires pour le détail
    private String medecinNom;
    private String natureMaladie;
    private String diagnostic;
    private String actesMedicaux;

    // ✅ Listes des prescriptions
    private List<PrescriptionMedicamentDetailDTO> prescriptionsMedicaments;
    private List<PrescriptionExamenDetailDTO> prescriptionsExamens;

    // Pour les médicaments seuls
    private String medicamentNom;
    private String medicamentDosage;
    private String medicamentForme;
    private Integer quantite;
    private Integer quantiteDelivree;
    private Boolean delivre;
    private String instructions;

    // Pour les examens seuls
    private String examenNom;
    private String examenCode;
    private LocalDate datePaiement;
    private Boolean realise;
    private Boolean paye;

    @Data
    @Builder
    public static class PrescriptionMedicamentDetailDTO {
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
    }

    @Data
    @Builder
    public static class PrescriptionExamenDetailDTO {
        private Long id;
        private String examenNom;
        private String codeActe;
        private String instructions;
        private Boolean realise;
        private Boolean paye;
        private Double prixTotal;
        private Double montantPrisEnCharge;
        private LocalDate datePaiement;
    }
}
