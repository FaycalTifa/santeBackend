// AssureResponseDTO.java
package com.uab.sante.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class AssureResponseDTO {
    private Long id;
    private String numeroPolice;
    private String nom;
    private String prenom;
    private String telephone;
    private String email;
    private String adresse;
    private LocalDate dateNaissance;
    private String statut;
}
