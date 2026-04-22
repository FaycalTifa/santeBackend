// service/NotificationService.java
package com.uab.sante.service;

import com.uab.sante.entities.PrescriptionExamen;
import com.uab.sante.entities.Utilisateur;
import com.uab.sante.repository.PrescriptionExamenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final PrescriptionExamenRepository prescriptionExamenRepository;

    /**
     * Notifier le médecin que son examen a été validé
     */
    public void notifierMedecinValidation(Long examenId) {
        PrescriptionExamen examen = prescriptionExamenRepository.findById(examenId)
                .orElseThrow(() -> new RuntimeException("Examen non trouvé"));

        // Ici vous pouvez envoyer un email, une notification push, etc.
        System.out.println("📧 Notification au médecin " + examen.getConsultation().getMedecin().getEmail());
        System.out.println("   Examen '" + examen.getExamenNom() + "' a été VALIDÉ par l'UAB");
    }

    /**
     * Notifier le médecin que son examen a été rejeté
     */
    public void notifierMedecinRejet(Long examenId, String motif) {
        PrescriptionExamen examen = prescriptionExamenRepository.findById(examenId)
                .orElseThrow(() -> new RuntimeException("Examen non trouvé"));

        System.out.println("📧 Notification au médecin " + examen.getConsultation().getMedecin().getEmail());
        System.out.println("   Examen '" + examen.getExamenNom() + "' a été REJETÉ par l'UAB");
        System.out.println("   Motif: " + motif);
    }
}
