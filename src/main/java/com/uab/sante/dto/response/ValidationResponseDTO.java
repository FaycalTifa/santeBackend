package com.uab.sante.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ValidationResponseDTO {

    private Long consultationId;
    private Boolean valide;
    private String message;
    private Double montantRembourse;
}
