package com.uab.sante.service;


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
    private final PrescriptionMedicamentRepository prescriptionMedicamentRepository;
    private final PrescriptionExamenRepository prescriptionExamenRepository;
    private final ExamenRepository examenRepository;
    private final MedicamentRepository medicamentRepository;
    private final NumberGenerator numberGenerator;

    // ÉTAPE 1: Caisse hôpital - Créer consultation avec paiement
    // ConsultationService.java
    @Transactional
    public Consultation createByCaisse(ConsultationCaisseRequestDTO request, UserDetails userDetails) {
        System.out.println("=== CRÉATION CONSULTATION PAR CAISSE ===");

        // 1. Récupérer l'utilisateur (caissier)
        Utilisateur caissier = getCaissier(userDetails);

        // 2. Créer ou récupérer l'assuré
        Assure assure = getOrCreateAssure(request);

        // 3. Récupérer le taux sélectionné
        TauxCouverture taux = tauxCouvertureRepository.findById(request.getTauxId())
                .orElseThrow(() -> new RuntimeException("Taux de couverture non trouvé"));

        System.out.println("Taux sélectionné: " + taux.getTauxPourcentage() + "% - " + taux.getLibelle());

        // 4. Calculer les montants
        double totalHospitalier = request.getPrixConsultation() +
                (request.getPrixActes() != null ? request.getPrixActes() : 0);
        double prisEnCharge = totalHospitalier * (taux.getTauxPourcentage() / 100);
        double ticketModerateur = totalHospitalier - prisEnCharge;

        System.out.println("Total: " + totalHospitalier);
        System.out.println("Pris en charge UAB: " + prisEnCharge);
        System.out.println("Ticket modérateur patient: " + ticketModerateur);

        // 5. Créer la consultation
        Consultation consultation = Consultation.builder()
                .numeroFeuille(numberGenerator.generateNumeroFeuille())
                .assure(assure)
                .dateConsultation(request.getDateConsultation())
                .prixConsultation(request.getPrixConsultation())
                .prixActes(request.getPrixActes())
                .montantTotalHospitalier(totalHospitalier)
                .tauxCouverture(taux.getTauxPourcentage())
                .montantPrisEnCharge(prisEnCharge)
                .montantTicketModerateur(ticketModerateur)
                .montantPayePatient(ticketModerateur)
                .structure(caissier.getStructure())
                .statut("PAYEE_CAISSE")
                .dateTransmission(LocalDate.now())
                .build();

        Consultation saved = consultationRepository.save(consultation);
        System.out.println("✅ Consultation créée avec ID: " + saved.getId());
        System.out.println("Numéro feuille: " + saved.getNumeroFeuille());

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
    private Assure getOrCreateAssure(ConsultationCaisseRequestDTO request) {
        return assureRepository.findByNumeroPolice(request.getNumeroPolice())
                .orElseGet(() -> {
                    System.out.println("Création d'un nouvel assuré pour police: " + request.getNumeroPolice());
                    Assure newAssure = Assure.builder()
                            .numeroPolice(request.getNumeroPolice())
                            .nom(request.getNomPatient())
                            .prenom(request.getPrenomPatient())
                            .telephone(request.getTelephonePatient())
                            .dateNaissance(request.getDateNaissance())
                            .statut("ACTIF")
                            .build();
                    return assureRepository.save(newAssure);
                });
    }

    /**
     * Convertir en DTO
     */
    public ConsultationResponseDTO toDTO(Consultation consultation) {
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
                .statut(consultation.getStatut())                    // ✅ Ajouter
                .validationUab(consultation.getValidationUab())      // ✅
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

    /**
     * Récupérer les consultations en attente de prescription pour un médecin
     */
    public List<Consultation> getConsultationsEnAttentePrescription(UserDetails userDetails) {
        System.out.println("=== GET CONSULTATIONS EN ATTENTE ===");

        Utilisateur medecin;

        if (userDetails == null) {
            System.out.println("⚠️ UserDetails is null! Using default doctor");
            medecin = utilisateurRepository.findByEmail("dr.kone@csm.ci")
                    .orElseThrow(() -> new RuntimeException("Médecin par défaut non trouvé"));
        } else {
            medecin = utilisateurRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé: " + userDetails.getUsername()));
        }

        System.out.println("Médecin: " + medecin.getEmail());

        List<Consultation> consultations = consultationRepository.findByMedecinIdAndStatutInOrderByDateConsultationDesc(
                medecin.getId(), List.of("PAYEE_CAISSE"));

        System.out.println("Nombre de consultations en attente: " + consultations.size());

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
        System.out.println("Avant validation - validationUab: " + consultation.getValidationUab());

        consultation.setValidationUab(true);
        consultation.setValidationUabDate(LocalDateTime.now());
        consultation.setValidationUabPar(uabAdmin);
        consultation.setStatut("VALIDEE_UAB");

        Consultation saved = consultationRepository.save(consultation);

        // ✅ Après validation
        System.out.println("Après validation - statut: " + saved.getStatut());
        System.out.println("Après validation - validationUab: " + saved.getValidationUab());

        return saved;
    }

    @Transactional
    public Consultation rejeter(Long consultationId, String motif, UserDetails userDetails) {
        Utilisateur uabAdmin = utilisateurRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        Consultation consultation = getById(consultationId);
        consultation.setValidationUab(false);
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

                // ✅ CRÉER OU RÉCUPÉRER LE MÉDICAMENT DANS LE RÉFÉRENTIEL
                Medicament medicament;

                if (p.getMedicamentId() != null) {
                    // Cas 1: Le médecin a sélectionné un médicament existant
                    medicament = medicamentService.getById(p.getMedicamentId());
                } else {
                    // Cas 2: Le médecin a saisi un nouveau médicament
                    // On le crée automatiquement dans la table medicaments
                    System.out.println("⚠️ Nouveau médicament détecté, création dans le référentiel: " + p.getMedicamentNom());

                    medicament = Medicament.builder()
                            .nom(p.getMedicamentNom())
                            .dosage(p.getDosage())
                            .forme(p.getForme())
                            .actif(true)
                            .build();

                    medicament = medicamentRepository.save(medicament);
                    System.out.println("✅ Nouveau médicament créé avec ID: " + medicament.getId());
                }

                // Créer la prescription
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

        // 6. Ajouter les prescriptions examens (même logique)
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

                prescriptionExamenRepository.save(prescription);
                consultation.getPrescriptionsExamens().add(prescription);
            }
        }

        consultation.setPrescriptionsValidees(true);
        consultation.setStatut("PRESCRIPTIONS_FAITES");

        return consultationRepository.save(consultation);
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
