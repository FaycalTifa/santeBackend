package com.uab.sante.dto.request;

import lombok.Data;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Data
public class PaiementLaboratoireRequestDTO {

    @NotNull(message = "L'ID de la prescription est requis")
    private Long prescriptionId;

    @NotNull(message = "Le prix total est requis")
    @Positive(message = "Le prix doit être positif")
    private Double prixTotal;

    private Double montantTicketModerateur;
    private Double montantPrisEnCharge;
    private Double montantPayePatient;

    @NotNull(message = "Le mode de paiement est requis")
    private String modePaiement;

    private String referencePaiement;
}
