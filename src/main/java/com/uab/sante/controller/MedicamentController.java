package com.uab.sante.controller;

import com.uab.sante.entities.Medicament;
import com.uab.sante.service.MedicamentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medicaments")
@RequiredArgsConstructor
public class MedicamentController {

    private final MedicamentService medicamentService;

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('MEDECIN', 'PHARMACIEN', 'UAB_ADMIN')")
    public ResponseEntity<List<Medicament>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(medicamentService.search(keyword));
    }

// MedicamentController.java
    @GetMapping("/search-autorises")
    public ResponseEntity<List<Medicament>> searchAutorises(@RequestParam String keyword) {
        return ResponseEntity.ok(medicamentService.searchAutorises(keyword));
    }

    @GetMapping("/autorises")
    public ResponseEntity<List<Medicament>> getAllAutorises() {
        return ResponseEntity.ok(medicamentService.getAllAutorises());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MEDECIN', 'PHARMACIEN', 'UAB_ADMIN')")
    public ResponseEntity<List<Medicament>> getAll() {
        return ResponseEntity.ok(medicamentService.getAllActive());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MEDECIN', 'PHARMACIEN', 'UAB_ADMIN')")
    public ResponseEntity<Medicament> getById(@PathVariable Long id) {
        return ResponseEntity.ok(medicamentService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MEDECIN', 'UAB_ADMIN')")
    public ResponseEntity<Medicament> create(@RequestBody Medicament medicament) {
        return ResponseEntity.ok(medicamentService.create(medicament));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('UAB_ADMIN')")
    public ResponseEntity<Medicament> update(@PathVariable Long id, @RequestBody Medicament medicament) {
        return ResponseEntity.ok(medicamentService.update(id, medicament));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('UAB_ADMIN')")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        medicamentService.deactivate(id);
        return ResponseEntity.ok().build();
    }
}
