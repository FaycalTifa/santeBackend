package com.uab.sante.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.time.LocalDate;

@Data
public class ConsultationCaisseRequestDTO {

    @NotBlank
    private String numeroPolice;

    @NotBlank
    private String nomPatient;

    @NotBlank
    private String prenomPatient;

    private String telephonePatient;

    private LocalDate dateNaissance;

    // ✅ NOUVEAU: Code membre (pour différencier les bénéficiaires)
    private String codeMemb;  // NULL pour l'assuré principal

    @NotBlank(message = "Le type de consultation est requis")
    private String typeConsultation; // 'GENERALISTE', 'SPECIALISTE', 'PROFESSEUR'  'DENTISTE'

    @NotNull
    private LocalDate dateConsultation;

    @NotNull
    @Positive
    private Double prixConsultation;

    @Positive
    private Double prixActes;

    @NotBlank
    private String codeInte;  // ← AJOUTER

    @NotBlank
    private String codeRisq;  // ← AJOUTER


    // Nouveaux champs pour le plafonnement
    private String codePres;        // C01, C04, C00
    private String libellePres;     // Libellé de la prestation
    private Double montantPlafond;  // VALEPLAF



    // ✅ AJOUTEZ CE CHAMP
    @NotNull(message = "Le taux de couverture est requis")
    private Long tauxId;

}
