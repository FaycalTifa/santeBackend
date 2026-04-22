// service/MedicamentService.java
package com.uab.sante.service;

import com.uab.sante.entities.Medicament;
import com.uab.sante.repository.MedicamentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicamentService {

    private final MedicamentRepository medicamentRepository;

    // ✅ Nouvelle méthode: rechercher uniquement les médicaments autorisés (exclusion = 'NON')
    public List<Medicament> searchAutorises(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllAutorises();
        }
        return medicamentRepository.findByNomContainingIgnoreCaseAndActifTrueAndExclusionNot(keyword.trim(), "OUI");
    }

    // ✅ Nouvelle méthode: récupérer tous les médicaments autorisés
    public List<Medicament> getAllAutorises() {
        return medicamentRepository.findByActifTrueAndExclusionNotOrderByNomAsc("OUI");
    }

    // Méthodes existantes
    public List<Medicament> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllActive();
        }
        return medicamentRepository.search(keyword.trim());
    }

    public List<Medicament> getAllActive() {
        return medicamentRepository.findByActifTrueOrderByNomAsc();
    }

    public Medicament getById(Long id) {
        return medicamentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Médicament non trouvé"));
    }

    @Transactional
    public Medicament create(Medicament medicament) {
        if (medicament.getCode() != null && medicamentRepository.findByCode(medicament.getCode()).isPresent()) {
            throw new RuntimeException("Un médicament avec ce code existe déjà");
        }
        // Par défaut, exclusion = "NON"
        if (medicament.getExclusion() == null) {
            medicament.setExclusion("NON");
        }
        medicament.setActif(true);
        return medicamentRepository.save(medicament);
    }

    @Transactional
    public Medicament createOrGet(String nom, String dosage, String forme) {
        List<Medicament> existants = medicamentRepository.findByNomContainingIgnoreCaseAndActifTrue(nom);
        if (!existants.isEmpty()) {
            return existants.get(0);
        }

        Medicament nouveau = new Medicament();
        nouveau.setNom(nom);
        nouveau.setDosage(dosage);
        nouveau.setForme(forme);
        nouveau.setActif(true);
        nouveau.setExclusion("NON");  // ✅ Par défaut NON
        return medicamentRepository.save(nouveau);
    }

    @Transactional
    public Medicament update(Long id, Medicament medicament) {
        Medicament existing = getById(id);
        existing.setNom(medicament.getNom());
        existing.setDosage(medicament.getDosage());
        existing.setForme(medicament.getForme());
        existing.setPrixReference(medicament.getPrixReference());
        existing.setExclusion(medicament.getExclusion());  // ✅ Permettre de mettre à jour l'exclusion
        return medicamentRepository.save(existing);
    }

    @Transactional
    public void deactivate(Long id) {
        Medicament medicament = getById(id);
        medicament.setActif(false);
        medicamentRepository.save(medicament);
    }
}
