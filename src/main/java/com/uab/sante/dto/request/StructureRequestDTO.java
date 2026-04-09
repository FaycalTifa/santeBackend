package com.uab.sante.dto.request;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class StructureRequestDTO {

    @NotNull(message = "Le type de structure est requis")
    private String type;

    @NotBlank(message = "Le nom est requis")
    private String nom;

    @NotBlank(message = "Le code structure est requis")
    private String codeStructure;

    private String adresse;
    private String telephone;
    private String email;
    private String agrement;
    private String compteBancaire;
}
