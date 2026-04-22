// service/ExamenService.java
package com.uab.sante.service;

import com.uab.sante.entities.Examen;
import com.uab.sante.repository.ExamenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExamenService {

    private final ExamenRepository examenRepository;

    // ✅ NOUVELLE MÉTHODE: rechercher uniquement les examens autorisés (validation = 'NON')
    public List<Examen> searchAutorises(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllAutorises();
        }
        return examenRepository.findByNomContainingIgnoreCaseAndActifTrueAndValidationNot(keyword.trim(), "OUI");
    }

    // ✅ NOUVELLE MÉTHODE: récupérer tous les examens autorisés
    public List<Examen> getAllAutorises() {
        return examenRepository.findByActifTrueAndValidationNotOrderByNomAsc("OUI");
    }

    // Méthodes existantes
    public List<Examen> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllActive();
        }
        return examenRepository.search(keyword.trim());
    }

    public List<Examen> getAllActive() {
        return examenRepository.findByActifTrueOrderByNomAsc();
    }

    public Examen getById(Long id) {
        return examenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Examen non trouvé"));
    }

    @Transactional
    public Examen create(Examen examen) {
        if (examen.getCode() != null && examenRepository.findByCode(examen.getCode()).isPresent()) {
            throw new RuntimeException("Un examen avec ce code existe déjà");
        }
        if (examen.getValidation() == null) {
            examen.setValidation("NON");
        }
        examen.setActif(true);
        return examenRepository.save(examen);
    }

    @Transactional
    public Examen createOrGet(String nom, String categorie, String codeActe) {
        List<Examen> existants = examenRepository.findByNomContainingIgnoreCaseAndActifTrue(nom);
        if (!existants.isEmpty()) {
            return existants.get(0);
        }

        Examen nouveau = new Examen();
        nouveau.setNom(nom);
        if (categorie != null) {
            try {
                nouveau.setCategorie(Examen.CategorieExamen.valueOf(categorie));
            } catch (IllegalArgumentException e) {
                // Ignorer
            }
        }
        nouveau.setCode(codeActe);
        nouveau.setActif(true);
        nouveau.setValidation("NON");
        return examenRepository.save(nouveau);
    }

    @Transactional
    public Examen update(Long id, Examen examen) {
        Examen existing = getById(id);
        existing.setNom(examen.getNom());
        existing.setCategorie(examen.getCategorie());
        existing.setCode(examen.getCode());
        existing.setPrixReference(examen.getPrixReference());
        existing.setValidation(examen.getValidation());
        return examenRepository.save(existing);
    }

    @Transactional
    public void deactivate(Long id) {
        Examen examen = getById(id);
        examen.setActif(false);
        examenRepository.save(examen);
    }
}
