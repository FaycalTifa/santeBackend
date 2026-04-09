package com.uab.sante.service;

import com.uab.sante.dto.StructureDashboardDTO;
import com.uab.sante.entities.Consultation;
import com.uab.sante.entities.PrescriptionExamen;
import com.uab.sante.entities.PrescriptionMedicament;
import com.uab.sante.entities.Structure;
import com.uab.sante.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StructureDashboardService {

    private final ConsultationRepository consultationRepository;
    private final PrescriptionMedicamentRepository prescriptionMedicamentRepository;
    private final PrescriptionExamenRepository prescriptionExamenRepository;
    private final StructureRepository structureRepository;

    /**
     * Récupérer les statistiques pour une structure
     */
    public StructureDashboardDTO getDashboardByStructure(Long structureId) {
        Structure structure = structureRepository.findById(structureId)
                .orElseThrow(() -> new RuntimeException("Structure non trouvée"));

        // Récupérer toutes les consultations de cette structure
        List<Consultation> consultations = consultationRepository.findByStructureId(structureId);

        // Statistiques générales
        StructureDashboardDTO.StatsGeneralesDTO statsGenerales = getStatsGenerales(consultations);

        // Évolution mensuelle (derniers 12 mois)
        List<StructureDashboardDTO.EvolutionMensuelleDTO> evolutionMensuelle = getEvolutionMensuelle(consultations);

        // Détail par année et mois
        Map<Integer, StructureDashboardDTO.AnneeDetailDTO> detailParAnnee = getDetailParAnnee(consultations);

        // Dernières activités
        List<StructureDashboardDTO.ActiviteRecenteDTO> dernieresActivites = getDernieresActivites(consultations, structureId);

        return StructureDashboardDTO.builder()
                .structureId(structure.getId())
                .structureNom(structure.getNom())
                .structureType(structure.getType().name())
                .statsGenerales(statsGenerales)
                .evolutionMensuelle(evolutionMensuelle)
                .detailParAnnee(detailParAnnee)
                .dernieresActivites(dernieresActivites)
                .build();
    }

    /**
     * Statistiques générales
     */
    private StructureDashboardDTO.StatsGeneralesDTO getStatsGenerales(List<Consultation> consultations) {
        long total = consultations.size();
        long enAttente = consultations.stream()
                .filter(c -> "COMPLET".equals(c.getStatut()) && !c.getValidationUab())
                .count();
        long valides = consultations.stream()
                .filter(c -> c.getValidationUab() != null && c.getValidationUab())
                .count();
        long rejetes = consultations.stream()
                .filter(c -> "REJETEE".equals(c.getStatut()))
                .count();

        double montantTotal = consultations.stream()
                .mapToDouble(c -> c.getMontantPrisEnCharge() != null ? c.getMontantPrisEnCharge() : 0)
                .sum();
        double montantRembourse = consultations.stream()
                .filter(c -> c.getRemboursementEffectue() != null && c.getRemboursementEffectue())
                .mapToDouble(c -> c.getMontantPrisEnCharge() != null ? c.getMontantPrisEnCharge() : 0)
                .sum();

        long totalPatients = consultations.stream()
                .map(c -> c.getAssure().getId())
                .distinct()
                .count();

        double montantMoyen = total > 0 ? montantTotal / total : 0;

        return StructureDashboardDTO.StatsGeneralesDTO.builder()
                .totalDossiers(total)
                .enAttente(enAttente)
                .valides(valides)
                .rejetes(rejetes)
                .montantTotalPrisEnCharge(montantTotal)
                .montantTotalRembourse(montantRembourse)
                .totalPatients(totalPatients)
                .montantMoyenParDossier(montantMoyen)
                .build();
    }

    /**
     * Évolution mensuelle (derniers 12 mois)
     */
    private List<StructureDashboardDTO.EvolutionMensuelleDTO> getEvolutionMensuelle(List<Consultation> consultations) {
        LocalDate now = LocalDate.now();
        List<StructureDashboardDTO.EvolutionMensuelleDTO> result = new ArrayList<>();

        for (int i = 11; i >= 0; i--) {
            LocalDate date = now.minusMonths(i);
            int annee = date.getYear();
            int mois = date.getMonthValue();

            List<Consultation> consultationsMois = consultations.stream()
                    .filter(c -> c.getDateConsultation().getYear() == annee &&
                            c.getDateConsultation().getMonthValue() == mois)
                    .collect(Collectors.toList());

            long total = consultationsMois.size();
            double montant = consultationsMois.stream()
                    .mapToDouble(c -> c.getMontantPrisEnCharge() != null ? c.getMontantPrisEnCharge() : 0)
                    .sum();

            result.add(StructureDashboardDTO.EvolutionMensuelleDTO.builder()
                    .mois(date.format(DateTimeFormatter.ofPattern("MMM")))
                    .annee(annee)
                    .nombreDossiers(total)
                    .montantTotal(montant)
                    .build());
        }

        return result;
    }

    /**
     * Détail par année et mois
     */
    private Map<Integer, StructureDashboardDTO.AnneeDetailDTO> getDetailParAnnee(List<Consultation> consultations) {
        Map<Integer, List<Consultation>> parAnnee = consultations.stream()
                .collect(Collectors.groupingBy(c -> c.getDateConsultation().getYear()));

        Map<Integer, StructureDashboardDTO.AnneeDetailDTO> result = new LinkedHashMap<>();
        String[] nomsMois = {"Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
                "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"};

        for (Map.Entry<Integer, List<Consultation>> entry : parAnnee.entrySet().stream()
                .sorted(Map.Entry.<Integer, List<Consultation>>comparingByKey().reversed())
                .collect(Collectors.toList())) {

            Integer annee = entry.getKey();
            List<Consultation> consultationsAnnee = entry.getValue();

            Map<Integer, StructureDashboardDTO.MoisDetailDTO> moisMap = new LinkedHashMap<>();

            for (int i = 1; i <= 12; i++) {
                int mois = i;
                List<Consultation> consultationsMois = consultationsAnnee.stream()
                        .filter(c -> c.getDateConsultation().getMonthValue() == mois)
                        .collect(Collectors.toList());

                long total = consultationsMois.size();
                double montant = consultationsMois.stream()
                        .mapToDouble(c -> c.getMontantPrisEnCharge() != null ? c.getMontantPrisEnCharge() : 0)
                        .sum();

                List<StructureDashboardDTO.ConsultationSimpleDTO> consultationsSimple = consultationsMois.stream()
                        .map(c -> StructureDashboardDTO.ConsultationSimpleDTO.builder()
                                .id(c.getId())
                                .numeroFeuille(c.getNumeroFeuille())
                                .patientNom(c.getAssure().getNom())
                                .patientPrenom(c.getAssure().getPrenom())
                                .numeroPolice(c.getAssure().getNumeroPolice())
                                .dateConsultation(c.getDateConsultation())
                                .montant(c.getMontantPrisEnCharge())
                                .statut(c.getStatut())
                                .build())
                        .collect(Collectors.toList());

                moisMap.put(mois, StructureDashboardDTO.MoisDetailDTO.builder()
                        .mois(mois)
                        .nomMois(nomsMois[mois - 1])
                        .totalDossiers(total)
                        .montantTotal(montant)
                        .consultations(consultationsSimple)
                        .build());
            }

            long totalAnnee = consultationsAnnee.size();
            double montantAnnee = consultationsAnnee.stream()
                    .mapToDouble(c -> c.getMontantPrisEnCharge() != null ? c.getMontantPrisEnCharge() : 0)
                    .sum();

            result.put(annee, StructureDashboardDTO.AnneeDetailDTO.builder()
                    .annee(annee)
                    .totalDossiers(totalAnnee)
                    .montantTotal(montantAnnee)
                    .mois(moisMap)
                    .build());
        }

        return result;
    }

    /**
     * Dernières activités
     */
    private List<StructureDashboardDTO.ActiviteRecenteDTO> getDernieresActivites(List<Consultation> consultations, Long structureId) {
        List<StructureDashboardDTO.ActiviteRecenteDTO> activites = new ArrayList<>();

        // Ajouter les consultations
        consultations.stream()
                .sorted((a, b) -> b.getDateConsultation().compareTo(a.getDateConsultation()))
                .limit(10)
                .forEach(c -> {
                    activites.add(StructureDashboardDTO.ActiviteRecenteDTO.builder()
                            .id(c.getId())
                            .type("CONSULTATION")
                            .description("Consultation de " + c.getAssure().getPrenom() + " " + c.getAssure().getNom())
                            .montant(c.getMontantTotalHospitalier())
                            .date(c.getDateConsultation())
                            .statut(c.getStatut())
                            .build());
                });

        // Pour les pharmacies, ajouter les délivrances
        List<PrescriptionMedicament> prescriptions = prescriptionMedicamentRepository
                .findByPharmacieIdAndDelivreTrue(structureId);
        prescriptions.stream()
                .sorted((a, b) -> b.getDateDelivrance().compareTo(a.getDateDelivrance()))
                .limit(10)
                .forEach(p -> {
                    activites.add(StructureDashboardDTO.ActiviteRecenteDTO.builder()
                            .id(p.getId())
                            .type("DELIVRANCE")
                            .description("Délivrance de " + p.getMedicamentNom())
                            .montant(p.getPrixTotal())
                            .date(p.getDateDelivrance())
                            .statut(p.getDelivre() ? "DÉLIVRÉ" : "EN ATTENTE")
                            .build());
                });

        // Pour les laboratoires, ajouter les réalisations
        List<PrescriptionExamen> examens = prescriptionExamenRepository
                .findByLaboratoireIdAndRealiseTrue(structureId);
        examens.stream()
                .sorted((a, b) -> b.getDateRealisation().compareTo(a.getDateRealisation()))
                .limit(10)
                .forEach(e -> {
                    activites.add(StructureDashboardDTO.ActiviteRecenteDTO.builder()
                            .id(e.getId())
                            .type("EXAMEN")
                            .description("Réalisation de " + e.getExamenNom())
                            .montant(e.getPrixTotal())
                            .date(e.getDateRealisation())
                            .statut(e.getRealise() ? "RÉALISÉ" : "EN ATTENTE")
                            .build());
                });

        // Trier par date décroissante
        activites.sort((a, b) -> b.getDate().compareTo(a.getDate()));

        return activites.stream().limit(20).collect(Collectors.toList());
    }
}
