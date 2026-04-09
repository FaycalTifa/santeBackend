package com.uab.sante.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class PrescriptionExamenResponseDTO {
    private Long id;
    private String numeroBulletin;
    private Long consultationId;
    private String consultationNumeroFeuille;
    private String patientNom;
    private String patientPrenom;
    private String patientPolice;
    private String examenNom;
    private String codeActe;
    private String instructions;
    private Boolean realise;
    private Boolean paye;           // ✅ AJOUTER CE CHAMP
    private Double prixTotal;
    private Double montantTicketModerateur;
    private Double montantPrisEnCharge;
    private LocalDate dateRealisation;
    private LocalDate datePaiement;  // ✅ AJOUTER CE CHAMP (optionnel)
    private String laboratoireNom;
    private String biologisteNom;
    private List<ResultatExamenResponseDTO> resultats;
    // ✅ Ajouter ces champs pour l'interprétation
    private String interpretation;
    private LocalDate dateInterpretation;
    private String medecinInterpretationNom;
    private Double tauxCouverture;
}
