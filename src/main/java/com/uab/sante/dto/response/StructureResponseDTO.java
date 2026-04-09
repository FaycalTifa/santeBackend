package com.uab.sante.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class StructureResponseDTO {
    private Long id;
    private String type;
    private String typeLabel;
    private String nom;
    private String codeStructure;
    private String adresse;
    private String telephone;
    private String email;
    private String agrement;
    private String compteBancaire;
    private Boolean actif;
    private LocalDateTime createdAt;
}
