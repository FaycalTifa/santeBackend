package com.uab.sante.dto.request;

import lombok.Data;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class UtilisateurRequestDTO {

    private Long structureId;

    @NotBlank(message = "Le nom est requis")
    private String nom;

    @NotBlank(message = "Le prénom est requis")
    private String prenom;

    @NotBlank(message = "L'email est requis")
    @Email(message = "Email invalide")
    private String email;

    @NotBlank(message = "Le mot de passe est requis")
    private String password;

    @NotNull(message = "Les rôles sont requis")
    private List<String> roles;  // ✅ Liste de codes de rôles

    private String telephone;
}
