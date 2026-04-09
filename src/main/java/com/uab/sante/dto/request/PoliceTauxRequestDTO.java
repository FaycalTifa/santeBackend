package com.uab.sante.dto.request;

import lombok.Data;
import javax.validation.constraints.*;
import java.time.LocalDate;

@Data
public class PoliceTauxRequestDTO {

    @NotBlank(message = "Le numéro de police est requis")
    private String numeroPolice;

    @NotNull(message = "Le taux est requis")
    private Long tauxId;

    @NotNull(message = "La date de début est requise")
    private LocalDate dateDebut;

    private LocalDate dateFin;
}
