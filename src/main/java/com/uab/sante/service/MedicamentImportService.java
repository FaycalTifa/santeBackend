// service/MedicamentImportService.java
package com.uab.sante.service;

import com.uab.sante.entities.Examen;
import com.uab.sante.entities.Medicament;
import com.uab.sante.repository.ExamenRepository;
import com.uab.sante.repository.MedicamentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicamentImportService {

    private final MedicamentRepository medicamentRepository;
    private final ExamenRepository examenRepository;


    @Transactional
    public void importerMedicaments(MultipartFile file) throws Exception {
        List<Medicament> medicaments = new ArrayList<>();
        int importedCount = 0;
        int updatedCount = 0;
        int duplicateCount = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue; // Ignorer l'en-tête
                }

                if (line.trim().isEmpty()) continue;

                // Séparateur : point-virgule ou virgule
                String[] parts = line.split("[;,]");
                if (parts.length >= 2) {
                    String nom = parts[0].trim();
                    String exclusion = parts[1].trim().toUpperCase();

                    if (nom.isEmpty()) continue;

                    // Nettoyer le nom
                    nom = nom.replace("\"", "").trim();
                    nom = nom.replace("<br>", " ").replace("&lt;br&gt;", " ").trim();

                    // Normaliser l'exclusion
                    if (exclusion.equals("OUI")) {
                        exclusion = "OUI";
                    } else {
                        exclusion = "NON";
                    }

                    // Vérifier si le médicament existe déjà - utiliser une requête qui retourne une liste
                    List<Medicament> existants = medicamentRepository.findByNomIgnoreCaseList(nom);

                    if (!existants.isEmpty()) {
                        // Prendre le premier et ignorer les doublons
                        Medicament existing = existants.get(0);
                        if (existants.size() > 1) {
                            duplicateCount++;
                            System.out.println("⚠️ Doublon trouvé pour: " + nom + " (" + existants.size() + " occurrences)");
                        }

                        if (existing.getExclusion() == null || !existing.getExclusion().equals(exclusion)) {
                            existing.setExclusion(exclusion);
                            medicamentRepository.save(existing);
                            updatedCount++;
                            System.out.println("📝 Mis à jour: " + nom + " - Exclusion: " + exclusion);
                        }
                    } else {
                        Medicament medicament = Medicament.builder()
                                .nom(nom)
                                .exclusion(exclusion)
                                .actif(true)
                                .build();
                        medicaments.add(medicament);
                        importedCount++;
                        System.out.println("➕ Nouveau: " + nom + " - Exclusion: " + exclusion);
                    }
                }
            }

            if (!medicaments.isEmpty()) {
                medicamentRepository.saveAll(medicaments);
            }
            System.out.println("✅ Import terminé: " + importedCount + " nouveaux, " + updatedCount + " mis à jour, " + duplicateCount + " doublons ignorés");
        }
    }


    @Transactional
    public void importerExamens(MultipartFile file) throws Exception {
        List<Examen> examens = new ArrayList<>();
        int importedCount = 0;
        int updatedCount = 0;
        int duplicateCount = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue; // Ignorer l'en-tête
                }

                if (line.trim().isEmpty()) continue;

                // Séparateur : point-virgule ou virgule
                String[] parts = line.split("[;,]");
                if (parts.length >= 2) {
                    // ✅ CHANGEMENT: parts[0] = NOM de l'examen, parts[1] = VALIDATION (exclusion)
                    String nom = parts[0].trim();           // Le nom de l'examen
                    String validation = parts[1].trim().toUpperCase();  // La validation (OUI/NON)

                    if (nom.isEmpty()) continue;

                    // Nettoyer les valeurs
                    nom = nom.replace("\"", "").trim();
                    nom = nom.replace("<br>", " ").replace("&lt;br&gt;", " ").trim();

                    // Normaliser la validation
                    if (validation.equals("OUI")) {
                        validation = "OUI";
                    } else {
                        validation = "NON";
                    }

                    // ✅ CHANGEMENT: Vérifier si l'examen existe déjà par son NOM (plus par code)
                    List<Examen> existants = examenRepository.findByNomIgnoreCaseList(nom);

                    if (!existants.isEmpty()) {
                        // Prendre le premier et ignorer les doublons
                        Examen existing = existants.get(0);
                        if (existants.size() > 1) {
                            duplicateCount++;
                            System.out.println("⚠️ Doublon trouvé pour nom: " + nom + " (" + existants.size() + " occurrences)");
                        }

                        // Mettre à jour la validation si nécessaire
                        if (existing.getValidation() == null || !existing.getValidation().equals(validation)) {
                            existing.setValidation(validation);
                            examenRepository.save(existing);
                            updatedCount++;
                            System.out.println("📝 Mis à jour: " + nom + " - Validation: " + validation);
                        }
                    } else {
                        // ✅ CHANGEMENT: Créer un nouvel examen avec le nom et la validation
                        Examen examen = Examen.builder()
                                .nom(nom)                    // Le nom de l'examen
                                .validation(validation)      // La validation (OUI/NON)
                                .actif(true)
                                .build();
                        examens.add(examen);
                        importedCount++;
                        System.out.println("➕ Nouveau: " + nom + " - Validation: " + validation);
                    }
                }
            }

            if (!examens.isEmpty()) {
                examenRepository.saveAll(examens);
            }
            System.out.println("✅ Import examens terminé: " + importedCount + " nouveaux, " + updatedCount + " mis à jour, " + duplicateCount + " doublons ignorés");
        }
    }

}
