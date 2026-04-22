// controller/MedicamentImportController.java
package com.uab.sante.controller;

import com.uab.sante.service.MedicamentImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/medicaments/import")
@RequiredArgsConstructor
@PreAuthorize("hasRole('UAB_ADMIN')")
public class MedicamentImportController {

    private final MedicamentImportService medicamentImportService;

    // ✅ IMPORT DES MÉDICAMENTS
    @PostMapping("/csv")
    public ResponseEntity<Map<String, String>> importerMedicamentsCsv(@RequestParam("file") MultipartFile file) {
        System.out.println("=== IMPORT CSV MEDICAMENTS ===");
        System.out.println("Nom du fichier: " + file.getOriginalFilename());
        System.out.println("Taille: " + file.getSize());

        Map<String, String> response = new HashMap<>();

        try {
            medicamentImportService.importerMedicaments(file);
            response.put("message", "Import des médicaments réussi");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // ✅ IMPORT DES EXAMENS
    @PostMapping("/examens/csv")
    @PreAuthorize("hasRole('UAB_ADMIN')")
    public ResponseEntity<Map<String, String>> importerExamensCsv(@RequestParam("file") MultipartFile file) {
        System.out.println("=== IMPORT CSV EXAMENS ===");
        System.out.println("Nom du fichier: " + file.getOriginalFilename());
        System.out.println("Taille: " + file.getSize());

        Map<String, String> response = new HashMap<>();

        try {
            medicamentImportService.importerExamens(file);
            response.put("message", "Import des examens réussi");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
