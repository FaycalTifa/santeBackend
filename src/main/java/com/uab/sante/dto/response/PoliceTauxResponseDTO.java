package com.uab.sante.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class PoliceTauxResponseDTO {
    private Long id;
    private String numeroPolice;
    private String nomAssure;
    private String prenomAssure;
    private Long tauxId;
    private String tauxCode;
    private String tauxLibelle;
    private Double tauxPourcentage;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private Boolean actif;
}
