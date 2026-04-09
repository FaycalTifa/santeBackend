package com.uab.sante.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class PrescriptionExamenDTO {

    private Long examenId;  // Si sélectionné dans la liste

    @NotBlank(message = "Le nom de l'examen est requis")
    private String examenNom;

    private String codeActe;

    private String instructions;
}
