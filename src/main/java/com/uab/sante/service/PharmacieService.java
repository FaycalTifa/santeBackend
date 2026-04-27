// service/PharmacieService.java
package com.uab.sante.service;

import com.uab.sante.dto.request.PharmacieDelivranceRequestDTO;
import com.uab.sante.dto.response.ConsultationResponseDTO;
import com.uab.sante.entities.*;
import com.uab.sante.repository.PrescriptionMedicamentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PharmacieService {

    private final PrescriptionMedicamentRepository prescriptionMedicamentRepository;

    /**
     * Récupérer les prescriptions en attente pour une pharmacie
     */
    public List<PrescriptionMedicament> getPrescriptionsEnAttente(Long pharmacieId) {
        return prescriptionMedicamentRepository.findByPharmacieIdAndDelivreFalse(pharmacieId);
    }

    /**
     * ✅ NOUVELLE MÉTHODE : Rechercher des prescriptions par CODEINTE, police et code risque
     */
    public List<PrescriptionMedicament> rechercherParCriteres(String numPolice, String codeInte, String codeRisq, String codeMemb) {
        System.out.println("=== RECHERCHE PRESCRIPTIONS PAR CRITÈRES (UNIQUEMENT NON DÉLIVRÉES) ===");
        System.out.println("numPolice: " + numPolice);
        System.out.println("codeInte: " + codeInte);
        System.out.println("codeRisq: " + codeRisq);
        System.out.println("codeMemb: " + codeMemb);

        // Récupérer toutes les prescriptions correspondant aux critères
        List<PrescriptionMedicament> prescriptions = prescriptionMedicamentRepository.findByCriteres(numPolice, codeInte, codeRisq, codeMemb);

        // ✅ FILTRAGE : Garder uniquement les prescriptions NON DÉLIVRÉES (delivre = false)
        List<PrescriptionMedicament> prescriptionsNonDelivrees = prescriptions.stream()
                .filter(p -> p.getDelivre() == null || !p.getDelivre())  // Uniquement les non délivrées
                .collect(Collectors.toList());

        System.out.println("=== RÉSULTAT DU FILTRAGE ===");
        System.out.println("Total prescriptions trouvées: " + prescriptions.size());
        System.out.println("Prescriptions NON DÉLIVRÉES (affichées): " + prescriptionsNonDelivrees.size());
        System.out.println("Prescriptions DÉLIVRÉES (exclues): " + (prescriptions.size() - prescriptionsNonDelivrees.size()));

        for (PrescriptionMedicament p : prescriptionsNonDelivrees) {
            System.out.println("  ✅ Prescription ID: " + p.getId() +
                    ", Médicament: " + p.getMedicamentNom() +
                    ", Délivrée: " + p.getDelivre() +
                    ", Quantité: " + p.getQuantitePrescitee());
        }

        return prescriptionsNonDelivrees;
    }
    /**
     * Récupérer les prescriptions par numéro de police
     */
    public List<PrescriptionMedicament> getPrescriptionsByPoliceNonLivre(String numeroPolice) {
        return prescriptionMedicamentRepository.findByConsultationAssureNumeroPoliceAndDelivreFalse(numeroPolice);
    }

    /**
     * Récupérer TOUTES les prescriptions (délivrées et non délivrées) par numéro de police
     */
    public List<PrescriptionMedicament> getPrescriptionsByPolice(String numeroPolice) {
        return prescriptionMedicamentRepository.findByConsultationAssureNumeroPolice(numeroPolice);
    }

    /**
     * Récupérer l'historique des délivrances
     */
    public List<PrescriptionMedicament> getHistoriqueDelivrances(Long pharmacieId) {
        return prescriptionMedicamentRepository.findByPharmacieIdAndDelivreTrue(pharmacieId);
    }

    /**
     * Récupérer une prescription par ID
     */
    public PrescriptionMedicament getPrescriptionById(Long id) {
        return prescriptionMedicamentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prescription non trouvée avec l'id: " + id));
    }

    /**
     * Récupérer TOUTES les prescriptions (délivrées et non délivrées) pour une pharmacie
     */
    public List<PrescriptionMedicament> getAllPrescriptions(Long pharmacieId) {
        return prescriptionMedicamentRepository.findByPharmacieId(pharmacieId);
    }

    /**
     * Délivrer un médicament
     */
    @Transactional
    public PrescriptionMedicament delivrerMedicament(Long prescriptionId, PharmacieDelivranceRequestDTO request, Utilisateur pharmacien) {

        System.out.println("=== DÉLIVRANCE MÉDICAMENT ===");
        System.out.println("Prescription ID: " + prescriptionId);
        System.out.println("Pharmacien: " + pharmacien.getEmail());

        PrescriptionMedicament prescription = prescriptionMedicamentRepository.findById(prescriptionId)
                .orElseThrow(() -> new RuntimeException("Prescription non trouvée"));

        if (prescription.getDelivre()) {
            throw new RuntimeException("Ce médicament a déjà été délivré");
        }

        if (request.getQuantiteDelivree() > prescription.getQuantitePrescitee()) {
            throw new RuntimeException("La quantité délivrée ne peut pas dépasser la quantité prescrite");
        }

        Consultation consultation = prescription.getConsultation();
        Double taux = consultation.getTauxCouverture();

        double prixTotal = request.getPrixUnitaire() * request.getQuantiteDelivree();
        double montantPrisEnCharge = prixTotal * (taux / 100);
        double montantTicketModerateur = prixTotal - montantPrisEnCharge;

        prescription.setPrixUnitaire(request.getPrixUnitaire());
        prescription.setPrixTotal(prixTotal);
        prescription.setQuantiteDelivree(request.getQuantiteDelivree());
        prescription.setDelivre(true);
        prescription.setValidationUabBool(true);
        prescription.setPharmacie(pharmacien.getStructure());
        prescription.setPharmacien(pharmacien);
        prescription.setDateDelivrance(LocalDate.now());
        prescription.setMontantPrisEnCharge(montantPrisEnCharge);
        prescription.setMontantTicketModerateur(montantTicketModerateur);
        prescription.setMontantPayePatient(montantTicketModerateur);

        PrescriptionMedicament saved = prescriptionMedicamentRepository.save(prescription);

        System.out.println("✅ Médicament délivré avec succès");
        return saved;
    }

    /**
     * Convertir en DTO
     */
    public ConsultationResponseDTO.PrescriptionMedicamentResponseDTO toDTO(PrescriptionMedicament prescription) {
        Consultation consultation = prescription.getConsultation();

        if (consultation == null || consultation.getAssure() == null) {
            System.err.println("Erreur: consultation ou assure null pour prescription ID: " + prescription.getId());
            return ConsultationResponseDTO.PrescriptionMedicamentResponseDTO.builder()
                    .id(prescription.getId())
                    .numeroOrdonnance(prescription.getNumeroOrdonnance())
                    .build();
        }

        Assure assure = consultation.getAssure();
        Double taux = consultation.getTauxCouverture();

        return ConsultationResponseDTO.PrescriptionMedicamentResponseDTO.builder()
                .id(prescription.getId())
                .numeroOrdonnance(prescription.getNumeroOrdonnance())
                .consultationId(consultation.getId())
                .consultationNumeroFeuille(consultation.getNumeroFeuille())
                .patientNom(assure.getNom())
                .patientPrenom(assure.getPrenom())
                .patientPolice(assure.getNumeroPolice())
                // ✅ AJOUTER CODEINTE ET CODERISQ
                .codeInte(consultation.getCodeInte())
                .codeRisq(consultation.getCodeRisq())
                .medicamentNom(prescription.getMedicamentNom())
                .medicamentDosage(prescription.getMedicamentDosage())
                .medicamentForme(prescription.getMedicamentForme())
                .quantitePrescitee(prescription.getQuantitePrescitee())
                .quantiteDelivree(prescription.getQuantiteDelivree())
                .instructions(prescription.getInstructions())
                .delivre(prescription.getDelivre())
                .prixUnitaire(prescription.getPrixUnitaire())
                .prixTotal(prescription.getPrixTotal())
                .tauxCouverture(taux)
                .montantTicketModerateur(prescription.getMontantTicketModerateur())
                .montantPrisEnCharge(prescription.getMontantPrisEnCharge())
                .dateDelivrance(prescription.getDateDelivrance())
                .pharmacieNom(prescription.getPharmacie() != null ? prescription.getPharmacie().getNom() : null)
                .pharmacienNom(prescription.getPharmacien() != null ?
                        prescription.getPharmacien().getPrenom() + " " + prescription.getPharmacien().getNom() : null)
                .build();
    }
}
