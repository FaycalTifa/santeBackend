// service/TauxCouvertureService.java
package com.uab.sante.service;

import com.uab.sante.dto.request.TauxCouvertureRequestDTO;
import com.uab.sante.entities.TauxCouverture;
import com.uab.sante.repository.TauxCouvertureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TauxCouvertureService {

    private final TauxCouvertureRepository tauxCouvertureRepository;

    /**
     * Récupérer tous les taux
     */
    public List<TauxCouverture> getAllTaux() {
        return tauxCouvertureRepository.findAll();
    }

    /**
     * Récupérer un taux par ID
     */
    public TauxCouverture getById(Long id) {
        return tauxCouvertureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Taux non trouvé"));
    }

    /**
     * Créer un nouveau taux
     */
    @Transactional
    public TauxCouverture create(TauxCouvertureRequestDTO request) {
        if (request.getCode() != null && tauxCouvertureRepository.findByCode(request.getCode()).isPresent()) {
            throw new RuntimeException("Un taux avec ce code existe déjà");
        }

        TauxCouverture taux = TauxCouverture.builder()
                .code(request.getCode())
                .libelle(request.getLibelle())
                .tauxPourcentage(request.getTauxPourcentage())
                .build();

        return tauxCouvertureRepository.save(taux);
    }

    /**
     * Modifier un taux
     */
    @Transactional
    public TauxCouverture update(Long id, TauxCouvertureRequestDTO request) {
        TauxCouverture existing = getById(id);

        existing.setCode(request.getCode());
        existing.setLibelle(request.getLibelle());
        existing.setTauxPourcentage(request.getTauxPourcentage());

        return tauxCouvertureRepository.save(existing);
    }

    /**
     * Supprimer un taux
     */
    @Transactional
    public void delete(Long id) {
        TauxCouverture taux = getById(id);
        tauxCouvertureRepository.delete(taux);
    }
}
