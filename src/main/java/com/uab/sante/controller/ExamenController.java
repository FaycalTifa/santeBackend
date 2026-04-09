package com.uab.sante.controller;

import com.uab.sante.entities.Examen;
import com.uab.sante.service.ExamenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/examens")
@RequiredArgsConstructor
public class ExamenController {

    private final ExamenService examenService;

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('MEDECIN', 'BIOLOGISTE', 'UAB_ADMIN')")
    public ResponseEntity<List<Examen>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(examenService.search(keyword));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MEDECIN', 'BIOLOGISTE', 'UAB_ADMIN')")
    public ResponseEntity<List<Examen>> getAll() {
        return ResponseEntity.ok(examenService.getAllActive());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MEDECIN', 'BIOLOGISTE', 'UAB_ADMIN')")
    public ResponseEntity<Examen> getById(@PathVariable Long id) {
        return ResponseEntity.ok(examenService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MEDECIN', 'UAB_ADMIN')")
    public ResponseEntity<Examen> create(@RequestBody Examen examen) {
        return ResponseEntity.ok(examenService.create(examen));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('UAB_ADMIN')")
    public ResponseEntity<Examen> update(@PathVariable Long id, @RequestBody Examen examen) {
        return ResponseEntity.ok(examenService.update(id, examen));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('UAB_ADMIN')")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        examenService.deactivate(id);
        return ResponseEntity.ok().build();
    }
}
