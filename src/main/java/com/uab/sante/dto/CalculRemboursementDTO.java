package com.uab.sante.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CalculRemboursementDTO {
    private Double montantTotal;
    private Double montantPlafond;
    private Double montantRembourseUAB;
    private Double montantTicketModerateur;
    private Double montantSurplus;
    private Double montantTotalPatient;
    private String typeConsultation;
    private Double tauxRemboursement;
}
