package com.uab.sante.dto.request;

import com.uab.sante.dto.PrescriptionExamenDTO;
import com.uab.sante.dto.PrescriptionMedicamentDTO;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
public class ConsultationPrescriptionRequestDTO {

    @NotBlank(message = "La nature de la maladie est requise")
    private String natureMaladie;

    private String diagnostic;

    private String actesMedicaux;

    private List<PrescriptionMedicamentDTO> prescriptionsMedicaments;

    private List<PrescriptionExamenDTO> prescriptionsExamens;
}
