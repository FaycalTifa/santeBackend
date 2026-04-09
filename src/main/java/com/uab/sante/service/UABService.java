package com.uab.sante.service;

import com.uab.sante.dto.DashboardStatsDTO;
import com.uab.sante.dto.response.DossierUABResponseDTO;
import com.uab.sante.entities.*;
import com.uab.sante.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UABService {

    private final ConsultationRepository consultationRepository;
    private final PrescriptionMedicamentRepository prescriptionMedicamentRepository;
    private final PrescriptionExamenRepository prescriptionExamenRepository;
    private final StructureRepository structureRepository;

    /**
     * Récupérer les statistiques du tableau de bord - Version complète avec toutes les structures
     */
    public DashboardStatsDTO getDashboardStats() {
        // 1. Récupérer toutes les consultations
        List<Consultation> consultations = consultationRepository.findAll();

        // 2. Récupérer toutes les prescriptions médicaments (pharmacies)
        List<PrescriptionMedicament> prescriptionsMedicaments = prescriptionMedicamentRepository.findAll();

        // 3. Récupérer toutes les prescriptions examens (laboratoires)
        List<PrescriptionExamen> prescriptionsExamens = prescriptionExamenRepository.findAll();

        // 4. Récupérer toutes les structures (hôpitaux, pharmacies, laboratoires)
        List<Structure> allStructures = structureRepository.findAll();

        // Statistiques générales (tous types confondus)
        long totalDossiers = consultations.size() + prescriptionsMedicaments.size() + prescriptionsExamens.size();

        long enAttente = getEnAttenteCount(consultations, prescriptionsMedicaments, prescriptionsExamens);
        long valides = getValidesCount(consultations, prescriptionsMedicaments, prescriptionsExamens);
        long rejetes = getRejetesCount(consultations);

        double montantTotalPrisEnCharge = getMontantTotalPrisEnCharge(consultations, prescriptionsMedicaments, prescriptionsExamens);
        double montantTotalRembourse = getMontantTotalRembourse(consultations);

        // Statistiques par structure (incluant hôpitaux, pharmacies, laboratoires)
        List<DashboardStatsDTO.StructureStatsDTO> structuresStats = getStructuresStats(consultations, prescriptionsMedicaments, prescriptionsExamens, allStructures);

        return DashboardStatsDTO.builder()
                .totalDossiers(totalDossiers)
                .enAttente(enAttente)
                .valides(valides)
                .rejetes(rejetes)
                .montantTotalPrisEnCharge(montantTotalPrisEnCharge)
                .montantTotalRembourse(montantTotalRembourse)
                .structures(structuresStats)
                .build();
    }

    private long getEnAttenteCount(List<Consultation> consultations, List<PrescriptionMedicament> prescriptionsMedicaments, List<PrescriptionExamen> prescriptionsExamens) {
        // Consultations en attente
        long consultationsEnAttente = consultations.stream()
                .filter(c -> "COMPLET".equals(c.getStatut()) && !Boolean.TRUE.equals(c.getValidationUab()))
                .count();

        // Prescriptions médicaments en attente
        long medicamentsEnAttente = prescriptionsMedicaments.stream()
                .filter(pm -> !Boolean.TRUE.equals(pm.getDelivre()))
                .count();

        // Prescriptions examens en attente
        long examensEnAttente = prescriptionsExamens.stream()
                .filter(pe -> !Boolean.TRUE.equals(pe.getPaye()) && !Boolean.TRUE.equals(pe.getRealise()))
                .count();

        return consultationsEnAttente + medicamentsEnAttente + examensEnAttente;
    }

    private long getValidesCount(List<Consultation> consultations, List<PrescriptionMedicament> prescriptionsMedicaments, List<PrescriptionExamen> prescriptionsExamens) {
        // Consultations validées
        long consultationsValidees = consultations.stream()
                .filter(c -> Boolean.TRUE.equals(c.getValidationUab()))
                .count();

        // Prescriptions médicaments délivrées
        long medicamentsDelivres = prescriptionsMedicaments.stream()
                .filter(pm -> Boolean.TRUE.equals(pm.getDelivre()))
                .count();

        // Prescriptions examens réalisés
        long examensRealises = prescriptionsExamens.stream()
                .filter(pe -> Boolean.TRUE.equals(pe.getRealise()))
                .count();

        return consultationsValidees + medicamentsDelivres + examensRealises;
    }

    private long getRejetesCount(List<Consultation> consultations) {
        return consultations.stream()
                .filter(c -> "REJETEE".equals(c.getStatut()))
                .count();
    }

    private double getMontantTotalPrisEnCharge(List<Consultation> consultations, List<PrescriptionMedicament> prescriptionsMedicaments, List<PrescriptionExamen> prescriptionsExamens) {
        double montantConsultations = consultations.stream()
                .mapToDouble(c -> c.getMontantPrisEnCharge() != null ? c.getMontantPrisEnCharge() : 0)
                .sum();

        double montantMedicaments = prescriptionsMedicaments.stream()
                .filter(pm -> Boolean.TRUE.equals(pm.getDelivre()))
                .mapToDouble(pm -> pm.getMontantPrisEnCharge() != null ? pm.getMontantPrisEnCharge() : 0)
                .sum();

        double montantExamens = prescriptionsExamens.stream()
                .filter(pe -> Boolean.TRUE.equals(pe.getRealise()))
                .mapToDouble(pe -> pe.getMontantPrisEnCharge() != null ? pe.getMontantPrisEnCharge() : 0)
                .sum();

        return montantConsultations + montantMedicaments + montantExamens;
    }

    private double getMontantTotalRembourse(List<Consultation> consultations) {
        return consultations.stream()
                .filter(c -> Boolean.TRUE.equals(c.getRemboursementEffectue()))
                .mapToDouble(c -> c.getMontantPrisEnCharge() != null ? c.getMontantPrisEnCharge() : 0)
                .sum();
    }

    /**
     * Récupérer les statistiques par structure (incluant hôpitaux, pharmacies, laboratoires)
     */
    private List<DashboardStatsDTO.StructureStatsDTO> getStructuresStats(
            List<Consultation> consultations,
            List<PrescriptionMedicament> prescriptionsMedicaments,
            List<PrescriptionExamen> prescriptionsExamens,
            List<Structure> allStructures) {

        // Utiliser des Maps pour accumuler les statistiques
        Map<Long, Long> totalDossiersMap = new HashMap<>();
        Map<Long, Double> montantTotalMap = new HashMap<>();
        Map<Long, Long> enAttenteMap = new HashMap<>();
        Map<Long, Long> validesMap = new HashMap<>();
        Map<Long, Long> rejetesMap = new HashMap<>();
        Map<Long, String> structureNomMap = new HashMap<>();
        Map<Long, String> structureTypeMap = new HashMap<>();

        // Initialiser toutes les structures
        for (Structure structure : allStructures) {
            totalDossiersMap.put(structure.getId(), 0L);
            montantTotalMap.put(structure.getId(), 0.0);
            enAttenteMap.put(structure.getId(), 0L);
            validesMap.put(structure.getId(), 0L);
            rejetesMap.put(structure.getId(), 0L);
            structureNomMap.put(structure.getId(), structure.getNom());
            structureTypeMap.put(structure.getId(), structure.getType() != null ? structure.getType().name() : "AUTRE");
        }

        // Ajouter les statistiques des consultations (hôpitaux)
        for (Consultation c : consultations) {
            if (c.getStructure() != null) {
                Long structureId = c.getStructure().getId();
                totalDossiersMap.merge(structureId, 1L, Long::sum);
                montantTotalMap.merge(structureId, c.getMontantPrisEnCharge() != null ? c.getMontantPrisEnCharge() : 0, Double::sum);

                if ("COMPLET".equals(c.getStatut()) && !Boolean.TRUE.equals(c.getValidationUab())) {
                    enAttenteMap.merge(structureId, 1L, Long::sum);
                }
                if (Boolean.TRUE.equals(c.getValidationUab())) {
                    validesMap.merge(structureId, 1L, Long::sum);
                }
                if ("REJETEE".equals(c.getStatut())) {
                    rejetesMap.merge(structureId, 1L, Long::sum);
                }
            }
        }

        // Ajouter les statistiques des prescriptions médicaments (pharmacies)
        for (PrescriptionMedicament pm : prescriptionsMedicaments) {
            if (pm.getPharmacie() != null) {
                Long structureId = pm.getPharmacie().getId();
                totalDossiersMap.merge(structureId, 1L, Long::sum);
                montantTotalMap.merge(structureId, pm.getMontantPrisEnCharge() != null ? pm.getMontantPrisEnCharge() : 0, Double::sum);

                if (!Boolean.TRUE.equals(pm.getDelivre())) {
                    enAttenteMap.merge(structureId, 1L, Long::sum);
                }
                if (Boolean.TRUE.equals(pm.getDelivre())) {
                    validesMap.merge(structureId, 1L, Long::sum);
                }
            }
        }

        // Ajouter les statistiques des prescriptions examens (laboratoires)
        for (PrescriptionExamen pe : prescriptionsExamens) {
            if (pe.getLaboratoire() != null) {
                Long structureId = pe.getLaboratoire().getId();
                totalDossiersMap.merge(structureId, 1L, Long::sum);
                montantTotalMap.merge(structureId, pe.getMontantPrisEnCharge() != null ? pe.getMontantPrisEnCharge() : 0, Double::sum);

                if (!Boolean.TRUE.equals(pe.getRealise())) {
                    enAttenteMap.merge(structureId, 1L, Long::sum);
                }
                if (Boolean.TRUE.equals(pe.getRealise())) {
                    validesMap.merge(structureId, 1L, Long::sum);
                }
            }
        }

        // Construire la liste des statistiques par structure
        List<DashboardStatsDTO.StructureStatsDTO> result = new ArrayList<>();
        for (Structure structure : allStructures) {
            Long id = structure.getId();
            DashboardStatsDTO.StructureStatsDTO stats = DashboardStatsDTO.StructureStatsDTO.builder()
                    .structureId(id)
                    .structureNom(structureNomMap.get(id))
                    .structureType(structureTypeMap.get(id))
                    .totalDossiers(totalDossiersMap.getOrDefault(id, 0L))
                    .enAttente(enAttenteMap.getOrDefault(id, 0L))
                    .valides(validesMap.getOrDefault(id, 0L))
                    .rejetes(rejetesMap.getOrDefault(id, 0L))
                    .montantTotal(montantTotalMap.getOrDefault(id, 0.0))
                    .annees(getAnneesStatsForStructure(consultations, prescriptionsMedicaments, prescriptionsExamens, id))
                    .build();
            result.add(stats);
        }

        // Trier par type et nom
        return result.stream()
                .sorted(Comparator
                        .comparing(DashboardStatsDTO.StructureStatsDTO::getStructureType)
                        .thenComparing(DashboardStatsDTO.StructureStatsDTO::getStructureNom))
                .collect(Collectors.toList());
    }

    /**
     * Récupérer les statistiques par année pour une structure spécifique
     */
    private List<DashboardStatsDTO.YearStatsDTO> getAnneesStatsForStructure(
            List<Consultation> consultations,
            List<PrescriptionMedicament> prescriptionsMedicaments,
            List<PrescriptionExamen> prescriptionsExamens,
            Long structureId) {

        Map<Integer, Map<Integer, Long>> moisStatsMap = new HashMap<>();
        Map<Integer, Map<Integer, Double>> moisMontantMap = new HashMap<>();
        Map<Integer, Long> totalAnneeMap = new HashMap<>();
        Map<Integer, Double> montantAnneeMap = new HashMap<>();

        // Consultations
        for (Consultation c : consultations) {
            if (c.getStructure() != null && c.getStructure().getId().equals(structureId)) {
                int annee = c.getDateConsultation().getYear();
                int mois = c.getDateConsultation().getMonthValue();

                totalAnneeMap.merge(annee, 1L, Long::sum);
                montantAnneeMap.merge(annee, c.getMontantPrisEnCharge() != null ? c.getMontantPrisEnCharge() : 0, Double::sum);

                moisStatsMap.computeIfAbsent(annee, k -> new HashMap<>()).merge(mois, 1L, Long::sum);
                moisMontantMap.computeIfAbsent(annee, k -> new HashMap<>()).merge(mois, c.getMontantPrisEnCharge() != null ? c.getMontantPrisEnCharge() : 0, Double::sum);
            }
        }

        // Prescriptions médicaments
        for (PrescriptionMedicament pm : prescriptionsMedicaments) {
            if (pm.getPharmacie() != null && pm.getPharmacie().getId().equals(structureId)) {
                int annee = pm.getDatePrescription().getYear();
                int mois = pm.getDatePrescription().getMonthValue();

                totalAnneeMap.merge(annee, 1L, Long::sum);
                montantAnneeMap.merge(annee, pm.getMontantPrisEnCharge() != null ? pm.getMontantPrisEnCharge() : 0, Double::sum);

                moisStatsMap.computeIfAbsent(annee, k -> new HashMap<>()).merge(mois, 1L, Long::sum);
                moisMontantMap.computeIfAbsent(annee, k -> new HashMap<>()).merge(mois, pm.getMontantPrisEnCharge() != null ? pm.getMontantPrisEnCharge() : 0, Double::sum);
            }
        }

        // Prescriptions examens
        for (PrescriptionExamen pe : prescriptionsExamens) {
            if (pe.getLaboratoire() != null && pe.getLaboratoire().getId().equals(structureId)) {
                int annee = pe.getDatePrescription().getYear();
                int mois = pe.getDatePrescription().getMonthValue();

                totalAnneeMap.merge(annee, 1L, Long::sum);
                montantAnneeMap.merge(annee, pe.getMontantPrisEnCharge() != null ? pe.getMontantPrisEnCharge() : 0, Double::sum);

                moisStatsMap.computeIfAbsent(annee, k -> new HashMap<>()).merge(mois, 1L, Long::sum);
                moisMontantMap.computeIfAbsent(annee, k -> new HashMap<>()).merge(mois, pe.getMontantPrisEnCharge() != null ? pe.getMontantPrisEnCharge() : 0, Double::sum);
            }
        }

        String[] nomsMois = {"Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
                "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"};

        List<DashboardStatsDTO.YearStatsDTO> result = new ArrayList<>();
        for (Map.Entry<Integer, Long> entry : totalAnneeMap.entrySet()) {
            int annee = entry.getKey();
            long totalDossiers = entry.getValue();
            double montantTotal = montantAnneeMap.getOrDefault(annee, 0.0);

            List<DashboardStatsDTO.MonthStatsDTO> moisStats = new ArrayList<>();
            for (int i = 1; i <= 12; i++) {
                long total = moisStatsMap.getOrDefault(annee, new HashMap<>()).getOrDefault(i, 0L);
                double montant = moisMontantMap.getOrDefault(annee, new HashMap<>()).getOrDefault(i, 0.0);
                moisStats.add(DashboardStatsDTO.MonthStatsDTO.builder()
                        .mois(i)
                        .nomMois(nomsMois[i - 1])
                        .totalDossiers(total)
                        .montantTotal(montant)
                        .build());
            }

            result.add(DashboardStatsDTO.YearStatsDTO.builder()
                    .annee(annee)
                    .totalDossiers(totalDossiers)
                    .montantTotal(montantTotal)
                    .mois(moisStats)
                    .build());
        }

        // Trier par année décroissante
        return result.stream()
                .sorted((a, b) -> b.getAnnee().compareTo(a.getAnnee()))
                .collect(Collectors.toList());
    }

    /**
     * Valider une consultation
     */
    @Transactional
    public Consultation validerConsultation(Long consultationId, String commentaire) {
        Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new RuntimeException("Consultation non trouvée"));

        if (!"COMPLET".equals(consultation.getStatut())) {
            throw new RuntimeException("Seules les consultations complètes peuvent être validées");
        }

        consultation.setValidationUab(true);
        consultation.setValidationUabDate(java.time.LocalDateTime.now());
        consultation.setStatut("VALIDEE_UAB");

        if (commentaire != null && !commentaire.isEmpty()) {
            consultation.setRemarques(commentaire);
        }

        return consultationRepository.save(consultation);
    }

    /**
     * Rejeter une consultation
     */
    @Transactional
    public Consultation rejeterConsultation(Long consultationId, String motif) {
        Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new RuntimeException("Consultation non trouvée"));

        consultation.setValidationUab(false);
        consultation.setStatut("REJETEE");
        consultation.setRemarques(motif);

        return consultationRepository.save(consultation);
    }

    /**
     * Calculer le montant total à rembourser pour une consultation
     */
    public Double calculerMontantRemboursement(Long consultationId) {
        Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new RuntimeException("Consultation non trouvée"));

        double montantHopital = consultation.getMontantPrisEnCharge() != null ? consultation.getMontantPrisEnCharge() : 0;

        List<PrescriptionMedicament> medicaments = prescriptionMedicamentRepository
                .findByConsultationId(consultationId);
        double montantPharmacie = medicaments.stream()
                .filter(m -> m.getDelivre() != null && m.getDelivre())
                .mapToDouble(m -> m.getMontantPrisEnCharge() != null ? m.getMontantPrisEnCharge() : 0)
                .sum();

        List<PrescriptionExamen> examens = prescriptionExamenRepository
                .findByConsultationId(consultationId);
        double montantLaboratoire = examens.stream()
                .filter(e -> e.getRealise() != null && e.getRealise())
                .mapToDouble(e -> e.getMontantPrisEnCharge() != null ? e.getMontantPrisEnCharge() : 0)
                .sum();

        return montantHopital + montantPharmacie + montantLaboratoire;
    }

    /**
     * Récupérer TOUS les dossiers pour UAB (consultations + prescriptions médicaments + prescriptions examens)
     */
    public List<DossierUABResponseDTO> getAllDossiers(String statut, String numeroPolice) {
        List<DossierUABResponseDTO> dossiers = new ArrayList<>();

        // 1. Ajouter les consultations
        List<Consultation> consultations;
        if (numeroPolice != null && !numeroPolice.isEmpty()) {
            consultations = consultationRepository.findByAssureNumeroPoliceOrderByDateConsultationDesc(numeroPolice);
        } else if (statut != null && !statut.isEmpty()) {
            consultations = consultationRepository.findByStatutOrderByDateConsultationDesc(statut);
        } else {
            consultations = consultationRepository.findAll();
        }

        for (Consultation c : consultations) {
            dossiers.add(convertConsultationToDossier(c));
        }

        // 2. Ajouter les prescriptions médicaments (pharmacie)
        List<PrescriptionMedicament> prescriptionsMedicaments;
        if (numeroPolice != null && !numeroPolice.isEmpty()) {
            prescriptionsMedicaments = prescriptionMedicamentRepository.findByConsultationAssureNumeroPolice(numeroPolice);
        } else {
            prescriptionsMedicaments = prescriptionMedicamentRepository.findAll();
        }

        for (PrescriptionMedicament pm : prescriptionsMedicaments) {
            dossiers.add(convertPrescriptionMedicamentToDossier(pm));
        }

        // 3. Ajouter les prescriptions examens (laboratoire)
        List<PrescriptionExamen> prescriptionsExamens;
        if (numeroPolice != null && !numeroPolice.isEmpty()) {
            prescriptionsExamens = prescriptionExamenRepository.findByConsultationAssureNumeroPolice(numeroPolice);
        } else {
            prescriptionsExamens = prescriptionExamenRepository.findAll();
        }

        for (PrescriptionExamen pe : prescriptionsExamens) {
            dossiers.add(convertPrescriptionExamenToDossier(pe));
        }

        // Filtrer par statut si nécessaire
        if (statut != null && !statut.isEmpty()) {
            dossiers = dossiers.stream()
                    .filter(d -> statut.equals(d.getStatut()))
                    .collect(Collectors.toList());
        }

        return dossiers;
    }

    private DossierUABResponseDTO convertConsultationToDossier(Consultation c) {
        return DossierUABResponseDTO.builder()
                .id(c.getId())
                .numero(c.getNumeroFeuille())
                .type("CONSULTATION")
                .patientNom(c.getAssure().getNom())
                .patientPrenom(c.getAssure().getPrenom())
                .patientPolice(c.getAssure().getNumeroPolice())
                .structureNom(c.getStructure() != null ? c.getStructure().getNom() : "Hôpital")
                .structureId(c.getStructure() != null ? c.getStructure().getId() : null)  // ✅ Ajouter
                .montantTotal(c.getMontantTotalHospitalier())
                .montantPrisEnCharge(c.getMontantPrisEnCharge())
                .montantTicketModerateur(c.getMontantTicketModerateur())
                .statut(c.getStatut())
                .validationUab(c.getValidationUab())
                .dateCreation(c.getDateConsultation())
                .origine("HOPITAL")
                .build();
    }

    private DossierUABResponseDTO convertPrescriptionMedicamentToDossier(PrescriptionMedicament pm) {
        Consultation consultation = pm.getConsultation();
        Assure assure = consultation.getAssure();

        String statut = pm.getDelivre() ? "DELIVRE" : "EN_ATTENTE_DELIVRANCE";

        return DossierUABResponseDTO.builder()
                .id(pm.getId())
                .numero(pm.getNumeroOrdonnance())
                .type("PRESCRIPTION_MEDICAMENT")
                .patientNom(assure.getNom())
                .patientPrenom(assure.getPrenom())
                .patientPolice(assure.getNumeroPolice())
                .structureNom(pm.getPharmacie() != null ? pm.getPharmacie().getNom() : "Pharmacie")
                .structureId(pm.getPharmacie() != null ? pm.getPharmacie().getId() : null)
                .montantTotal(pm.getPrixTotal() != null ? pm.getPrixTotal() : 0)
                .montantPrisEnCharge(pm.getMontantPrisEnCharge() != null ? pm.getMontantPrisEnCharge() : 0)
                .montantTicketModerateur(pm.getMontantTicketModerateur() != null ? pm.getMontantTicketModerateur() : 0)
                .statut(statut)
                .validationUab(pm.getDelivre())
                .dateCreation(pm.getDatePrescription())
                .origine("PHARMACIE")
                .build();
    }

    private DossierUABResponseDTO convertPrescriptionExamenToDossier(PrescriptionExamen pe) {
        Consultation consultation = pe.getConsultation();
        Assure assure = consultation.getAssure();

        String statut = pe.getRealise() ? "REALISE" : (pe.getPaye() ? "PAYE" : "EN_ATTENTE_PAIEMENT");

        return DossierUABResponseDTO.builder()
                .id(pe.getId())
                .numero(pe.getNumeroBulletin())
                .type("PRESCRIPTION_EXAMEN")
                .patientNom(assure.getNom())
                .patientPrenom(assure.getPrenom())
                .patientPolice(assure.getNumeroPolice())
                .structureNom(pe.getLaboratoire() != null ? pe.getLaboratoire().getNom() : "Laboratoire")
                .structureId(pe.getLaboratoire() != null ? pe.getLaboratoire().getId() : null)
                .montantTotal(pe.getPrixTotal() != null ? pe.getPrixTotal() : 0)
                .montantPrisEnCharge(pe.getMontantPrisEnCharge() != null ? pe.getMontantPrisEnCharge() : 0)
                .montantTicketModerateur(pe.getMontantTicketModerateur() != null ? pe.getMontantTicketModerateur() : 0)
                .statut(statut)
                .validationUab(pe.getRealise())
                .dateCreation(pe.getDatePrescription())
                .origine("LABORATOIRE")
                .build();
    }
}
