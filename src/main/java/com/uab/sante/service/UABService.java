// UABService.java - Version complète et corrigée

package com.uab.sante.service;

import com.uab.sante.dto.DashboardStatsDTO;
import com.uab.sante.dto.response.DossierUABResponseDTO;
import com.uab.sante.entities.*;
import com.uab.sante.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UABService {

    private final ConsultationRepository consultationRepository;
    private final PrescriptionMedicamentRepository prescriptionMedicamentRepository;
    private final PrescriptionExamenRepository prescriptionExamenRepository;
    private final StructureRepository structureRepository;

    // ==================== TABLEAU DE BORD ====================

    public DashboardStatsDTO getDashboardStats() {
        System.out.println("=== UABService.getDashboardStats() ===");

        // 1. Consultations PAYEES
        List<Consultation> consultations = consultationRepository.findAll().stream()
                .filter(c -> c.getPaye() != null && c.getPaye())
                .collect(Collectors.toList());

        // 2. Prescriptions médicaments DELIVREES
        List<PrescriptionMedicament> prescriptionsMedicaments = prescriptionMedicamentRepository.findAll().stream()
                .filter(pm -> pm.getDelivre() != null && pm.getDelivre())
                .collect(Collectors.toList());

        // 3. Examens PAYES
        List<PrescriptionExamen> prescriptionsExamens = prescriptionExamenRepository.findAll().stream()
                .filter(pe -> pe.getPaye() != null && pe.getPaye())
                .collect(Collectors.toList());

        System.out.println("Consultations: " + consultations.size());
        System.out.println("Médicaments: " + prescriptionsMedicaments.size());
        System.out.println("Examens: " + prescriptionsExamens.size());

        List<Structure> allStructures = structureRepository.findAll();

        long totalDossiers = consultations.size() + prescriptionsMedicaments.size() + prescriptionsExamens.size();
        double montantTotalPrisEnCharge = getMontantTotalPrisEnCharge(consultations, prescriptionsMedicaments, prescriptionsExamens);

        List<DashboardStatsDTO.StructureStatsDTO> structuresStats = getStructuresStats(
                consultations, prescriptionsMedicaments, prescriptionsExamens, allStructures);

        return DashboardStatsDTO.builder()
                .totalDossiers(totalDossiers)
                .enAttente(0L)
                .valides(totalDossiers)
                .rejetes(0L)
                .montantTotalPrisEnCharge(montantTotalPrisEnCharge)
                .montantTotalRembourse(0.0)
                .structures(structuresStats)
                .build();
    }

// UABService.java - Méthode getStructuresStats() corrigée

    private List<DashboardStatsDTO.StructureStatsDTO> getStructuresStats(
            List<Consultation> consultations,
            List<PrescriptionMedicament> prescriptionsMedicaments,
            List<PrescriptionExamen> prescriptionsExamens,
            List<Structure> allStructures) {

        // Map pour retrouver les structures par ID
        Map<Long, Structure> structureMap = new HashMap<>();
        for (Structure s : allStructures) {
            structureMap.put(s.getId(), s);
        }

        // Map pour agréger les données par structure (on utilise l'ID de la structure parente)
        Map<Long, StructureAggregator> aggregatorMap = new HashMap<>();

        // ========== 1. TRAITER LES CONSULTATIONS ==========
        // Les consultations sont directement liées à l'hôpital
        for (Consultation c : consultations) {
            if (c.getStructure() != null) {
                Structure structure = c.getStructure();
                // Si la structure a un parent, on utilise le parent
                Long structureId = getEffectiveStructureId(structure);

                StructureAggregator aggregator = aggregatorMap.computeIfAbsent(structureId,
                        k -> new StructureAggregator(structureMap.get(structureId)));

                LocalDate date = c.getDateConsultation();
                int annee = date.getYear();
                int mois = date.getMonthValue();
                double montant = c.getMontantPrisEnCharge() != null ? c.getMontantPrisEnCharge() : 0;
                aggregator.addConsultation(annee, mois, montant);

                System.out.println("Consultation -> Structure: " + structure.getNom() +
                        " -> Parent: " + structureMap.get(structureId).getNom());
            }
        }

        // ========== 2. TRAITER LES MÉDICAMENTS (PHARMACIE) ==========
        for (PrescriptionMedicament pm : prescriptionsMedicaments) {
            if (pm.getPharmacie() != null) {
                Structure pharmacie = pm.getPharmacie();
                // Trouver la structure parente (hôpital) si elle existe
                Long structureId = getEffectiveStructureId(pharmacie);

                StructureAggregator aggregator = aggregatorMap.computeIfAbsent(structureId,
                        k -> new StructureAggregator(structureMap.get(structureId)));

                LocalDate date = pm.getDatePrescription();
                int annee = date.getYear();
                int mois = date.getMonthValue();
                double montant = pm.getMontantPrisEnCharge() != null ? pm.getMontantPrisEnCharge() : 0;
                aggregator.addMedicament(annee, mois, montant);

                System.out.println("Médicament -> Pharmacie: " + pharmacie.getNom() +
                        " -> Parent: " + structureMap.get(structureId).getNom());
            }
        }

        // ========== 3. TRAITER LES EXAMENS (LABORATOIRE) ==========
        for (PrescriptionExamen pe : prescriptionsExamens) {
            if (pe.getLaboratoire() != null) {
                Structure laboratoire = pe.getLaboratoire();
                // Trouver la structure parente (hôpital) si elle existe
                Long structureId = getEffectiveStructureId(laboratoire);

                System.out.println("=== EXAMEN ===");
                System.out.println("Laboratoire: " + laboratoire.getNom());
                System.out.println("Type: " + laboratoire.getType());
                System.out.println("Parent ID: " + (laboratoire.getStructureParente() != null ?
                        laboratoire.getStructureParente().getId() : "null"));
                System.out.println("Structure effective ID: " + structureId);
                System.out.println("Structure effective: " + structureMap.get(structureId).getNom());

                StructureAggregator aggregator = aggregatorMap.computeIfAbsent(structureId,
                        k -> new StructureAggregator(structureMap.get(structureId)));

                LocalDate date = pe.getDatePaiement() != null ? pe.getDatePaiement() : pe.getDatePrescription();
                int annee = date.getYear();
                int mois = date.getMonthValue();
                double montant = pe.getMontantPrisEnCharge() != null ? pe.getMontantPrisEnCharge() : 0;
                aggregator.addExamen(annee, mois, montant);

                System.out.println("Examen ajouté -> Parent: " + structureMap.get(structureId).getNom());
            }
        }

        // Construire les résultats
        List<DashboardStatsDTO.StructureStatsDTO> result = new ArrayList<>();
        for (StructureAggregator aggregator : aggregatorMap.values()) {
            if (aggregator.getTotalDossiers() > 0) {
                result.add(aggregator.toStatsDTO());
            }
        }

        return result.stream()
                .sorted(Comparator
                        .comparing(DashboardStatsDTO.StructureStatsDTO::getStructureType)
                        .thenComparing(DashboardStatsDTO.StructureStatsDTO::getStructureNom))
                .collect(Collectors.toList());
    }

// UABService.java - Ajouter cette classe interne après les autres méthodes

    /**
     * Classe interne pour agréger les données par structure
     */
    private static class StructureAggregator {
        private final Structure structure;
        private final Map<Integer, Map<Integer, Long>> dossiersParAnneeMois = new HashMap<>();
        private final Map<Integer, Map<Integer, Double>> montantParAnneeMois = new HashMap<>();
        private final Map<Integer, Long> totalParAnnee = new HashMap<>();
        private final Map<Integer, Double> montantTotalParAnnee = new HashMap<>();
        private long totalDossiers = 0;
        private double montantTotal = 0;

        public StructureAggregator(Structure structure) {
            this.structure = structure;
        }

        public void addConsultation(int annee, int mois, double montant) {
            add(annee, mois, montant);
        }

        public void addMedicament(int annee, int mois, double montant) {
            add(annee, mois, montant);
        }

        public void addExamen(int annee, int mois, double montant) {
            add(annee, mois, montant);
        }

        private void add(int annee, int mois, double montant) {
            dossiersParAnneeMois.computeIfAbsent(annee, k -> new HashMap<>())
                    .merge(mois, 1L, Long::sum);
            montantParAnneeMois.computeIfAbsent(annee, k -> new HashMap<>())
                    .merge(mois, montant, Double::sum);
            totalParAnnee.merge(annee, 1L, Long::sum);
            montantTotalParAnnee.merge(annee, montant, Double::sum);
            totalDossiers++;
            montantTotal += montant;
        }

        public long getTotalDossiers() {
            return totalDossiers;
        }

        public DashboardStatsDTO.StructureStatsDTO toStatsDTO() {
            String[] nomsMois = {"Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
                    "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"};

            List<DashboardStatsDTO.YearStatsDTO> anneesStats = new ArrayList<>();

            for (Map.Entry<Integer, Long> anneeEntry : totalParAnnee.entrySet()) {
                int annee = anneeEntry.getKey();
                long totalDossiersAnnee = anneeEntry.getValue();
                double montantTotalAnnee = montantTotalParAnnee.getOrDefault(annee, 0.0);

                List<DashboardStatsDTO.MonthStatsDTO> moisStats = new ArrayList<>();
                for (int i = 1; i <= 12; i++) {
                    long nbDossiers = dossiersParAnneeMois.getOrDefault(annee, new HashMap<>()).getOrDefault(i, 0L);
                    double montantMois = montantParAnneeMois.getOrDefault(annee, new HashMap<>()).getOrDefault(i, 0.0);

                    moisStats.add(DashboardStatsDTO.MonthStatsDTO.builder()
                            .mois(i)
                            .nomMois(nomsMois[i - 1])
                            .totalDossiers(nbDossiers)
                            .montantTotal(montantMois)
                            .build());
                }

                anneesStats.add(DashboardStatsDTO.YearStatsDTO.builder()
                        .annee(annee)
                        .totalDossiers(totalDossiersAnnee)
                        .montantTotal(montantTotalAnnee)
                        .mois(moisStats)
                        .build());
            }

            anneesStats.sort((a, b) -> b.getAnnee().compareTo(a.getAnnee()));

            return DashboardStatsDTO.StructureStatsDTO.builder()
                    .structureId(structure.getId())
                    .structureNom(structure.getNom())
                    .structureType(structure.getType().name())
                    .totalDossiers(totalDossiers)
                    .montantTotal(montantTotal)
                    .annees(anneesStats)
                    .build();
        }
    }

    /**
     * Retourne l'ID de la structure à utiliser pour l'agrégation
     * Si la structure a un parent, on utilise le parent (hôpital)
     * Sinon on utilise la structure elle-même
     */
    private Long getEffectiveStructureId(Structure structure) {
        if (structure.getStructureParente() != null) {
            return structure.getStructureParente().getId();
        }
        return structure.getId();
    }

    // ==================== VALIDATION ====================

    @Transactional
    public Object validerDossier(Long id, String type, String commentaire) {
        System.out.println("=== VALIDATION DOSSIER ===");
        System.out.println("ID: " + id);
        System.out.println("Type: " + type);

        switch (type) {
            case "CONSULTATION":
                return validerConsultation(id, commentaire);
            case "PRESCRIPTION_MEDICAMENT":
                return validerPrescriptionMedicament(id, commentaire);
            case "PRESCRIPTION_EXAMEN":
                return validerPrescriptionExamen(id, commentaire);
            default:
                throw new RuntimeException("Type inconnu: " + type);
        }
    }

    @Transactional
    public Consultation validerConsultation(Long consultationId, String commentaire) {
        Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new RuntimeException("Consultation non trouvée"));

        consultation.setValidationUabBool(true);
        consultation.setValidationUabDate(LocalDateTime.now());
        consultation.setStatut("VALIDEE_UAB");

        return consultationRepository.save(consultation);
    }

    @Transactional
    public PrescriptionMedicament validerPrescriptionMedicament(Long id, String commentaire) {
        PrescriptionMedicament pm = prescriptionMedicamentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prescription non trouvée"));

        pm.setValidationUabBool(true);
        pm.setValidationUabDate(LocalDateTime.now());

        return prescriptionMedicamentRepository.save(pm);
    }

    @Transactional
    public PrescriptionExamen validerPrescriptionExamen(Long id, String commentaire) {
        PrescriptionExamen pe = prescriptionExamenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Examen non trouvé"));

        pe.setValidationUabBool(true);
        pe.setValidationUabDate(LocalDateTime.now());

        return prescriptionExamenRepository.save(pe);
    }

    @Transactional
    public Object rejeterDossier(Long id, String type, String motif) {
        System.out.println("=== REJET DOSSIER ===");
        System.out.println("ID: " + id);
        System.out.println("Type: " + type);

        switch (type) {
            case "CONSULTATION":
                return rejeterConsultation(id, motif);
            case "PRESCRIPTION_MEDICAMENT":
                return rejeterPrescriptionMedicament(id, motif);
            case "PRESCRIPTION_EXAMEN":
                return rejeterPrescriptionExamen(id, motif);
            default:
                throw new RuntimeException("Type inconnu: " + type);
        }
    }

    @Transactional
    public Consultation rejeterConsultation(Long consultationId, String motif) {
        Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new RuntimeException("Consultation non trouvée"));

        consultation.setValidationUabBool(false);
        consultation.setValidationUabDate(LocalDateTime.now());
        consultation.setStatut("REJETEE_UAB");
        consultation.setMotifRejet(motif);

        return consultationRepository.save(consultation);
    }

    @Transactional
    public PrescriptionMedicament rejeterPrescriptionMedicament(Long id, String motif) {
        PrescriptionMedicament pm = prescriptionMedicamentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prescription non trouvée"));

        pm.setValidationUabBool(false);
        pm.setValidationUabDate(LocalDateTime.now());
        pm.setMotifRejet(motif);

        return prescriptionMedicamentRepository.save(pm);
    }

    @Transactional
    public PrescriptionExamen rejeterPrescriptionExamen(Long id, String motif) {
        PrescriptionExamen pe = prescriptionExamenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Examen non trouvé"));

        pe.setValidationUabBool(false);
        pe.setValidationUabDate(LocalDateTime.now());
        pe.setMotifRejet(motif);

        return prescriptionExamenRepository.save(pe);
    }

    // ==================== DOSSIERS ====================

    public List<DossierUABResponseDTO> getAllDossiers(String statut, String numeroPolice) {
        System.out.println("=== UABService.getAllDossiers() ===");

        List<DossierUABResponseDTO> dossiers = new ArrayList<>();

        // Consultations PAYEES
        List<Consultation> consultations;
        if (numeroPolice != null && !numeroPolice.isEmpty()) {
            consultations = consultationRepository.findByAssureNumeroPoliceOrderByDateConsultationDesc(numeroPolice);
        } else {
            consultations = consultationRepository.findAll();
        }

        for (Consultation c : consultations) {
            if (c.getPaye() != null && c.getPaye()) {
                dossiers.add(convertConsultationToDossier(c));
            }
        }

        // Prescriptions médicaments DELIVREES
        List<PrescriptionMedicament> prescriptionsMedicaments;
        if (numeroPolice != null && !numeroPolice.isEmpty()) {
            prescriptionsMedicaments = prescriptionMedicamentRepository.findByConsultationAssureNumeroPolice(numeroPolice);
        } else {
            prescriptionsMedicaments = prescriptionMedicamentRepository.findAll();
        }

        for (PrescriptionMedicament pm : prescriptionsMedicaments) {
            if (pm.getDelivre() != null && pm.getDelivre()) {
                dossiers.add(convertPrescriptionMedicamentToDossier(pm));
            }
        }

        // Prescriptions examens PAYES
        List<PrescriptionExamen> prescriptionsExamens;
        if (numeroPolice != null && !numeroPolice.isEmpty()) {
            prescriptionsExamens = prescriptionExamenRepository.findByConsultationAssureNumeroPolice(numeroPolice);
        } else {
            prescriptionsExamens = prescriptionExamenRepository.findAll();
        }

        for (PrescriptionExamen pe : prescriptionsExamens) {
            if (pe.getPaye() != null && pe.getPaye()) {
                dossiers.add(convertPrescriptionExamenToDossier(pe));
            }
        }

        if (statut != null && !statut.isEmpty()) {
            dossiers = dossiers.stream()
                    .filter(d -> statut.equals(d.getStatut()))
                    .collect(Collectors.toList());
        }

        System.out.println("Nombre total de dossiers: " + dossiers.size());
        return dossiers;
    }

    // ==================== MÉTHODE POUR LE DÉTAIL D'UN DOSSIER ====================

    public Map<String, Object> getDossierDetail(Long id) {
        System.out.println("=== UABService.getDossierDetail() ===");
        System.out.println("ID: " + id);

        Map<String, Object> result = new HashMap<>();

        // ==================== 1. RECHERCHER DANS LES PRESCRIPTIONS EXAMENS ====================
        Optional<PrescriptionExamen> peOpt = prescriptionExamenRepository.findById(id);
        System.out.println("=============================EXAM &======================================");
        if (peOpt.isPresent()) {
            PrescriptionExamen pe = peOpt.get();
            Consultation c = pe.getConsultation();
            System.out.println("=============================EXAM======================================");
            System.out.println("✅ Trouvé une prescription EXAMEN avec ID: " + pe.getId());
            System.out.println("   - examenNom: " + pe.getExamenNom());
            System.out.println("   - paye: " + pe.getPaye());
            System.out.println("   - datePaiement: " + pe.getDatePaiement());
            System.out.println("==============================EXAM=====================================");

            String medecinNom = null;
            if (c.getMedecin() != null) {
                medecinNom = c.getMedecin().getPrenom() + " " + c.getMedecin().getNom();
            }

            result.put("id", pe.getId());
            result.put("numero", pe.getNumeroBulletin());
            result.put("type", "PRESCRIPTION_EXAMEN");
            result.put("patientNom", c.getAssure().getNom());
            result.put("patientPrenom", c.getAssure().getPrenom());
            result.put("patientPolice", c.getAssure().getNumeroPolice());
            result.put("montantTotal", pe.getPrixTotal());
            result.put("montantPrisEnCharge", pe.getMontantPrisEnCharge());
            result.put("montantTicketModerateur", pe.getMontantTicketModerateur());
            result.put("statut", pe.getPaye() ? "PAYE" : "EN_ATTENTE");
            result.put("validationUab", pe.getValidationUabBool());
            result.put("dateCreation", pe.getDatePrescription());
            result.put("codeInte", c.getCodeInte());
            result.put("codeRisq", c.getCodeRisq());
            result.put("motifRejet", pe.getMotifRejet());
            result.put("structureNom", pe.getLaboratoire() != null ? pe.getLaboratoire().getNom() : null);
            result.put("structureId", pe.getLaboratoire() != null ? pe.getLaboratoire().getId() : null);
            result.put("origine", "LABORATOIRE");
            result.put("medecinNom", medecinNom);
            result.put("examenNom", pe.getExamenNom());
            result.put("examenCode", pe.getCodeActe());
            result.put("datePaiement", pe.getDatePaiement());
            result.put("instructions", pe.getInstructions());
            result.put("realise", pe.getRealise());
            result.put("paye", pe.getPaye());


            return result;
        }

        // ==================== 2. RECHERCHER DANS LES CONSULTATIONS ====================
        Optional<Consultation> consultationOpt = consultationRepository.findById(id);
        System.out.println("=============================CONSUL &======================================");
        if (consultationOpt.isPresent()) {
            Consultation c = consultationOpt.get();
            System.out.println("=============================CONSUL ======================================");
            System.out.println("✅ Trouvé une CONSULTATION avec ID: " + c.getId());
            System.out.println("✅ Trouvé une CONSULTATION avec PRIX: " + c.getPrixConsultation());
            System.out.println("=============================CONSUL ======================================");


            String medecinNom = null;
            if (c.getMedecin() != null) {
                medecinNom = c.getMedecin().getPrenom() + " " + c.getMedecin().getNom();
            }

            result.put("id", c.getId());
            result.put("numero", c.getNumeroFeuille());
            result.put("type", "CONSULTATION");
            result.put("patientNom", c.getAssure().getNom());
            result.put("patientPrenom", c.getAssure().getPrenom());
            result.put("patientPolice", c.getAssure().getNumeroPolice());
            result.put("montantTotal", c.getMontantTotalHospitalier());
            result.put("montantPrisEnCharge", c.getMontantPrisEnCharge());
            result.put("montantTicketModerateur", c.getMontantTicketModerateur());
            result.put("statut", c.getStatut());
            result.put("validationUab", c.getValidationUabBool());
            result.put("dateCreation", c.getDateConsultation());
            result.put("codeInte", c.getCodeInte());
            result.put("codeRisq", c.getCodeRisq());
            result.put("motifRejet", c.getMotifRejet());
            result.put("structureNom", c.getStructure() != null ? c.getStructure().getNom() : null);
            result.put("structureId", c.getStructure() != null ? c.getStructure().getId() : null);
            result.put("origine", "HOPITAL");
            result.put("medecinNom", medecinNom);
            result.put("natureMaladie", c.getNatureMaladie());
            result.put("diagnostic", c.getDiagnostic());
            result.put("actesMedicaux", c.getActesMedicaux());
            result.put("typeConsultation", c.getTypeConsultation());

            // Prescriptions médicaments
            List<Map<String, Object>> medocs = new ArrayList<>();
            if (c.getPrescriptionsMedicaments() != null && !c.getPrescriptionsMedicaments().isEmpty()) {
                for (PrescriptionMedicament pm : c.getPrescriptionsMedicaments()) {
                    Map<String, Object> medMap = new HashMap<>();
                    medMap.put("id", pm.getId());
                    medMap.put("medicamentNom", pm.getMedicamentNom());
                    medMap.put("medicamentDosage", pm.getMedicamentDosage());
                    medMap.put("medicamentForme", pm.getMedicamentForme());
                    medMap.put("quantitePrescitee", pm.getQuantitePrescitee());
                    medMap.put("quantiteDelivree", pm.getQuantiteDelivree());
                    medMap.put("instructions", pm.getInstructions());
                    medMap.put("delivre", pm.getDelivre());
                    medMap.put("prixTotal", pm.getPrixTotal());
                    medMap.put("montantPrisEnCharge", pm.getMontantPrisEnCharge());
                    medocs.add(medMap);
                }
            }
            result.put("prescriptionsMedicaments", medocs);

            // Prescriptions examens
            List<Map<String, Object>> examens = new ArrayList<>();
            if (c.getPrescriptionsExamens() != null && !c.getPrescriptionsExamens().isEmpty()) {
                for (PrescriptionExamen pe : c.getPrescriptionsExamens()) {
                    Map<String, Object> examMap = new HashMap<>();
                    examMap.put("id", pe.getId());
                    examMap.put("examenNom", pe.getExamenNom());
                    examMap.put("codeActe", pe.getCodeActe());
                    examMap.put("instructions", pe.getInstructions());
                    examMap.put("realise", pe.getRealise());
                    examMap.put("paye", pe.getPaye());
                    examMap.put("prixTotal", pe.getPrixTotal());
                    examMap.put("montantPrisEnCharge", pe.getMontantPrisEnCharge());
                    examMap.put("datePaiement", pe.getDatePaiement());
                    examens.add(examMap);
                }
            }
            result.put("prescriptionsExamens", examens);

            return result;
        }

        // ==================== 3. RECHERCHER DANS LES PRESCRIPTIONS MÉDICAMENTS ====================
        Optional<PrescriptionMedicament> pmOpt = prescriptionMedicamentRepository.findById(id);
        System.out.println("=============================MEDOC &======================================");
        if (pmOpt.isPresent()) {
            PrescriptionMedicament pm = pmOpt.get();
            Consultation c = pm.getConsultation();
            System.out.println("=============================MEDOC ======================================");
            System.out.println("✅ Trouvé une prescription MÉDICAMENT avec ID: " + pm.getId());
            System.out.println("   - medicamentNom: " + pm.getMedicamentNom());
            System.out.println("   - medicamentDosage: " + pm.getMedicamentDosage());
            System.out.println("   - delivre: " + pm.getDelivre());
            System.out.println("✅ Trouvé une prescription MÉDICAMENT avec ID: " + pm.getId());
            System.out.println("=============================MEDOC ======================================");

            String medecinNom = null;
            if (c.getMedecin() != null) {
                medecinNom = c.getMedecin().getPrenom() + " " + c.getMedecin().getNom();
            }

            result.put("id", pm.getId());
            result.put("numero", pm.getNumeroOrdonnance());
            result.put("type", "PRESCRIPTION_MEDICAMENT");
            result.put("patientNom", c.getAssure().getNom());
            result.put("patientPrenom", c.getAssure().getPrenom());
            result.put("patientPolice", c.getAssure().getNumeroPolice());
            result.put("montantTotal", pm.getPrixTotal());
            result.put("montantPrisEnCharge", pm.getMontantPrisEnCharge());
            result.put("montantTicketModerateur", pm.getMontantTicketModerateur());
            result.put("statut", pm.getDelivre() ? "DELIVRE" : "EN_ATTENTE");
            result.put("validationUab", pm.getValidationUabBool());
            result.put("dateCreation", pm.getDatePrescription());
            result.put("codeInte", c.getCodeInte());
            result.put("codeRisq", c.getCodeRisq());
            result.put("motifRejet", pm.getMotifRejet());
            result.put("structureNom", pm.getPharmacie() != null ? pm.getPharmacie().getNom() : null);
            result.put("structureId", pm.getPharmacie() != null ? pm.getPharmacie().getId() : null);
            result.put("origine", "PHARMACIE");
            result.put("medecinNom", medecinNom);
            result.put("medicamentNom", pm.getMedicamentNom());
            result.put("medicamentDosage", pm.getMedicamentDosage());
            result.put("medicamentForme", pm.getMedicamentForme());
            result.put("quantite", pm.getQuantitePrescitee());        // Utilisé dans le template
            result.put("quantitePrescitee", pm.getQuantitePrescitee()); // Backup
            result.put("quantiteDelivree", pm.getQuantiteDelivree());
            result.put("delivre", pm.getDelivre());
            result.put("instructions", pm.getInstructions());



            return result;
        }

        System.out.println("❌ Aucun dossier trouvé avec l'ID: " + id);
        return null;
    }
    // ==================== CONVERSIONS ====================

    private DossierUABResponseDTO convertConsultationToDossier(Consultation c) {
        return DossierUABResponseDTO.builder()
                .id(c.getId())
                .numero(c.getNumeroFeuille())
                .type("CONSULTATION")
                .patientNom(c.getAssure().getNom())
                .patientPrenom(c.getAssure().getPrenom())
                .patientPolice(c.getAssure().getNumeroPolice())
                .structureNom(c.getStructure() != null ? c.getStructure().getNom() : "Hôpital")
                .structureId(c.getStructure() != null ? c.getStructure().getId() : null)
                .montantTotal(c.getMontantTotalHospitalier())
                .montantPrisEnCharge(c.getMontantPrisEnCharge())
                .montantTicketModerateur(c.getMontantTicketModerateur())
                .statut(c.getValidationUabBool() != null && c.getValidationUabBool() ? "VALIDEE_UAB" : "PAYEE_CAISSE")
                .validationUab(c.getValidationUabBool())
                .dateCreation(c.getDateConsultation())
                .origine("HOPITAL")
                .codeInte(c.getCodeInte())
                .codeRisq(c.getCodeRisq())
                .typeConsultation(c.getTypeConsultation())
                .medecinNom(c.getMedecin() != null ? c.getMedecin().getPrenom() + " " + c.getMedecin().getNom() : null)
                .build();
    }

    private DossierUABResponseDTO convertPrescriptionMedicamentToDossier(PrescriptionMedicament pm) {
        Consultation consultation = pm.getConsultation();
        Assure assure = consultation.getAssure();

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
                .statut(pm.getValidationUabBool() != null && pm.getValidationUabBool() ? "VALIDEE_UAB" : "DELIVRE")
                .validationUab(pm.getValidationUabBool())
                .dateCreation(pm.getDatePrescription())
                .codeInte(consultation.getCodeInte())
                .codeRisq(consultation.getCodeRisq())
                .medicamentNom(pm.getMedicament().getNom())
                .origine("PHARMACIE")
                .build();
    }

    private DossierUABResponseDTO convertPrescriptionExamenToDossier(PrescriptionExamen pe) {
        Consultation consultation = pe.getConsultation();
        Assure assure = consultation.getAssure();

        // ✅ Récupérer le nom de l'examen depuis l'entité PrescriptionExamen
        String examenNom = pe.getExamenNom();
        if (examenNom == null && pe.getExamen() != null) {
            examenNom = pe.getExamen().getNom();
        }

        // ✅ Récupérer le code acte
        String codeActe = pe.getCodeActe();
        if (codeActe == null && pe.getExamen() != null) {
            codeActe = pe.getExamen().getCode();
        }

        String statutDossier;
        if (pe.getValidationUabBool() != null && pe.getValidationUabBool()) {
            statutDossier = "VALIDEE_UAB";
        } else if (pe.getValidationUabBool() != null && !pe.getValidationUabBool()) {
            statutDossier = "REJETEE_UAB";
        } else {
            statutDossier = "EN_ATTENTE";
        }

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
                .statut(statutDossier)
                .validationUab(pe.getValidationUabBool())
                .dateCreation(pe.getDatePrescription())
                .codeInte(consultation.getCodeInte())
                .codeRisq(consultation.getCodeRisq())
                .origine("LABORATOIRE")
                // ✅ AJOUTER CES CHAMPS IMPORTANTS
                .examenNom(pe.getExamenNom())
                .examenCode(pe.getCodeActe())
                .paye(pe.getPaye())
                .realise(pe.getRealise())
                .datePaiement(pe.getDatePaiement())
                .instructions(pe.getInstructions())
                .build();
    }

    // ==================== UTILITAIRES ====================

    private double getMontantTotalPrisEnCharge(List<Consultation> consultations,
                                               List<PrescriptionMedicament> prescriptionsMedicaments,
                                               List<PrescriptionExamen> prescriptionsExamens) {
        double montantConsultations = consultations.stream()
                .mapToDouble(c -> c.getMontantPrisEnCharge() != null ? c.getMontantPrisEnCharge() : 0)
                .sum();
        double montantMedicaments = prescriptionsMedicaments.stream()
                .mapToDouble(pm -> pm.getMontantPrisEnCharge() != null ? pm.getMontantPrisEnCharge() : 0)
                .sum();
        double montantExamens = prescriptionsExamens.stream()
                .mapToDouble(pe -> pe.getMontantPrisEnCharge() != null ? pe.getMontantPrisEnCharge() : 0)
                .sum();
        return montantConsultations + montantMedicaments + montantExamens;
    }

    public Double calculerMontantRemboursement(Long consultationId) {
        Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new RuntimeException("Consultation non trouvée"));
        return consultation.getMontantPrisEnCharge() != null ? consultation.getMontantPrisEnCharge() : 0;
    }

    public PrescriptionMedicament getPrescriptionMedicamentById(Long id) {
        return prescriptionMedicamentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prescription non trouvée"));
    }

    public PrescriptionExamen getPrescriptionExamenById(Long id) {
        return prescriptionExamenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Examen non trouvé"));
    }


// UABService.java - AJOUTER cette nouvelle méthode (ne pas modifier getAllDossiers)

    public Page<DossierUABResponseDTO> getAllDossiersPaginated(String statut, String numeroPolice,
                                                               int page, int size) {
        System.out.println("=== UABService.getAllDossiersPaginated() ===");
        System.out.println("Page: " + page + ", Size: " + size);

        // ✅ Correction : utiliser une page non triée ou trier par dateConsultation
        Pageable pageable = PageRequest.of(page, size, Sort.by("dateConsultation").descending());

        List<DossierUABResponseDTO> dossiers = new ArrayList<>();

        // Récupérer toutes les consultations (non paginées pour l'instant)
        List<Consultation> consultations;
        if (numeroPolice != null && !numeroPolice.isEmpty()) {
            consultations = consultationRepository.findByAssureNumeroPoliceOrderByDateConsultationDesc(numeroPolice);
        } else {
            consultations = consultationRepository.findAll();
        }

        for (Consultation c : consultations) {
            if (c.getPaye() != null && c.getPaye()) {
                dossiers.add(convertConsultationToDossier(c));
            }
        }

        // Prescriptions médicaments
        List<PrescriptionMedicament> prescriptionsMedicaments;
        if (numeroPolice != null && !numeroPolice.isEmpty()) {
            prescriptionsMedicaments = prescriptionMedicamentRepository
                    .findByConsultationAssureNumeroPolice(numeroPolice);
        } else {
            prescriptionsMedicaments = prescriptionMedicamentRepository.findAll();
        }

        for (PrescriptionMedicament pm : prescriptionsMedicaments) {
            if (pm.getDelivre() != null && pm.getDelivre()) {
                dossiers.add(convertPrescriptionMedicamentToDossier(pm));
            }
        }

        // Prescriptions examens
        List<PrescriptionExamen> prescriptionsExamens;
        if (numeroPolice != null && !numeroPolice.isEmpty()) {
            prescriptionsExamens = prescriptionExamenRepository
                    .findByConsultationAssureNumeroPolice(numeroPolice);
        } else {
            prescriptionsExamens = prescriptionExamenRepository.findAll();
        }

        for (PrescriptionExamen pe : prescriptionsExamens) {
            if (pe.getPaye() != null && pe.getPaye()) {
                dossiers.add(convertPrescriptionExamenToDossier(pe));
            }
        }

        // Filtrer par statut
        if (statut != null && !statut.isEmpty()) {
            dossiers = dossiers.stream()
                    .filter(d -> statut.equals(d.getStatut()))
                    .collect(Collectors.toList());
        }

        // Trier par date de création (du plus récent au plus ancien)
        dossiers.sort((a, b) -> b.getDateCreation().compareTo(a.getDateCreation()));

        // Appliquer la pagination manuellement
        int start = page * size;
        int end = Math.min(start + size, dossiers.size());
        List<DossierUABResponseDTO> paginatedList = dossiers.subList(start, end);

        long totalElements = dossiers.size();

        return new PageImpl<>(paginatedList, pageable, totalElements);
    }


    // UABService.java - Ajouter ces trois méthodes

    public Map<String, Object> getDossierConsultationDetail(Long id) {
        System.out.println("=== RECHERCHE CONSULTATION SPÉCIFIQUE ===");
        System.out.println("ID: " + id);

        Optional<Consultation> consultationOpt = consultationRepository.findById(id);
        if (consultationOpt.isEmpty()) {
            System.out.println("❌ Consultation non trouvée pour l'ID: " + id);
            return null;
        }

        Consultation c = consultationOpt.get();
        System.out.println("✅ Consultation trouvée pour l'ID: " + c.getId());

        Map<String, Object> result = new HashMap<>();
        String medecinNom = null;
        if (c.getMedecin() != null) {
            medecinNom = c.getMedecin().getPrenom() + " " + c.getMedecin().getNom();
        }

        result.put("id", c.getId());
        result.put("numero", c.getNumeroFeuille());
        result.put("type", "CONSULTATION");
        result.put("patientNom", c.getAssure().getNom());
        result.put("patientPrenom", c.getAssure().getPrenom());
        result.put("patientPolice", c.getAssure().getNumeroPolice());
        result.put("montantTotal", c.getMontantTotalHospitalier());
        result.put("montantPrisEnCharge", c.getMontantPrisEnCharge());
        result.put("montantTicketModerateur", c.getMontantTicketModerateur());
        result.put("statut", c.getStatut());
        result.put("validationUab", c.getValidationUabBool());
        result.put("dateCreation", c.getDateConsultation());
        result.put("codeInte", c.getCodeInte());
        result.put("codeRisq", c.getCodeRisq());
        result.put("motifRejet", c.getMotifRejet());
        result.put("structureNom", c.getStructure() != null ? c.getStructure().getNom() : null);
        result.put("structureId", c.getStructure() != null ? c.getStructure().getId() : null);
        result.put("origine", "HOPITAL");
        result.put("medecinNom", medecinNom);
        result.put("natureMaladie", c.getNatureMaladie());
        result.put("diagnostic", c.getDiagnostic());
        result.put("actesMedicaux", c.getActesMedicaux());
        result.put("typeConsultation", c.getTypeConsultation());

        // Prescriptions médicaments
        List<Map<String, Object>> medocs = new ArrayList<>();
        if (c.getPrescriptionsMedicaments() != null && !c.getPrescriptionsMedicaments().isEmpty()) {
            for (PrescriptionMedicament pm : c.getPrescriptionsMedicaments()) {
                Map<String, Object> medMap = new HashMap<>();
                medMap.put("id", pm.getId());
                medMap.put("medicamentNom", pm.getMedicamentNom());
                medMap.put("medicamentDosage", pm.getMedicamentDosage());
                medMap.put("medicamentForme", pm.getMedicamentForme());
                medMap.put("quantitePrescitee", pm.getQuantitePrescitee());
                medMap.put("quantiteDelivree", pm.getQuantiteDelivree());
                medMap.put("instructions", pm.getInstructions());
                medMap.put("delivre", pm.getDelivre());
                medMap.put("prixTotal", pm.getPrixTotal());
                medMap.put("montantPrisEnCharge", pm.getMontantPrisEnCharge());
                medocs.add(medMap);
            }
        }
        result.put("prescriptionsMedicaments", medocs);

        // Prescriptions examens
        List<Map<String, Object>> examens = new ArrayList<>();
        if (c.getPrescriptionsExamens() != null && !c.getPrescriptionsExamens().isEmpty()) {
            for (PrescriptionExamen pe : c.getPrescriptionsExamens()) {
                Map<String, Object> examMap = new HashMap<>();
                examMap.put("id", pe.getId());
                examMap.put("examenNom", pe.getExamenNom());
                examMap.put("codeActe", pe.getCodeActe());
                examMap.put("instructions", pe.getInstructions());
                examMap.put("realise", pe.getRealise());
                examMap.put("paye", pe.getPaye());
                examMap.put("prixTotal", pe.getPrixTotal());
                examMap.put("montantPrisEnCharge", pe.getMontantPrisEnCharge());
                examMap.put("datePaiement", pe.getDatePaiement());
                examens.add(examMap);
            }
        }
        result.put("prescriptionsExamens", examens);

        return result;
    }

    public Map<String, Object> getDossierExamenDetail(Long id) {
        System.out.println("=== RECHERCHE EXAMEN SPÉCIFIQUE ===");
        System.out.println("ID: " + id);

        Optional<PrescriptionExamen> peOpt = prescriptionExamenRepository.findById(id);
        if (peOpt.isEmpty()) {
            System.out.println("❌ Examen non trouvé pour l'ID: " + id);
            return null;
        }

        PrescriptionExamen pe = peOpt.get();
        Consultation c = pe.getConsultation();
        System.out.println("✅ Examen trouvé pour l'ID: " + pe.getId());

        Map<String, Object> result = new HashMap<>();
        String medecinNom = null;
        if (c.getMedecin() != null) {
            medecinNom = c.getMedecin().getPrenom() + " " + c.getMedecin().getNom();
        }

        result.put("id", pe.getId());
        result.put("numero", pe.getNumeroBulletin());
        result.put("type", "PRESCRIPTION_EXAMEN");
        result.put("patientNom", c.getAssure().getNom());
        result.put("patientPrenom", c.getAssure().getPrenom());
        result.put("patientPolice", c.getAssure().getNumeroPolice());
        result.put("montantTotal", pe.getPrixTotal());
        result.put("montantPrisEnCharge", pe.getMontantPrisEnCharge());
        result.put("montantTicketModerateur", pe.getMontantTicketModerateur());
        result.put("statut", pe.getPaye() ? "PAYE" : "EN_ATTENTE");
        result.put("validationUab", pe.getValidationUabBool());
        result.put("dateCreation", pe.getDatePrescription());
        result.put("codeInte", c.getCodeInte());
        result.put("codeRisq", c.getCodeRisq());
        result.put("motifRejet", pe.getMotifRejet());
        result.put("structureNom", pe.getLaboratoire() != null ? pe.getLaboratoire().getNom() : null);
        result.put("structureId", pe.getLaboratoire() != null ? pe.getLaboratoire().getId() : null);
        result.put("origine", "LABORATOIRE");
        result.put("medecinNom", medecinNom);
        result.put("examenNom", pe.getExamenNom());
        result.put("examenCode", pe.getCodeActe());
        result.put("datePaiement", pe.getDatePaiement());
        result.put("instructions", pe.getInstructions());
        result.put("realise", pe.getRealise());
        result.put("paye", pe.getPaye());

        return result;
    }

    public Map<String, Object> getDossierMedicamentDetail(Long id) {
        System.out.println("=== RECHERCHE MÉDICAMENT SPÉCIFIQUE ===");
        System.out.println("ID: " + id);

        Optional<PrescriptionMedicament> pmOpt = prescriptionMedicamentRepository.findById(id);
        if (pmOpt.isEmpty()) {
            System.out.println("❌ Médicament non trouvé pour l'ID: " + id);
            return null;
        }

        PrescriptionMedicament pm = pmOpt.get();
        Consultation c = pm.getConsultation();
        System.out.println("✅ Médicament trouvé pour l'ID: " + pm.getId());

        Map<String, Object> result = new HashMap<>();
        String medecinNom = null;
        if (c.getMedecin() != null) {
            medecinNom = c.getMedecin().getPrenom() + " " + c.getMedecin().getNom();
        }

        result.put("id", pm.getId());
        result.put("numero", pm.getNumeroOrdonnance());
        result.put("type", "PRESCRIPTION_MEDICAMENT");
        result.put("patientNom", c.getAssure().getNom());
        result.put("patientPrenom", c.getAssure().getPrenom());
        result.put("patientPolice", c.getAssure().getNumeroPolice());
        result.put("montantTotal", pm.getPrixTotal());
        result.put("montantPrisEnCharge", pm.getMontantPrisEnCharge());
        result.put("montantTicketModerateur", pm.getMontantTicketModerateur());
        result.put("statut", pm.getDelivre() ? "DELIVRE" : "EN_ATTENTE");
        result.put("validationUab", pm.getValidationUabBool());
        result.put("dateCreation", pm.getDatePrescription());
        result.put("codeInte", c.getCodeInte());
        result.put("codeRisq", c.getCodeRisq());
        result.put("motifRejet", pm.getMotifRejet());
        result.put("structureNom", pm.getPharmacie() != null ? pm.getPharmacie().getNom() : null);
        result.put("structureId", pm.getPharmacie() != null ? pm.getPharmacie().getId() : null);
        result.put("origine", "PHARMACIE");
        result.put("medecinNom", medecinNom);
        result.put("medicamentNom", pm.getMedicamentNom());
        result.put("medicamentDosage", pm.getMedicamentDosage());
        result.put("medicamentForme", pm.getMedicamentForme());
        result.put("quantite", pm.getQuantitePrescitee());
        result.put("quantitePrescitee", pm.getQuantitePrescitee());
        result.put("quantiteDelivree", pm.getQuantiteDelivree());
        result.put("delivre", pm.getDelivre());
        result.put("instructions", pm.getInstructions());

        return result;
    }


}
