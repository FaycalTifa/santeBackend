package com.uab.sante.dto.request;

import com.uab.sante.dto.ResultatExamenDTO;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.List;

@Data
public class LaboratoireRealisationRequestDTO {

    @NotNull
    private Long prescriptionId;

    @NotNull
    @Positive
    private Double prixTotal;

    private List<ResultatExamenDTO> resultats;
}
