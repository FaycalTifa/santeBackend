package com.uab.sante.controller;

import com.uab.sante.dto.request.TauxCouvertureRequestDTO;
import com.uab.sante.dto.response.TauxCouvertureResponseDTO;
import com.uab.sante.entities.TauxCouverture;
import com.uab.sante.service.TauxCouvertureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/taux-couverture")
@RequiredArgsConstructor
public class TauxCouvertureController {

    private final TauxCouvertureService tauxCouvertureService;

    /**
     * Liste de tous les taux actifs (pour la liste déroulante)
     */
    @GetMapping
    public ResponseEntity<List<TauxCouvertureResponseDTO>> getAllActive() {
        List<TauxCouverture> taux = tauxCouvertureService.getAllActive();
        List<TauxCouvertureResponseDTO> response = taux.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * Créer un nouveau taux (UAB uniquement)
     */
    @PostMapping
    @PreAuthorize("hasRole('UAB_ADMIN')")
    public ResponseEntity<TauxCouvertureResponseDTO> create(@Valid @RequestBody TauxCouvertureRequestDTO request) {
        TauxCouverture taux = tauxCouvertureService.create(request);
        return ResponseEntity.ok(toDTO(taux));
    }

    /**
     * Convertir en DTO
     */
    private TauxCouvertureResponseDTO toDTO(TauxCouverture taux) {
        return TauxCouvertureResponseDTO.builder()
                .id(taux.getId())
                .code(taux.getCode())
                .libelle(taux.getLibelle())
                .tauxPourcentage(taux.getTauxPourcentage())
                .actif(taux.getActif())
                .build();
    }
}
