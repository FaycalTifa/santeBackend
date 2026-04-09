package com.uab.sante.controller;

import com.uab.sante.dto.request.PoliceTauxRequestDTO;
import com.uab.sante.dto.response.PoliceTauxResponseDTO;
import com.uab.sante.service.PoliceTauxService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/polices-taux")
@RequiredArgsConstructor
public class PoliceTauxController {

    private final PoliceTauxService policeTauxService;

    /**
     * Récupérer le taux actif d'une police (pour la caisse)
     */
    @GetMapping("/actif/{numeroPolice}")
    public ResponseEntity<PoliceTauxResponseDTO> getActiveByPolice(@PathVariable String numeroPolice) {
        return ResponseEntity.ok(policeTauxService.getActiveByPolice(numeroPolice));
    }

    /**
     * Récupérer l'historique des taux d'une police
     */
    @GetMapping("/historique/{numeroPolice}")
    @PreAuthorize("hasRole('UAB_ADMIN')")
    public ResponseEntity<List<PoliceTauxResponseDTO>> getHistoriqueByPolice(@PathVariable String numeroPolice) {
        return ResponseEntity.ok(policeTauxService.getHistoriqueByPolice(numeroPolice));
    }

    /**
     * Assigner un taux à une police (UAB uniquement)
     */
    @PostMapping
    @PreAuthorize("hasRole('UAB_ADMIN')")
    public ResponseEntity<PoliceTauxResponseDTO> assignerTaux(@Valid @RequestBody PoliceTauxRequestDTO request) {
        return ResponseEntity.ok(policeTauxService.assignerTaux(request));
    }
}
