package com.uab.sante.service;

import com.uab.sante.dto.request.TauxCouvertureRequestDTO;
import com.uab.sante.dto.response.TauxCouvertureResponseDTO;
import com.uab.sante.entities.TauxCouverture;
import com.uab.sante.repository.TauxCouvertureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TauxCouvertureService {

    private final TauxCouvertureRepository tauxCouvertureRepository;

    /**
     * Récupérer tous les taux actifs
     */
    public List<TauxCouverture> getAllActive() {
        return tauxCouvertureRepository.findAllActive();
    }

    /**
     * Créer un nouveau taux
     */
    @Transactional
    public TauxCouverture create(TauxCouvertureRequestDTO request) {
        // Vérifier si le code existe déjà
        if (request.getCode() != null && tauxCouvertureRepository.findByCode(request.getCode()).isPresent()) {
            throw new RuntimeException("Un taux avec ce code existe déjà");
        }

        TauxCouverture taux = TauxCouverture.builder()
                .code(request.getCode())
                .libelle(request.getLibelle())
                .tauxPourcentage(request.getTauxPourcentage())
                .dateDebut(LocalDate.now())
                .actif(true)
                .build();

        return tauxCouvertureRepository.save(taux);
    }
}
