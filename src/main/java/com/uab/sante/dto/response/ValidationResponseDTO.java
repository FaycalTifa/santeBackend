package com.uab.sante.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ValidationResponseDTO {

    private Long dossierId;
    private String type;
    private Boolean valide;
    private String message;
    private Double montantRembourse;
    private String motifRejet;
}
