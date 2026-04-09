package com.uab.sante.service;

import com.uab.sante.dto.request.PoliceTauxRequestDTO;
import com.uab.sante.dto.response.PoliceTauxResponseDTO;
import com.uab.sante.entities.Assure;
import com.uab.sante.entities.PoliceTaux;
import com.uab.sante.entities.TauxCouverture;
import com.uab.sante.repository.AssureRepository;
import com.uab.sante.repository.PoliceTauxRepository;
import com.uab.sante.repository.TauxCouvertureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PoliceTauxService {

    private final PoliceTauxRepository policeTauxRepository;
    private final AssureRepository assureRepository;
    private final TauxCouvertureRepository tauxCouvertureRepository;

    /**
     * Récupérer le taux actif pour une police
     */
    public PoliceTauxResponseDTO getActiveByPolice(String numeroPolice) {
        PoliceTaux policeTaux = policeTauxRepository.findActiveByPolice(numeroPolice)
                .orElseThrow(() -> new RuntimeException("Aucun taux actif trouvé pour la police: " + numeroPolice));
        return toDTO(policeTaux);
    }

    /**
     * Récupérer tous les taux d'une police (historique)
     */
    public List<PoliceTauxResponseDTO> getHistoriqueByPolice(String numeroPolice) {
        return policeTauxRepository.findByPoliceNumeroPoliceOrderByDateDebutDesc(numeroPolice)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Associer un taux à une police
     */
    @Transactional
    public PoliceTauxResponseDTO assignerTaux(PoliceTauxRequestDTO request) {
        // Récupérer la police
        Assure police = assureRepository.findByNumeroPolice(request.getNumeroPolice())
                .orElseThrow(() -> new RuntimeException("Police non trouvée: " + request.getNumeroPolice()));

        // Récupérer le taux
        TauxCouverture taux = tauxCouvertureRepository.findById(request.getTauxId())
                .orElseThrow(() -> new RuntimeException("Taux non trouvé"));

        // Désactiver l'ancien taux actif
        policeTauxRepository.findActiveByPolice(request.getNumeroPolice())
                .ifPresent(ancien -> {
                    ancien.setActif(false);
                    ancien.setDateFin(request.getDateDebut().minusDays(1));
                    policeTauxRepository.save(ancien);
                });

        // Créer la nouvelle association
        PoliceTaux policeTaux = new PoliceTaux();
        policeTaux.setPolice(police);
        policeTaux.setTaux(taux);
        policeTaux.setDateDebut(request.getDateDebut());
        policeTaux.setDateFin(request.getDateFin());
        policeTaux.setActif(true);

        PoliceTaux saved = policeTauxRepository.save(policeTaux);
        return toDTO(saved);
    }

    /**
     * Convertir en DTO
     */
    private PoliceTauxResponseDTO toDTO(PoliceTaux policeTaux) {
        return PoliceTauxResponseDTO.builder()
                .id(policeTaux.getId())
                .numeroPolice(policeTaux.getPolice().getNumeroPolice())
                .nomAssure(policeTaux.getPolice().getNom())
                .prenomAssure(policeTaux.getPolice().getPrenom())
                .tauxId(policeTaux.getTaux().getId())
                .tauxLibelle(policeTaux.getTaux().getLibelle())
                .tauxPourcentage(policeTaux.getTaux().getTauxPourcentage())
                .dateDebut(policeTaux.getDateDebut())
                .dateFin(policeTaux.getDateFin())
                .actif(policeTaux.getActif())
                .build();
    }
}
