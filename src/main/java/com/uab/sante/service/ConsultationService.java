package com.uab.sante.service;


import com.uab.sante.dto.CalculRemboursementDTO;
import com.uab.sante.dto.PrescriptionExamenDTO;
import com.uab.sante.dto.PrescriptionMedicamentDTO;
import com.uab.sante.dto.request.ConsultationCaisseRequestDTO;
import com.uab.sante.dto.request.ConsultationPrescriptionRequestDTO;
import com.uab.sante.dto.request.LaboratoireRealisationRequestDTO;
import com.uab.sante.dto.request.PharmacieDelivranceRequestDTO;
import com.uab.sante.dto.response.ConsultationResponseDTO;
import com.uab.sante.dto.response.PrescriptionExamenResponseDTO;
import com.uab.sante.dto.response.ResultatExamenResponseDTO;
import com.uab.sante.entities.*;
import com.uab.sante.exception.ResourceNotFoundException;
import com.uab.sante.repository.*;
import com.uab.sante.utils.NumberGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConsultationService {

    private final ConsultationRepository consultationRepository;
    private final AssureRepository assureRepository;
    private final StructureRepository structureRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final TauxCouvertureRepository tauxCouvertureRepository;
    private final MedicamentService medicamentService;
    private final ExamenService examenService;
    private final PlafonnementService plafonnementService;
    private final PrescriptionMedicamentRepository prescriptionMedicamentRepository;
    private final PrescriptionExamenRepository prescriptionExamenRepository;
    private final ExamenRepository examenRepository;
    private final MedicamentRepository medicamentRepository;
    private final NumberGenerator numberGenerator;


    // service/ConsultationService.java - Modifier createByCaisse

    // service/ConsultationService.java
    // service/ConsultationService.java - Dans createByCaisse()
    @Transactional
    public Consultation createByCaisse(ConsultationCaisseRequestDTO request, UserDetails userDetails) {
        System.out.println("=== CRÉATION CONSULTATION PAR CAISSE ===");

        Utilisateur caissier = getCaissier(userDetails);
        Assure assure = getOrCreateAssure(request);
        TauxCouverture taux = tauxCouvertureRepository.findById(request.getTauxId())
                .orElseThrow(() -> new RuntimeException("Taux de couverture non trouvé"));

        // Calculer les montants
        double totalHospitalier = request.getPrixConsultation() +
                (request.getPrixActes() != null ? request.getPrixActes() : 0);
        double montantPlafond = request.getMontantPlafond() != null ? request.getMontantPlafond() : totalHospitalier;
        double tauxRemboursement = taux.getTauxPourcentage();

        double montantRembourseUAB = Math.min(totalHospitalier, montantPlafond) * (tauxRemboursement / 100);
        double ticketModerateur = Math.min(totalHospitalier, montantPlafond) - montantRembourseUAB;
        double surplus = (totalHospitalier > montantPlafond) ? (totalHospitalier - montantPlafond) : 0;
        double montantTotalPatient = ticketModerateur + surplus;

        Consultation consultation = Consultation.builder()
                .numeroFeuille(numberGenerator.generateNumeroFeuille())
                .assure(assure)
                .dateConsultation(request.getDateConsultation())
                .prixConsultation(request.getPrixConsultation())
                .prixActes(request.getPrixActes())
                .montantTotalHospitalier(totalHospitalier)
                .tauxCouverture(tauxRemboursement)
                .montantPrisEnCharge(montantRembourseUAB)
                .montantTicketModerateur(montantTotalPatient)
                .montantPayePatient(montantTotalPatient)
                .structure(caissier.getStructure())
                .statut("PAYEE_CAISSE")
                .paye(true)  // ✅ Le patient a payé à la caisse
                .validationUabBool(true)
                .dateTransmission(LocalDate.now())
                .codePres(request.getCodePres())
                .libellePres(request.getLibellePres())
                .montantPlafond(montantPlafond)
                .montantSurplus(surplus)
                .codeInte(request.getCodeInte())
                .codeRisq(request.getCodeRisq())
                .codeMemb(request.getCodeMemb())
                .numeroPolice(assure.getNumeroPolice())
                .build();

        Consultation saved = consultationRepository.save(consultation);
        System.out.println("✅ Consultation créée avec ID: " + saved.getId());
        System.out.println("Payé: " + saved.getPaye());

        return saved;
    }

    /**
     * Récupérer le caissier (avec fallback pour le développement)
     */
    private Utilisateur getCaissier(UserDetails userDetails) {
        if (userDetails == null) {
            System.out.println("⚠️ UserDetails null, utilisation de l'utilisateur par défaut");
            return utilisateurRepository.findByEmail("caisse@csm.ci")
                    .orElseThrow(() -> new RuntimeException("Utilisateur par défaut non trouvé"));
        }
        return utilisateurRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé: " + userDetails.getUsername()));
    }

    /**
     * Créer ou récupérer l'assuré
     */
    // service/ConsultationService.java

    /**
     * Créer ou récupérer l'assuré - VERSION CORRIGÉE
     */
// service/ConsultationService.java

    // service/ConsultationService.java

    /**
     * Créer un NOUVEL assuré pour chaque consultation
     * (Ne pas réutiliser les anciens enregistrements)
     */
    private Assure getOrCreateAssure(ConsultationCaisseRequestDTO request) {
        System.out.println("=== CRÉATION D'UN NOUVEL ASSURÉ POUR LA CONSULTATION ===");
        System.out.println("Numéro police: " + request.getNumeroPolice());
        System.out.println("CODEINTE: " + request.getCodeInte());
        System.out.println("CODERISQ: " + request.getCodeRisq());
        System.out.println("CODEMEMB: " + request.getCodeMemb());
        System.out.println("Nom patient: " + request.getNomPatient());
        System.out.println("Prénom patient: " + request.getPrenomPatient());

        // ✅ Toujours créer un nouvel assuré, même s'il existe déjà
        // Cela permet de garder un historique des consultations

        String typeAssure = (request.getCodeMemb() != null && !request.getCodeMemb().isEmpty())
                ? "BENEFICIAIRE"
                : "PRINCIPAL";

        Assure newAssure = Assure.builder()
                .numeroPolice(request.getNumeroPolice())
                .codeInte(request.getCodeInte())
                .codeRisq(request.getCodeRisq())
                .codeMemb(request.getCodeMemb())  // null pour principal, valeur pour bénéficiaire
                .typeAssure(typeAssure)
                .nom(request.getNomPatient())
                .prenom(request.getPrenomPatient())
                .telephone(request.getTelephonePatient() != null ? request.getTelephonePatient() : "")
                .dateNaissance(request.getDateNaissance())
                .statut("ACTIF")
                .build();

        Assure saved = assureRepository.save(newAssure);
        System.out.println("✅ Nouvel assuré créé avec ID: " + saved.getId());

        return saved;
    }
    /**
     * Convertir en DTO
     */
    public ConsultationResponseDTO toDTO(Consultation consultation) {
        // ✅ Récupérer le nom du médecin
        String medecinNomValue = null;
        if (consultation.getMedecin() != null) {
            medecinNomValue = consultation.getMedecin().getPrenom() + " " + consultation.getMedecin().getNom();
        }

        // ✅ Récupérer le nom de la structure
        String structureNomValue = null;
        if (consultation.getStructure() != null) {
            structureNomValue = consultation.getStructure().getNom();
        }

        // ✅ Convertir les prescriptions médicaments
        List<ConsultationResponseDTO.PrescriptionMedicamentResponseDTO> medocsDTO = null;
        if (consultation.getPrescriptionsMedicaments() != null && !consultation.getPrescriptionsMedicaments().isEmpty()) {
            medocsDTO = consultation.getPrescriptionsMedicaments().stream()
                    .map(this::toPrescriptionMedicamentDTO)
                    .collect(Collectors.toList());
        }


        return ConsultationResponseDTO.builder()
                .id(consultation.getId())
                .numeroFeuille(consultation.getNumeroFeuille())
                .numeroPolice(consultation.getAssure().getNumeroPolice())
                .nomPatient(consultation.getAssure().getNom())
                .prenomPatient(consultation.getAssure().getPrenom())
                .dateConsultation(consultation.getDateConsultation())
                .montantTotalHospitalier(consultation.getMontantTotalHospitalier())
                .tauxCouverture(consultation.getTauxCouverture())
                .montantPrisEnCharge(consultation.getMontantPrisEnCharge())
                .montantTicketModerateur(consultation.getMontantTicketModerateur())
                .montantPayePatient(consultation.getMontantPayePatient())
                .statut(consultation.getStatut())
                .codeInte(consultation.getCodeInte())                    // ✅ Ajouter
                .validationUab(consultation.getValidationUabBool())      // ✅
                .medecinNom(medecinNomValue)
                .structureNom(consultation.getStructure() != null ? consultation.getStructure().getNom() : null)
                .prescriptionsValidees(consultation.getPrescriptionsValidees())
                .build();
    }

    // ========== MÉTHODES EXISTANTES ==========

    public Consultation getById(Long id) {
        return consultationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consultation non trouvée"));
    }

    public List<Consultation> getByNumeroPolice(String numeroPolice) {
        return consultationRepository.findByAssureNumeroPoliceOrderByDateConsultationDesc(numeroPolice);
    }

// service/ConsultationService.java

    // service/ConsultationService.java - Remplacer la méthode existante

    /**
     * Récupérer les consultations en attente de prescription pour un médecin avec filtres
     */
    // service/ConsultationService.java
    // service/ConsultationService.java
    public List<Consultation> getConsultationsEnAttentePrescription(UserDetails userDetails,
                                                                    String numPolice,
                                                                    String codeInte,
                                                                    String codeRisq,
                                                                    String codeMemb) {
        System.out.println("=== GET CONSULTATIONS EN ATTENTE ===");
        System.out.println("Filtres - numPolice: " + numPolice);
        System.out.println("Filtres - codeInte: " + codeInte);
        System.out.println("Filtres - codeRisq: " + codeRisq);
        System.out.println("Filtres - codeMemb: " + codeMemb);

        // ✅ Récupérer le médecin et sa structure
        Utilisateur medecin = getMedecin(userDetails);
        Long structureId = medecin.getStructure() != null ? medecin.getStructure().getId() : null;

        System.out.println("Médecin: " + medecin.getEmail());
        System.out.println("Structure du médecin ID: " + structureId);
        System.out.println("Structure du médecin Nom: " + (medecin.getStructure() != null ? medecin.getStructure().getNom() : "Aucune"));

        // ✅ Vérifier que le médecin est bien rattaché à une structure
        if (structureId == null) {
            System.out.println("❌ Le médecin n'est pas rattaché à une structure");
            return new ArrayList<>();
        }

        // ✅ Récupérer les consultations de la structure du médecin
        List<Consultation> consultations = consultationRepository.findByStructureIdAndMedecinIdAndStatutInWithFilters(
                structureId,  // ✅ Filtrer par la structure du médecin
                null,         // Pas de filtre sur medecinId (pour voir toutes les consultations de la structure)
                List.of("PAYEE_CAISSE"),
                (numPolice != null && !numPolice.isEmpty()) ? numPolice : null,
                (codeInte != null && !codeInte.isEmpty()) ? codeInte : null,
                (codeRisq != null && !codeRisq.isEmpty()) ? codeRisq : null,
                (codeMemb != null && !codeMemb.isEmpty()) ? codeMemb : null);

        System.out.println("Nombre de consultations trouvées pour la structure: " + consultations.size());
        return consultations;
    }
    public List<Consultation> getAllForUAB(String statut, String numeroPolice) {
        if (numeroPolice != null && !numeroPolice.isEmpty()) {
            return consultationRepository.findByAssureNumeroPoliceOrderByDateConsultationDesc(numeroPolice);
        }
        if (statut != null && !statut.isEmpty()) {
            return consultationRepository.findByStatutOrderByDateConsultationDesc(statut);
        }
        return consultationRepository.findAll();
    }

    // ConsultationService.java - Vérifiez la méthode valider
    @Transactional
    public Consultation valider(Long consultationId, UserDetails userDetails) {
        System.out.println("=== VALIDATION CONSULTATION ===");
        System.out.println("Consultation ID: " + consultationId);

        Utilisateur uabAdmin = utilisateurRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        Consultation consultation = getById(consultationId);

        // ✅ Avant validation
        System.out.println("Avant validation - statut: " + consultation.getStatut());
        System.out.println("Avant validation - validationUab: " + consultation.getValidationUabBool());

        consultation.setValidationUabBool(true);
        consultation.setValidationUabDate(LocalDateTime.now());
        consultation.setValidationUabPar(uabAdmin);
        consultation.setStatut("VALIDEE_UAB");

        Consultation saved = consultationRepository.save(consultation);

        // ✅ Après validation
        System.out.println("Après validation - statut: " + saved.getStatut());
        System.out.println("Après validation - validationUab: " + saved.getValidationUabBool());

        return saved;
    }

    @Transactional
    public Consultation rejeter(Long consultationId, String motif, UserDetails userDetails) {
        Utilisateur uabAdmin = utilisateurRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        Consultation consultation = getById(consultationId);
        consultation.setValidationUabBool(false);
        consultation.setValidationUabDate(LocalDateTime.now());
        consultation.setValidationUabPar(uabAdmin);
        consultation.setStatut("REJETEE");
        consultation.setRemarques(motif);

        return consultationRepository.save(consultation);
    }

    // Convertir en DTO


    // service/ConsultationService.java - Modifier addPrescriptions

    @Transactional
    public Consultation addPrescriptions(Long consultationId, ConsultationPrescriptionRequestDTO request, UserDetails userDetails) {
        System.out.println("=== AJOUT DES PRESCRIPTIONS ===");

        // 1. Récupérer le médecin
        Utilisateur medecin = getMedecin(userDetails);

        // 2. Récupérer la consultation
        Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new RuntimeException("Consultation non trouvée"));

        // 3. Vérifier le statut
        if (!"PAYEE_CAISSE".equals(consultation.getStatut())) {
            throw new RuntimeException("Cette consultation n'est pas en attente de prescription");
        }

        // 4. Ajouter les informations médicales
        consultation.setMedecin(medecin);
        consultation.setNatureMaladie(request.getNatureMaladie());
        consultation.setDiagnostic(request.getDiagnostic());
        consultation.setActesMedicaux(request.getActesMedicaux());
        consultation.setDatePrescription(LocalDate.now());

        // 5. Ajouter les prescriptions médicaments
        if (request.getPrescriptionsMedicaments() != null) {
            for (PrescriptionMedicamentDTO p : request.getPrescriptionsMedicaments()) {

                Medicament medicament;
                if (p.getMedicamentId() != null) {
                    medicament = medicamentService.getById(p.getMedicamentId());
                } else {
                    System.out.println("⚠️ Nouveau médicament détecté, création dans le référentiel: " + p.getMedicamentNom());
                    medicament = Medicament.builder()
                            .nom(p.getMedicamentNom())
                            .dosage(p.getDosage())
                            .forme(p.getForme())
                            .actif(true)
                            .exclusion("NON")
                            .build();
                    medicament = medicamentRepository.save(medicament);
                    System.out.println("✅ Nouveau médicament créé avec ID: " + medicament.getId());
                }

                PrescriptionMedicament prescription = new PrescriptionMedicament();
                prescription.setNumeroOrdonnance(numberGenerator.generateNumeroOrdonnance());
                prescription.setConsultation(consultation);
                prescription.setMedicament(medicament);
                prescription.setMedicamentNom(medicament.getNom());
                prescription.setMedicamentDosage(medicament.getDosage());
                prescription.setMedicamentForme(medicament.getForme());
                prescription.setQuantitePrescitee(p.getQuantitePrescitee());
                prescription.setInstructions(p.getInstructions());
                prescription.setDatePrescription(LocalDate.now());

                prescriptionMedicamentRepository.save(prescription);
                consultation.getPrescriptionsMedicaments().add(prescription);
            }
        }

        // 6. Ajouter les prescriptions examens (VERSION CORRIGÉE - sans doublon)
        if (request.getPrescriptionsExamens() != null) {
            for (PrescriptionExamenDTO p : request.getPrescriptionsExamens()) {

                // ✅ CRÉER OU RÉCUPÉRER L'EXAMEN DANS LE RÉFÉRENTIEL
                Examen examen;
                if (p.getExamenId() != null) {
                    examen = examenService.getById(p.getExamenId());
                } else {
                    System.out.println("⚠️ Nouvel examen détecté, création dans le référentiel: " + p.getExamenNom());
                    examen = Examen.builder()
                            .nom(p.getExamenNom())
                            .code(p.getCodeActe())
                            .actif(true)
                            .validation("NON")  // Par défaut NON (pas besoin validation UAB)
                            .build();
                    examen = examenRepository.save(examen);
                    System.out.println("✅ Nouvel examen créé avec ID: " + examen.getId());
                }

                PrescriptionExamen prescription = new PrescriptionExamen();
                prescription.setNumeroBulletin(numberGenerator.generateNumeroBulletin());
                prescription.setConsultation(consultation);
                prescription.setExamen(examen);
                prescription.setExamenNom(examen.getNom());
                prescription.setCodeActe(examen.getCode());
                prescription.setInstructions(p.getInstructions());
                prescription.setDatePrescription(LocalDate.now());

                // ✅ AJOUTER CES LIGNES - GESTION DE LA VALIDATION UAB
                // Vérifier si l'examen nécessite une validation UAB
                String validationExamen = examen.getValidation();
                System.out.println("Examen: " + examen.getNom() + " - Validation requise: " + validationExamen);

                if (validationExamen != null && "OUI".equals(validationExamen)) {
                    // Examen nécessitant validation UAB
                    prescription.setValidationUab("EN_ATTENTE");  // En attente de validation
                    System.out.println("✅ Examen en attente de validation UAB");
                } else {
                    // Examen sans validation requise
                    prescription.setValidationUab("OUI");  // Directement validé
                    System.out.println("✅ Examen directement validé (pas besoin UAB)");
                }

                // Valeurs par défaut
                prescription.setPaye(false);
                prescription.setRealise(false);
                prescription.setPrixTotal(null);
                prescription.setMontantPrisEnCharge(null);
                prescription.setMontantTicketModerateur(null);
                prescription.setMontantPayePatient(null);

                prescriptionExamenRepository.save(prescription);
                consultation.getPrescriptionsExamens().add(prescription);
            }
        }

        consultation.setPrescriptionsValidees(true);
        consultation.setStatut("PRESCRIPTIONS_FAITES");

        Consultation saved = consultationRepository.save(consultation);
        System.out.println("=== FIN AJOUT DES PRESCRIPTIONS ===");
        System.out.println("Nombre de médicaments: " + consultation.getPrescriptionsMedicaments().size());
        System.out.println("Nombre d'examens: " + consultation.getPrescriptionsExamens().size());

        return saved;
    }

    /**
     * Récupérer le médecin (avec fallback)
     */
    private Utilisateur getMedecin(UserDetails userDetails) {
        if (userDetails == null) {
            return utilisateurRepository.findByEmail("dr.kone@csm.ci")
                    .orElseThrow(() -> new RuntimeException("Médecin par défaut non trouvé"));
        }
        return utilisateurRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Médecin non trouvé"));
    }

    // service/ConsultationService.java - Ajouter ces méthodes

    /**
     * Récupérer les examens réalisés pour une consultation
     */
    public List<PrescriptionExamen> getExamensRealisesByConsultation(Long consultationId) {
        return prescriptionExamenRepository.findByConsultationIdAndRealiseTrue(consultationId);
    }

    /**
     * Récupérer les examens réalisés par numéro de police
     */
    public List<PrescriptionExamen> getExamensRealisesByPolice(String numeroPolice) {
        return prescriptionExamenRepository.findByConsultationAssureNumeroPoliceAndRealiseTrue(numeroPolice);
    }

    /**
     * Récupérer tous les examens par numéro de police (sans filtre realise)
     */
    public List<PrescriptionExamen> getExamensByPolice(String numeroPolice) {
        return prescriptionExamenRepository.findByConsultationAssureNumeroPolice(numeroPolice);
    }

    /**
     * Ajouter une interprétation à un examen
     */
    @Transactional
    public PrescriptionExamen ajouterInterpretation(Long examenId, String interpretation, UserDetails userDetails) {
        Utilisateur medecin = utilisateurRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Médecin non trouvé"));

        PrescriptionExamen examen = prescriptionExamenRepository.findById(examenId)
                .orElseThrow(() -> new RuntimeException("Examen non trouvé"));

        if (!examen.getRealise()) {
            throw new RuntimeException("L'examen doit d'abord être réalisé avant interprétation");
        }

        examen.setInterpretation(interpretation);
        examen.setMedecinInterpretation(medecin);
        examen.setDateInterpretation(LocalDate.now());

        return prescriptionExamenRepository.save(examen);
    }

    public List<PrescriptionExamen> getAllExamensRealises() {
        return prescriptionExamenRepository.findByRealiseTrue();
    }

    // service/ConsultationService.java
    // service/ConsultationService.java
    /**
     * Récupérer TOUS les examens ayant fait l'objet d'une demande de validation UAB
     * (EN_ATTENTE, OUI, NON) pour un médecin avec filtres
     */
    public List<PrescriptionExamen> getDemandesValidationWithFilters(Long medecinId,
                                                                     String numPolice,
                                                                     String codeInte,
                                                                     String codeRisq,
                                                                     String codeMemb) {  // ✅ Ajouter codeMemb
        System.out.println("=== RECHERCHE TOUTES LES DEMANDES DE VALIDATION ===");
        System.out.println("Médecin ID: " + medecinId);
        System.out.println("Filtres - numPolice: " + numPolice);
        System.out.println("Filtres - codeInte: " + codeInte);
        System.out.println("Filtres - codeRisq: " + codeRisq);
        System.out.println("Filtres - codeMemb: " + codeMemb);

        return prescriptionExamenRepository.findDemandesValidationWithFilters(medecinId, numPolice, codeInte, codeRisq, codeMemb);
    }
    // service/ConsultationService.java - Ajouter ces méthodes

    /**
     * Récupérer les demandes d'examens en attente pour un médecin
     */
    public List<PrescriptionExamen> getDemandesExamenEnAttente(Long medecinId) {
        return prescriptionExamenRepository.findByConsultationMedecinIdAndValidationUab(medecinId, "EN_ATTENTE");
    }

    /**
     * Récupérer les demandes d'examens par consultation
     */
    public List<PrescriptionExamen> getDemandesExamenByConsultation(Long consultationId) {
        return prescriptionExamenRepository.findByConsultationIdAndValidationUab(consultationId, "EN_ATTENTE");
    }

    // service/ConsultationService.java - Ajouter ces méthodes de conversion

    /**
     * Convertir une prescription d'examen en DTO
     */
    public PrescriptionExamenResponseDTO toPrescriptionExamenDTO(PrescriptionExamen prescription) {
        Consultation consultation = prescription.getConsultation();
        Assure assure = consultation.getAssure();

        // Convertir les résultats
        List<ResultatExamenResponseDTO> resultatsDTO = null;
        if (prescription.getResultatsList() != null && !prescription.getResultatsList().isEmpty()) {
            resultatsDTO = prescription.getResultatsList().stream()
                    .map(r -> ResultatExamenResponseDTO.builder()
                            .id(r.getId())
                            .parametre(r.getParametre())
                            .valeur(r.getValeur())
                            .unite(r.getUnite())
                            .valeurNormaleMin(r.getValeurNormaleMin())
                            .valeurNormaleMax(r.getValeurNormaleMax())
                            .anormal(r.getAnormal())
                            .build())
                    .collect(Collectors.toList());
        }

        return PrescriptionExamenResponseDTO.builder()
                .id(prescription.getId())
                .numeroBulletin(prescription.getNumeroBulletin())
                .consultationId(consultation.getId())
                .consultationNumeroFeuille(consultation.getNumeroFeuille())
                .patientNom(assure.getNom())
                .patientPrenom(assure.getPrenom())
                .patientPolice(assure.getNumeroPolice())
                .examenNom(prescription.getExamenNom())
                .codeActe(prescription.getCodeActe())
                .instructions(prescription.getInstructions())
                .realise(prescription.getRealise())
                .prixTotal(prescription.getPrixTotal())
                .montantTicketModerateur(prescription.getMontantTicketModerateur())
                .montantPrisEnCharge(prescription.getMontantPrisEnCharge())
                .dateRealisation(prescription.getDateRealisation())
                .laboratoireNom(prescription.getLaboratoire() != null ? prescription.getLaboratoire().getNom() : null)
                .biologisteNom(prescription.getBiologiste() != null ?
                        prescription.getBiologiste().getPrenom() + " " + prescription.getBiologiste().getNom() : null)
                .resultats(resultatsDTO)
                .interpretation(prescription.getInterpretation())
                // ✅ AJOUTER CES LIGNES ESSENTIELLES
                .validationUab(prescription.getValidationUab())   // ← TRÈS IMPORTANT
                .motifRejet(prescription.getMotifRejet())
                .datePrescription(prescription.getDatePrescription())
                .medecinNom(consultation.getMedecin() != null ?
                        consultation.getMedecin().getPrenom() + " " + consultation.getMedecin().getNom() : null)
                .build();
    }

    /**
     * Convertir une prescription de médicament en DTO
     */
    public ConsultationResponseDTO.PrescriptionMedicamentResponseDTO toPrescriptionMedicamentDTO(PrescriptionMedicament prescription) {
        Consultation consultation = prescription.getConsultation();
        Assure assure = consultation.getAssure();

        return ConsultationResponseDTO.PrescriptionMedicamentResponseDTO.builder()
                .id(prescription.getId())
                .numeroOrdonnance(prescription.getNumeroOrdonnance())
                .consultationId(consultation.getId())
                .consultationNumeroFeuille(consultation.getNumeroFeuille())
                .patientNom(assure.getNom())
                .patientPrenom(assure.getPrenom())
                .patientPolice(assure.getNumeroPolice())
                .medicamentNom(prescription.getMedicamentNom())
                .medicamentDosage(prescription.getMedicamentDosage())
                .medicamentForme(prescription.getMedicamentForme())
                .quantitePrescitee(prescription.getQuantitePrescitee())
                .quantiteDelivree(prescription.getQuantiteDelivree())
                .instructions(prescription.getInstructions())
                .delivre(prescription.getDelivre())
                .prixUnitaire(prescription.getPrixUnitaire())
                .prixTotal(prescription.getPrixTotal())
                .montantTicketModerateur(prescription.getMontantTicketModerateur())
                .montantPrisEnCharge(prescription.getMontantPrisEnCharge())
                .dateDelivrance(prescription.getDateDelivrance())
                .pharmacieNom(prescription.getPharmacie() != null ? prescription.getPharmacie().getNom() : null)
                .pharmacienNom(prescription.getPharmacien() != null ?
                        prescription.getPharmacien().getPrenom() + " " + prescription.getPharmacien().getNom() : null)
                .build();
    }


}
