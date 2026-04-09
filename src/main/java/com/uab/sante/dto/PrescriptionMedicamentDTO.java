package com.uab.sante.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Data
public class PrescriptionMedicamentDTO {

    private Long medicamentId;  // Si sélectionné dans la liste

    @NotBlank(message = "Le nom du médicament est requis")
    private String medicamentNom;

    private String dosage;

    private String forme;

    @NotNull(message = "La quantité est requise")
    @Positive(message = "La quantité doit être positive")
    private Integer quantitePrescitee;

    private String instructions;
}
