package com.uab.sante.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResultatExamenDTO {

    private String parametre;

    private String valeur;

    private String unite;

    private String valeurNormaleMin;

    private String valeurNormaleMax;
}
