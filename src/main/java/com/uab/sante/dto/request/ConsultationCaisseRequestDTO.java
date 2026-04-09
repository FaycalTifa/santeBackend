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

    @NotNull
    private LocalDate dateConsultation;

    @NotNull
    @Positive
    private Double prixConsultation;

    @Positive
    private Double prixActes;

    // ✅ AJOUTEZ CE CHAMP
    @NotNull(message = "Le taux de couverture est requis")
    private Long tauxId;

}
