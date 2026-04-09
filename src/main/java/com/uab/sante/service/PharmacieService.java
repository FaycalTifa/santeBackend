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
     * ✅ AJOUTER CETTE MÉTHODE - Récupérer les prescriptions par numéro de police
     */
    public List<PrescriptionMedicament> getPrescriptionsByPoliceNonLivre(String numeroPolice) {
        return prescriptionMedicamentRepository.findByConsultationAssureNumeroPoliceAndDelivreFalse(numeroPolice);
    }

    /**
     * ✅ Récupérer TOUTES les prescriptions (délivrées et non délivrées) par numéro de police
     */
    public List<PrescriptionMedicament> getPrescriptionsByPolice(String numeroPolice) {
        // Ne pas filtrer sur delivre=false pour afficher toutes les prescriptions
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

        // 1. Récupérer la prescription
        PrescriptionMedicament prescription = prescriptionMedicamentRepository.findById(prescriptionId)
                .orElseThrow(() -> new RuntimeException("Prescription non trouvée"));

        // 2. Vérifier que le médicament n'est pas déjà délivré
        if (prescription.getDelivre()) {
            throw new RuntimeException("Ce médicament a déjà été délivré");
        }

        // 3. Vérifier la quantité
        if (request.getQuantiteDelivree() > prescription.getQuantitePrescitee()) {
            throw new RuntimeException("La quantité délivrée ne peut pas dépasser la quantité prescrite");
        }

        // 4. Récupérer la consultation pour le taux
        Consultation consultation = prescription.getConsultation();
        Double taux = consultation.getTauxCouverture();

        // ✅ LOGS POUR VÉRIFIER LE TAUX
        System.out.println("=== INFORMATIONS CONSULTATION ===");
        System.out.println("Consultation ID: " + consultation.getId());
        System.out.println("Taux couverture stocké: " + consultation.getTauxCouverture() + "%");

        // 5. Calculer les montants
        double prixTotal = request.getPrixUnitaire() * request.getQuantiteDelivree();
        double montantPrisEnCharge = prixTotal * (taux / 100);
        double montantTicketModerateur = prixTotal - montantPrisEnCharge;

        // 6. Mettre à jour la prescription
        prescription.setPrixUnitaire(request.getPrixUnitaire());
        prescription.setPrixTotal(prixTotal);
        prescription.setQuantiteDelivree(request.getQuantiteDelivree());
        prescription.setDelivre(true);
        prescription.setPharmacie(pharmacien.getStructure());
        prescription.setPharmacien(pharmacien);
        prescription.setDateDelivrance(LocalDate.now());
        prescription.setMontantPrisEnCharge(montantPrisEnCharge);
        prescription.setMontantTicketModerateur(montantTicketModerateur);
        prescription.setMontantPayePatient(montantTicketModerateur);

        // 7. Sauvegarder
        PrescriptionMedicament saved = prescriptionMedicamentRepository.save(prescription);

        System.out.println("✅ Médicament délivré avec succès");
        System.out.println("Prix total: " + prixTotal);
        System.out.println("Ticket modérateur patient: " + montantTicketModerateur);
        System.out.println("UAB rembourse: " + montantPrisEnCharge);

        return saved;
    }

    /**
     * Convertir en DTO
     */
    // PharmacieService.java
    public ConsultationResponseDTO.PrescriptionMedicamentResponseDTO toDTO(PrescriptionMedicament prescription) {
        Consultation consultation = prescription.getConsultation();

        // ✅ Vérifiez que consultation et assure ne sont pas null
        if (consultation == null || consultation.getAssure() == null) {
            System.err.println("Erreur: consultation ou assure null pour prescription ID: " + prescription.getId());
            return ConsultationResponseDTO.PrescriptionMedicamentResponseDTO.builder()
                    .id(prescription.getId())
                    .numeroOrdonnance(prescription.getNumeroOrdonnance())
                    .build();
        }

        Assure assure = consultation.getAssure();
        Double taux = consultation.getTauxCouverture();
        System.out.println("=== MAPPING PRESCRIPTION MEDICAMENT ===");
        System.out.println("Prescription ID: " + prescription.getId());
        System.out.println("Patient Nom: " + assure.getNom());
        System.out.println("Patient Prenom: " + assure.getPrenom());
        System.out.println("Patient Police: " + assure.getNumeroPolice());
        System.out.println("Taux de couverture de la consultation: " + taux);

        return ConsultationResponseDTO.PrescriptionMedicamentResponseDTO.builder()
                .id(prescription.getId())
                .numeroOrdonnance(prescription.getNumeroOrdonnance())
                .consultationId(consultation.getId())
                .consultationNumeroFeuille(consultation.getNumeroFeuille())
                // ✅ Ces trois champs sont essentiels
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
                .tauxCouverture(taux)  // ✅ Ajouter cette ligne
                .montantTicketModerateur(prescription.getMontantTicketModerateur())
                .montantPrisEnCharge(prescription.getMontantPrisEnCharge())
                .dateDelivrance(prescription.getDateDelivrance())
                .tauxCouverture(consultation.getTauxCouverture())
                .pharmacieNom(prescription.getPharmacie() != null ? prescription.getPharmacie().getNom() : null)
                .pharmacienNom(prescription.getPharmacien() != null ?
                        prescription.getPharmacien().getPrenom() + " " + prescription.getPharmacien().getNom() : null)
                .build();
    }
}
