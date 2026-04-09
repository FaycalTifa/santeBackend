// DTO unifié pour UAB
package com.uab.sante.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class DossierUABResponseDTO {
    private Long id;
    private String numero;
    private String type; // "CONSULTATION", "PRESCRIPTION_MEDICAMENT", "PRESCRIPTION_EXAMEN"
    private String patientNom;
    private String patientPrenom;
    private String patientPolice;
    private String structureNom;
    private Double montantTotal;
    private Double montantPrisEnCharge;
    private Double montantTicketModerateur;
    private String statut;
    private Boolean validationUab;
    private LocalDate dateCreation;
    private Long structureId;  // ✅ Ajouter ce champ
    private String origine; // "HOPITAL", "PHARMACIE", "LABORATOIRE"
}
