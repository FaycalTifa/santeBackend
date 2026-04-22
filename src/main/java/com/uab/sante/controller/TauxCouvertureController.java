// controller/TauxCouvertureController.java
package com.uab.sante.controller;

import com.uab.sante.dto.request.TauxCouvertureRequestDTO;
import com.uab.sante.dto.response.TauxCouvertureResponseDTO;
import com.uab.sante.entities.TauxCouverture;
import com.uab.sante.service.TauxCouvertureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
     * Liste de tous les taux (plus de notion d'actif)
     */
    @GetMapping
    public ResponseEntity<List<TauxCouvertureResponseDTO>> getAllTaux() {
        List<TauxCouverture> taux = tauxCouvertureService.getAllTaux();
        List<TauxCouvertureResponseDTO> response = taux.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * Récupérer un taux par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<TauxCouvertureResponseDTO> getTauxById(@PathVariable Long id) {
        TauxCouverture taux = tauxCouvertureService.getById(id);
        return ResponseEntity.ok(toDTO(taux));
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
     * Modifier un taux (UAB uniquement)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('UAB_ADMIN')")
    public ResponseEntity<TauxCouvertureResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody TauxCouvertureRequestDTO request) {
        TauxCouverture taux = tauxCouvertureService.update(id, request);
        return ResponseEntity.ok(toDTO(taux));
    }

    /**
     * Supprimer un taux (UAB uniquement)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('UAB_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tauxCouvertureService.delete(id);
        return ResponseEntity.ok().build();
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
                .build();
    }
}
