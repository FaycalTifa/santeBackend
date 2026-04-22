// controller/PlafonnementController.java
package com.uab.sante.controller;

import com.uab.sante.dto.CalculRemboursementDTO;
import com.uab.sante.dto.request.PlafonnementRequestDTO;
import com.uab.sante.entities.PlafonnementConsultation;
import com.uab.sante.service.PlafonnementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plafonnement")
@RequiredArgsConstructor
@PreAuthorize("hasRole('UAB_ADMIN')")
public class PlafonnementController {

    private final PlafonnementService plafonnementService;

    @PostMapping
    public ResponseEntity<PlafonnementConsultation> create(@RequestBody PlafonnementRequestDTO request) {
        return ResponseEntity.ok(plafonnementService.save(request));
    }

    @GetMapping("/{codeInte}/{typeConsultation}")
    public ResponseEntity<PlafonnementConsultation> getByCodeInteAndType(
            @PathVariable String codeInte,
            @PathVariable String typeConsultation) {
        PlafonnementConsultation plafonnement = plafonnementService.getByCodeInteAndType(codeInte, typeConsultation);
        return plafonnement != null ? ResponseEntity.ok(plafonnement) : ResponseEntity.notFound().build();
    }

    @GetMapping("/{codeInte}")
    public ResponseEntity<List<PlafonnementConsultation>> getByCodeInte(@PathVariable String codeInte) {
        return ResponseEntity.ok(plafonnementService.getByCodeInte(codeInte));
    }

    @GetMapping
    public ResponseEntity<List<PlafonnementConsultation>> getAll() {
        return ResponseEntity.ok(plafonnementService.getAll());
    }

    @GetMapping("/calculer")
    public ResponseEntity<CalculRemboursementDTO> calculerRemboursement(
            @RequestParam String codeInte,
            @RequestParam String typeConsultation,
            @RequestParam Double montant,
            @RequestParam Double tauxCouverture) {
        return ResponseEntity.ok(plafonnementService.calculerRemboursement(codeInte, typeConsultation, montant, tauxCouverture));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlafonnementConsultation> update(@PathVariable Long id, @RequestBody PlafonnementRequestDTO request) {
        return ResponseEntity.ok(plafonnementService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        plafonnementService.delete(id);
        return ResponseEntity.ok().build();
    }
}
