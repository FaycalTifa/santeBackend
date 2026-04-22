// service/PlafonnementService.java
package com.uab.sante.service;

import com.uab.sante.dto.CalculRemboursementDTO;
import com.uab.sante.dto.request.PlafonnementRequestDTO;
import com.uab.sante.entities.PlafonnementConsultation;
import com.uab.sante.repository.PlafonnementConsultationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlafonnementService {

    private final PlafonnementConsultationRepository plafonnementRepository;

    /**
     * Créer ou modifier un plafonnement
     */
    @Transactional
    public PlafonnementConsultation save(PlafonnementRequestDTO request) {
        PlafonnementConsultation plafonnement = PlafonnementConsultation.builder()
                .codeInte(request.getCodeInte())
                .typeConsultation(request.getTypeConsultation())
                .montantPlafond(request.getMontantPlafond())
                .tauxRemboursement(request.getTauxRemboursement())
                .actif(true)
                .createdAt(LocalDateTime.now())
                .build();

        return plafonnementRepository.save(plafonnement);
    }

    /**
     * Récupérer le plafonnement pour une police et un type de consultation
     */
    public PlafonnementConsultation getByCodeInteAndType(String codeInte, String typeConsultation) {
        return plafonnementRepository.findByCodeInteAndTypeConsultationAndActifTrue(codeInte, typeConsultation)
                .orElse(null);
    }

    /**
     * Récupérer tous les plafonnements d'une police
     */
    public List<PlafonnementConsultation> getByCodeInte(String codeInte) {
        return plafonnementRepository.findByCodeInteAndActifTrue(codeInte);
    }

    /**
     * Récupérer tous les plafonnements
     */
    public List<PlafonnementConsultation> getAll() {
        return plafonnementRepository.findByActifTrueOrderByCodeInte();
    }

    /**
     * Calculer le remboursement avec plafonnement
     */
    public CalculRemboursementDTO calculerRemboursement(
            String codeInte,
            String typeConsultation,
            Double montantConsultation,
            Double tauxCouverture) {

        // Récupérer le plafonnement
        PlafonnementConsultation plafonnement = getByCodeInteAndType(codeInte, typeConsultation);

        Double montantPlafond = (plafonnement != null) ? plafonnement.getMontantPlafond() : montantConsultation;
        Double tauxRemboursement = (plafonnement != null) ? plafonnement.getTauxRemboursement() : tauxCouverture;

        // Calculer le montant remboursé par UAB (sur le plafond)
        Double montantRembourseUAB = Math.min(montantConsultation, montantPlafond) * (tauxRemboursement / 100);

        // Calculer le ticket modérateur (part patient sur le plafond)
        Double ticketModerateur = Math.min(montantConsultation, montantPlafond) - montantRembourseUAB;

        // Calculer le surplus (dépassement du plafond)
        Double surplus = (montantConsultation > montantPlafond) ? (montantConsultation - montantPlafond) : 0;

        // Total à payer par le patient
        Double montantTotalPatient = ticketModerateur + surplus;

        return CalculRemboursementDTO.builder()
                .montantTotal(montantConsultation)
                .montantPlafond(montantPlafond)
                .montantRembourseUAB(montantRembourseUAB)
                .montantTicketModerateur(ticketModerateur)
                .montantSurplus(surplus)
                .montantTotalPatient(montantTotalPatient)
                .typeConsultation(typeConsultation)
                .tauxRemboursement(tauxRemboursement)
                .build();
    }

    @Transactional
    public void delete(Long id) {
        plafonnementRepository.deleteById(id);
    }

    @Transactional
    public PlafonnementConsultation update(Long id, PlafonnementRequestDTO request) {
        PlafonnementConsultation existing = plafonnementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plafonnement non trouvé"));

        existing.setMontantPlafond(request.getMontantPlafond());
        existing.setTauxRemboursement(request.getTauxRemboursement());

        return plafonnementRepository.save(existing);
    }
}
