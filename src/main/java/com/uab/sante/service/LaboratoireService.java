package com.uab.sante.service;

import com.uab.sante.dto.ResultatExamenDTO;
import com.uab.sante.dto.request.LaboratoireRealisationRequestDTO;
import com.uab.sante.dto.request.PaiementLaboratoireRequestDTO;
import com.uab.sante.dto.response.PrescriptionExamenResponseDTO;
import com.uab.sante.dto.response.ResultatExamenResponseDTO;
import com.uab.sante.entities.*;
import com.uab.sante.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LaboratoireService {

    private final PrescriptionExamenRepository prescriptionExamenRepository;
    private final ResultatExamenRepository resultatExamenRepository;
    private final ConsultationRepository consultationRepository;
    private final UtilisateurRepository utilisateurRepository;

    /**
     * Récupérer les examens en attente pour un laboratoire
     */
    public List<PrescriptionExamen> getExamensEnAttente(Long laboratoireId) {
        return prescriptionExamenRepository.findByLaboratoireIdAndRealiseFalse(laboratoireId);
    }

    /**
     * Récupérer les examens par numéro de police
     */
    public List<PrescriptionExamen> getExamensByPolice(String numeroPolice) {
        return prescriptionExamenRepository.findByConsultationAssureNumeroPoliceAndRealiseFalse(numeroPolice);
    }

    /**
     * Récupérer les examens réalisés (historique)
     */
    public List<PrescriptionExamen> getExamensRealises(Long laboratoireId) {
        return prescriptionExamenRepository.findByLaboratoireIdAndRealiseTrue(laboratoireId);
    }

    /**
     * Récupérer une prescription d'examen par ID
     */
    public PrescriptionExamen getPrescriptionExamenById(Long id) {
        return prescriptionExamenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Examen non trouvé"));
    }



    /**
     * Récupérer le taux de couverture d'un examen
     */
    public Double getTauxCouverture(Long prescriptionId) {
        PrescriptionExamen prescription = getPrescriptionExamenById(prescriptionId);
        Consultation consultation = prescription.getConsultation();
        return consultation.getTauxCouverture();
    }

    /**
     * Réaliser un examen (caisse laboratoire + biologiste)
     */
// service/LaboratoireService.java - Modifier realiserExamen

    @Transactional
    public PrescriptionExamen realiserExamen(Long prescriptionId, LaboratoireRealisationRequestDTO request, Utilisateur biologiste) {

        System.out.println("=== RÉALISATION D'EXAMEN ===");
        System.out.println("Prescription ID: " + prescriptionId);
        System.out.println("Biologiste: " + biologiste.getEmail());

        // 1. Récupérer la prescription
        PrescriptionExamen prescription = prescriptionExamenRepository.findById(prescriptionId)
                .orElseThrow(() -> new RuntimeException("Examen non trouvé"));

        // ✅ VÉRIFICATION CRITIQUE - Validation UAB requise
        if (!"OUI".equals(prescription.getValidationUab())) {
            String statut = prescription.getValidationUab() == null ? "NON_DEFINI" : prescription.getValidationUab();
            throw new RuntimeException("❌ Cet examen n'a pas été validé par l'UAB. Réalisation impossible. Statut actuel: " + statut);
        }

        // Vérifier que l'examen a été payé
        if (!prescription.getPaye()) {
            throw new RuntimeException("L'examen doit d'abord être payé avant réalisation");
        }

        // Vérifier que l'examen n'est pas déjà réalisé
        if (prescription.getRealise()) {
            throw new RuntimeException("Cet examen a déjà été réalisé");
        }

        // 2. Vérifier que l'examen a été payé
        if (!prescription.getPaye()) {
            throw new RuntimeException("L'examen doit d'abord être payé avant réalisation");
        }

        // 3. Vérifier que l'examen n'est pas déjà réalisé
        if (prescription.getRealise()) {
            throw new RuntimeException("Cet examen a déjà été réalisé");
        }

        // 4. Récupérer la consultation pour le taux
        Consultation consultation = prescription.getConsultation();
        Double taux = consultation.getTauxCouverture();

        // 5. Saisir les informations de réalisation
        prescription.setRealise(true);
        prescription.setLaboratoire(biologiste.getStructure());
        prescription.setBiologiste(biologiste);
        prescription.setDateRealisation(LocalDate.now());

        // 6. Ajouter les résultats détaillés
        if (request.getResultats() != null && !request.getResultats().isEmpty()) {
            for (ResultatExamenDTO resultatDTO : request.getResultats()) {
                ResultatExamen resultat = ResultatExamen.builder()
                        .prescriptionExamen(prescription)
                        .parametre(resultatDTO.getParametre())
                        .valeur(resultatDTO.getValeur())
                        .unite(resultatDTO.getUnite())
                        .valeurNormaleMin(resultatDTO.getValeurNormaleMin())
                        .valeurNormaleMax(resultatDTO.getValeurNormaleMax())
                        .anormal(estAnormal(resultatDTO))
                        .build();
                resultatExamenRepository.save(resultat);

                if (prescription.getResultatsList() == null) {
                    prescription.setResultatsList(new ArrayList<>());
                }
                prescription.getResultatsList().add(resultat);
            }
        }

        // 7. Sauvegarder
        PrescriptionExamen saved = prescriptionExamenRepository.save(prescription);
        System.out.println("✅ Examen réalisé avec succès");

        return saved;
    }

    // service/LaboratoireService.java - Ajouter ces méthodes

    /**
     * Récupérer les examens validés par UAB mais non payés
     */
    public List<PrescriptionExamen> getExamensValidesNonPayes(Long laboratoireId) {
        return prescriptionExamenRepository.findByLaboratoireIdAndValidationUabAndPayeFalseAndRealiseFalse(laboratoireId, "OUI");
    }

    /**
     * Récupérer les examens payés non réalisés
     */
    public List<PrescriptionExamen> getExamensPayesNonRealises(Long laboratoireId) {
        return prescriptionExamenRepository.findByLaboratoireIdAndPayeTrueAndRealiseFalse(laboratoireId);
    }

    // service/LaboratoireService.java - Ajouter cette méthode

    // service/LaboratoireService.java
    /**
     * Rechercher des examens par CODEINTE, police, code risque et codeMemb (optionnel)
     */
    public List<PrescriptionExamen> rechercherParCriteres(String numPolice, String codeInte, String codeRisq, String codeMemb) {
        System.out.println("=== RECHERCHE EXAMENS PAR CRITÈRES ===");
        System.out.println("numPolice: " + numPolice);
        System.out.println("codeInte: " + codeInte);
        System.out.println("codeRisq: " + codeRisq);
        System.out.println("codeMemb: " + codeMemb);

        // ✅ Appel avec codeMemb
        List<PrescriptionExamen> examens = prescriptionExamenRepository.findByCriteres(numPolice, codeInte, codeRisq, codeMemb);

        // Afficher les statuts pour debug
        for (PrescriptionExamen e : examens) {
            System.out.println("Examen ID: " + e.getId() +
                    ", Nom: " + e.getExamenNom() +
                    ", validationUab: " + e.getValidationUab() +
                    ", paye: " + e.getPaye());
        }

        return examens;
    }


    // service/LaboratoireService.java

    /**
     * Récupérer les examens en attente de paiement pour un laboratoire
     */
    public List<PrescriptionExamen> getExamensEnAttentePaiement(Long laboratoireId) {
        // ✅ Filtrer par validationUab = 'OUI' ET paye = false ET realise = false
        return prescriptionExamenRepository.findByLaboratoireIdAndValidationUabAndPayeFalseAndRealiseFalse(laboratoireId, "OUI");
    }
    /**
     * Récupérer les examens payés en attente de réalisation
     */
    public List<PrescriptionExamen> getExamensPayesEnAttenteRealisation(Long laboratoireId) {
        return prescriptionExamenRepository.findByLaboratoireIdAndPayeTrueAndRealiseFalse(laboratoireId);
    }

    /**
     * Vérifier si une valeur est anormale
     */
    private boolean estAnormal(ResultatExamenDTO resultat) {
        if (resultat.getValeur() == null || resultat.getValeurNormaleMin() == null || resultat.getValeurNormaleMax() == null) {
            return false;
        }
        try {
            double valeur = Double.parseDouble(resultat.getValeur());
            double min = Double.parseDouble(resultat.getValeurNormaleMin());
            double max = Double.parseDouble(resultat.getValeurNormaleMax());
            return valeur < min || valeur > max;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Convertir en DTO
     */
    /**
     * Convertir en DTO
     */
// LaboratoireService.java
    public PrescriptionExamenResponseDTO toDTO(PrescriptionExamen prescription) {
        Consultation consultation = prescription.getConsultation();

        // ✅ Vérifiez que consultation et assure ne sont pas null
        if (consultation == null || consultation.getAssure() == null) {
            System.err.println("Erreur: consultation ou assure null pour prescription ID: " + prescription.getId());
            return PrescriptionExamenResponseDTO.builder()
                    .id(prescription.getId())
                    .numeroBulletin(prescription.getNumeroBulletin())
                    .build();
        }

        Assure assure = consultation.getAssure();

        System.out.println("=== MAPPING PRESCRIPTION EXAMEN ===");
        System.out.println("Prescription ID: " + prescription.getId());
        System.out.println("Patient Nom: " + assure.getNom());
        System.out.println("Patient Prenom: " + assure.getPrenom());
        System.out.println("Patient Police: " + assure.getNumeroPolice());

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
                // ✅ Ces trois champs sont essentiels
                .patientNom(assure.getNom())
                .patientPrenom(assure.getPrenom())
                .patientPolice(assure.getNumeroPolice())
                .examenNom(prescription.getExamenNom())
                .codeActe(prescription.getCodeActe())
                .instructions(prescription.getInstructions())
                .realise(prescription.getRealise())
                .paye(prescription.getPaye())  // ✅ Ajouter ce champ
                .codeInte(consultation.getCodeInte())      // CODEINTE de la consultation
                .codeRisq(consultation.getCodeRisq())      // CODERISQ de la consultation
                .validationUab(prescription.getValidationUab())  // ✅ IMPORTANT
                .motifRejet(prescription.getMotifRejet())        // ✅
                .prixTotal(prescription.getPrixTotal())
                .montantTicketModerateur(prescription.getMontantTicketModerateur())
                .montantPrisEnCharge(prescription.getMontantPrisEnCharge())
                .dateRealisation(prescription.getDateRealisation())
                .datePaiement(prescription.getDatePaiement())
                .laboratoireNom(prescription.getLaboratoire() != null ? prescription.getLaboratoire().getNom() : null)
                .biologisteNom(prescription.getBiologiste() != null ?
                        prescription.getBiologiste().getPrenom() + " " + prescription.getBiologiste().getNom() : null)
                .resultats(resultatsDTO)
                .interpretation(prescription.getInterpretation())
                .tauxCouverture(consultation.getTauxCouverture())
                .dateInterpretation(prescription.getDateInterpretation())
                .medecinInterpretationNom(prescription.getMedecinInterpretation() != null ?
                        prescription.getMedecinInterpretation().getPrenom() + " " + prescription.getMedecinInterpretation().getNom() : null)
                .build();
    }
    // service/LaboratoireService.java - Ajouter cette méthode

    /**
     * Enregistrer le paiement d'un examen (caisse laboratoire)
     */
    @Transactional
    public PrescriptionExamen enregistrerPaiement(PaiementLaboratoireRequestDTO request, Utilisateur caissier) {

        System.out.println("=== ENREGISTREMENT PAIEMENT LABORATOIRE ===");
        System.out.println("Prescription ID: " + request.getPrescriptionId());
        System.out.println("Prix total: " + request.getPrixTotal());
        System.out.println("Mode paiement: " + request.getModePaiement());
        System.out.println("Caissier: " + caissier.getEmail());

        // 1. Récupérer la prescription
        PrescriptionExamen prescription = prescriptionExamenRepository.findById(request.getPrescriptionId())
                .orElseThrow(() -> new RuntimeException("Examen non trouvé"));

        // ✅ VÉRIFICATION CRITIQUE - Validation UAB requise
        if (!"OUI".equals(prescription.getValidationUab())) {
            String statut = prescription.getValidationUab() == null ? "NON_DEFINI" : prescription.getValidationUab();
            throw new RuntimeException("❌ Cet examen n'a pas encore été validé par l'UAB. Paiement impossible. Statut actuel: " + statut);
        }

        // Vérifier que l'examen n'est pas déjà payé
        if (prescription.getPaye()) {
            throw new RuntimeException("Cet examen a déjà été payé");
        }

        // Vérifier que l'examen n'est pas déjà réalisé
        if (prescription.getRealise()) {
            throw new RuntimeException("Cet examen a déjà été réalisé");
        }


        // 2. Récupérer la consultation
     //   Consultation consultation = prescription.getConsultation();



        // 2. Vérifier que l'examen n'est pas déjà payé
        if (prescription.getPaye()) {
            throw new RuntimeException("Cet examen a déjà été payé");
        }

        // 3. Vérifier que l'examen n'est pas déjà réalisé
        if (prescription.getRealise()) {
            throw new RuntimeException("Cet examen a déjà été réalisé");
        }

        // 4. Récupérer la consultation pour le taux
        Consultation consultation = prescription.getConsultation();
        Double taux = consultation.getTauxCouverture();
        // ✅ LOGS POUR VÉRIFIER LE TAUX
        System.out.println("=== INFORMATIONS CONSULTATION ===");
        System.out.println("Consultation ID: " + consultation.getId());
        System.out.println("Numéro feuille: " + consultation.getNumeroFeuille());
        System.out.println("Taux couverture stocké: " + consultation.getTauxCouverture() + "%");
        System.out.println("Montant total hospitalier: " + consultation.getMontantTotalHospitalier());
        System.out.println("Montant pris en charge UAB: " + consultation.getMontantPrisEnCharge());
        System.out.println("Ticket modérateur patient: " + consultation.getMontantTicketModerateur());


        // 5. Calculer les montants
        double prixTotal = request.getPrixTotal();
        double montantPrisEnCharge = prixTotal * (taux / 100);
        double montantTicketModerateur = prixTotal - montantPrisEnCharge;

        // 6. Mettre à jour la prescription
        prescription.setPrixTotal(prixTotal);
        prescription.setMontantPrisEnCharge(montantPrisEnCharge);
        prescription.setMontantTicketModerateur(montantTicketModerateur);
        prescription.setMontantPayePatient(montantTicketModerateur);
        prescription.setPaye(true);
        prescription.setDatePaiement(LocalDate.now());

        // 7. Sauvegarder
        PrescriptionExamen saved = prescriptionExamenRepository.save(prescription);

        System.out.println("✅ Paiement enregistré avec succès");
        System.out.println("Montant encaissé: " + montantTicketModerateur + " FCFA");
        System.out.println("UAB remboursera: " + montantPrisEnCharge + " FCFA");

        return saved;
    }
}
