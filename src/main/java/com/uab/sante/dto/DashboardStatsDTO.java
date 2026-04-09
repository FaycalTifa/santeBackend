package com.uab.sante.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {
    // Statistiques générales
    private Long totalDossiers;
    private Long enAttente;
    private Long valides;
    private Long rejetes;
    private Double montantTotalPrisEnCharge;
    private Double montantTotalRembourse;

    // Statistiques par structure (hôpitaux, pharmacies, laboratoires)
    private List<StructureStatsDTO> structures;

    // Détail par structure avec années et mois
    private Map<String, StructureDetailDTO> detailParStructure;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StructureStatsDTO {
        private Long structureId;
        private String structureNom;
        private String structureType;
        private Long totalDossiers;
        private Long enAttente;
        private Long valides;
        private Long rejetes;
        private Double montantTotal;
        private List<YearStatsDTO> annees;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class YearStatsDTO {
        private Integer annee;
        private Long totalDossiers;
        private Double montantTotal;
        private List<MonthStatsDTO> mois;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthStatsDTO {
        private Integer mois;
        private String nomMois;
        private Long totalDossiers;
        private Double montantTotal;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StructureDetailDTO {
        private String nom;
        private String type;
        private Map<Integer, AnneeDetailDTO> annees;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnneeDetailDTO {
        private Integer annee;
        private Long totalDossiers;
        private Double montantTotal;
        private Map<Integer, MoisDetailDTO> mois;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MoisDetailDTO {
        private Integer mois;
        private String nomMois;
        private Long totalDossiers;
        private Double montantTotal;
        private List<ConsultationSimpleDTO> consultations;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConsultationSimpleDTO {
        private Long id;
        private String numeroFeuille;
        private String patientNom;
        private String patientPrenom;
        private String numeroPolice;
        private LocalDate dateConsultation;
        private Double montant;
        private String statut;
    }
}
