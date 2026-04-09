package com.uab.sante.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class UtilisateurResponseDTO {
    private Long id;
    private Long structureId;
    private String structureNom;
    private String nom;
    private String prenom;
    private String email;
    private List<String> roles;  // ✅ Liste des codes de rôles
    private List<String> rolesLabels;  // ✅ Liste des libellés de rôles
    private String telephone;
    private Boolean actif;
    private LocalDateTime dernierAcces;
    private LocalDateTime createdAt;
}
