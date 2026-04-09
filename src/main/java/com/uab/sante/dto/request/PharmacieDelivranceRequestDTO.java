package com.uab.sante.dto.request;


import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Data
public class PharmacieDelivranceRequestDTO {

    @NotNull(message = "L'ID de la prescription est requis")
    private Long prescriptionId;

    @NotNull(message = "Le prix unitaire est requis")
    @Positive(message = "Le prix doit être positif")
    private Double prixUnitaire;

    @NotNull(message = "La quantité délivrée est requise")
    @Positive(message = "La quantité doit être positive")
    private Integer quantiteDelivree;
}
