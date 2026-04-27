package com.uab.sante.dto.request;

import com.uab.sante.dto.PrescriptionExamenDTO;
import com.uab.sante.dto.PrescriptionMedicamentDTO;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.time.LocalDate;

@Data
public class ConsultationRequestDTO {
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

    @NotNull  // ← NOUVEAU: le taux sélectionné
    private Long tauxId;

    private Boolean prescriptionsValidees;

    private String typeConsultation; // 'GENERALISTE', 'SPECIALISTE', 'PROFESSEUR' 'DENTISTE'
}
