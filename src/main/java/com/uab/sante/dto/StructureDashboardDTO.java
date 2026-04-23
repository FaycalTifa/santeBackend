package com.uab.sante.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class StructureDashboardDTO {
    // Informations de la structure
    private Long structureId;
    private String structureNom;
    private String structureType;

    // Statistiques générales
    private StatsGeneralesDTO statsGenerales;

    // Évolution mensuelle
    private List<EvolutionMensuelleDTO> evolutionMensuelle;

    // Détail par année et mois
    private Map<Integer, AnneeDetailDTO> detailParAnnee;

    // Dernières activités
    private List<ActiviteRecenteDTO> dernieresActivites;

    @Data
    @Builder
    public static class StatsGeneralesDTO {
        private Long totalDossiers;
        private Long enAttente;
        private Long valides;
        private Long rejetes;
        private Double montantTotalPrisEnCharge;
        private Double montantTotalRembourse;
        private Long totalPatients;
        private Double montantMoyenParDossier;
    }

    @Data
    @Builder
    public static class EvolutionMensuelleDTO {
        private String mois;
        private Integer annee;
        private Long nombreDossiers;
        private Double montantTotal;
    }

    @Data
    @Builder
    public static class AnneeDetailDTO {
        private Integer annee;
        private Long totalDossiers;
        private Double montantTotal;
        private Map<Integer, MoisDetailDTO> mois;
    }

    @Data
    @Builder
    public static class MoisDetailDTO {
        private Integer mois;
        private String nomMois;
        private Long totalDossiers;
        private Double montantTotal;
        private List<ConsultationSimpleDTO> consultations;
    }

    @Data
    @Builder
    public static class ConsultationSimpleDTO {
        private Long id;
        private String numeroFeuille;
        private String patientNom;
        private String patientPrenom;
        private String numeroPolice;
        private LocalDate dateConsultation;
        private Double montant;
        private String statut;
        private Boolean validationUab;
        private String structureNom;
        private Long structureId;
        private String codeInte;
        private String codeRisq;
        private String motifRejet;
        private String type;  // ✅ AJOUTER CE CHAMP
    }

    @Data
    @Builder
    public static class ActiviteRecenteDTO {
        private Long id;
        private String type;
        private String description;
        private Double montant;
        private LocalDate date;
        private String statut;
    }
}
