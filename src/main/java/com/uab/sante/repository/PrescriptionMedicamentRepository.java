package com.uab.sante.repository;

import com.uab.sante.entities.PrescriptionMedicament;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescriptionMedicamentRepository extends JpaRepository<PrescriptionMedicament, Long> {

    // ✅ Vérifiez que ces méthodes existent
    List<PrescriptionMedicament> findByConsultationIdAndDelivreFalse(Long consultationId);
    // ✅ Nouvelle méthode : retourne TOUTES les prescriptions par police (sans filtre delivre)
    List<PrescriptionMedicament> findByConsultationAssureNumeroPolice(String numeroPolice);


    List<PrescriptionMedicament> findByPharmacieId(Long pharmacieId);
    @Query("SELECT pm FROM PrescriptionMedicament pm WHERE pm.consultation.assure.numeroPolice = :numeroPolice AND pm.delivre = false")
    List<PrescriptionMedicament> findByConsultationAssureNumeroPoliceAndDelivreFalse(@Param("numeroPolice") String numeroPolice);

    List<PrescriptionMedicament> findByPharmacieIdAndDelivreFalse(Long pharmacieId);

    List<PrescriptionMedicament> findByPharmacieIdAndDelivreTrue(Long pharmacieId);

    // ✅ AJOUTER CETTE MÉTHODE
    List<PrescriptionMedicament> findByConsultationId(Long consultationId);
}
