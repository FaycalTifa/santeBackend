package com.uab.sante.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResultatExamenResponseDTO {
    private Long id;
    private String parametre;
    private String valeur;
    private String unite;
    private String valeurNormaleMin;
    private String valeurNormaleMax;
    private Boolean anormal;
}
