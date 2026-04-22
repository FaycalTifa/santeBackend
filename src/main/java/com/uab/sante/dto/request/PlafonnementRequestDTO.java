package com.uab.sante.dto.request;

import lombok.Data;

@Data
public class PlafonnementRequestDTO {
    private String codeInte;
    private String typeConsultation;
    private Double montantPlafond;
    private Double tauxRemboursement;
}
