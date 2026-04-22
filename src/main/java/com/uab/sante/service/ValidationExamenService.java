// service/ValidationExamenService.java
package com.uab.sante.service;

import com.uab.sante.entities.PrescriptionExamen;
import com.uab.sante.entities.Utilisateur;
import com.uab.sante.repository.PrescriptionExamenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ValidationExamenService {

    private final PrescriptionExamenRepository prescriptionExamenRepository;
    private final NotificationService notificationService;  // À injecter

    public List<PrescriptionExamen> getExamensEnAttenteValidation() {
        // ✅ Récupérer les examens avec validationUab = 'EN_ATTENTE'
        return prescriptionExamenRepository.findExamensEnAttenteValidationUAB();
    }

    @Transactional
    public PrescriptionExamen validerExamen(Long id, Utilisateur uabAdmin) {
        PrescriptionExamen examen = prescriptionExamenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Examen non trouvé"));

        // ✅ Vérifier qu'il est bien en attente
        if (!"EN_ATTENTE".equals(examen.getValidationUab())) {
            throw new RuntimeException("Cet examen n'est pas en attente de validation (statut actuel: " + examen.getValidationUab() + ")");
        }

        // ✅ Mettre à jour le statut
        examen.setValidationUab("OUI");
        examen.setValidationUabDate(LocalDateTime.now());
        examen.setValidationUabPar(uabAdmin);

        PrescriptionExamen saved = prescriptionExamenRepository.save(examen);

        // ✅ Notifier le médecin (optionnel)
        // notificationService.notifierMedecinValidation(id);

        return saved;
    }

    @Transactional
    public PrescriptionExamen rejeterExamen(Long id, String motif, Utilisateur uabAdmin) {
        PrescriptionExamen examen = prescriptionExamenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Examen non trouvé"));

        examen.setValidationUab("NON");
        examen.setValidationUabDate(LocalDateTime.now());
        examen.setValidationUabPar(uabAdmin);
        examen.setMotifRejet(motif);

        PrescriptionExamen saved = prescriptionExamenRepository.save(examen);

        // ✅ Notifier le médecin (optionnel)
        // notificationService.notifierMedecinRejet(id, motif);

        return saved;
    }

    public List<PrescriptionExamen> getExamensValidesNonPayes() {
        // ✅ Examens validés (OUI) mais non payés
        return prescriptionExamenRepository.findExamensValidesNonPayes();
    }

    public List<PrescriptionExamen> getExamensPayesNonRealises() {
        return prescriptionExamenRepository.findExamensPayesNonRealises();
    }
}
