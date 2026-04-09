package com.uab.sante.dto.response;

import com.uab.sante.entities.Utilisateur;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class LoginResponseDTO {

    private String token;
    private String type = "Bearer";
    private Long id;
    private String email;
    private String nom;
    private String prenom;
    private List<String> roles;  // ✅ Changé de Utilisateur.Role à List<String>
    private List<String> rolesLabels;  // ✅ Ajouté pour les libellés
    private Long structureId;
    private String structureNom;
}
