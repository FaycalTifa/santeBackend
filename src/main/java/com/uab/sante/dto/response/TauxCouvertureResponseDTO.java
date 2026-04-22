package com.uab.sante.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class TauxCouvertureResponseDTO {
    private Long id;
    private String code;
    private String libelle;
    private Double tauxPourcentage;
}
