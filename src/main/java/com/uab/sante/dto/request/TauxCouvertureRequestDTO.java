// dto/request/TauxCouvertureRequestDTO.java
package com.uab.sante.dto.request;

import lombok.Data;
import javax.validation.constraints.*;

@Data
public class TauxCouvertureRequestDTO {

    @NotBlank(message = "Le code est requis")
    @Size(max = 50, message = "Le code ne peut pas dépasser 50 caractères")
    private String code;

    @NotBlank(message = "Le libellé est requis")
    @Size(max = 100, message = "Le libellé ne peut pas dépasser 100 caractères")
    private String libelle;

    @NotNull(message = "Le taux est requis")
    @DecimalMin(value = "0.0", message = "Le taux doit être supérieur ou égal à 0")
    @DecimalMax(value = "100.0", message = "Le taux ne peut pas dépasser 100%")
    private Double tauxPourcentage;
}
