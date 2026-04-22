// repository/PrescriptionMedicamentRepository.java
package com.uab.sante.repository;

import com.uab.sante.entities.PrescriptionMedicament;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescriptionMedicamentRepository extends JpaRepository<PrescriptionMedicament, Long> {

    List<PrescriptionMedicament> findByConsultationIdAndDelivreFalse(Long consultationId);

    List<PrescriptionMedicament> findByConsultationAssureNumeroPolice(String numeroPolice);

    List<PrescriptionMedicament> findByPharmacieId(Long pharmacieId);

    @Query("SELECT pm FROM PrescriptionMedicament pm WHERE pm.consultation.assure.numeroPolice = :numeroPolice AND pm.delivre = false")
    List<PrescriptionMedicament> findByConsultationAssureNumeroPoliceAndDelivreFalse(@Param("numeroPolice") String numeroPolice);

    List<PrescriptionMedicament> findByPharmacieIdAndDelivreFalse(Long pharmacieId);

    List<PrescriptionMedicament> findByPharmacieIdAndDelivreTrue(Long pharmacieId);

    List<PrescriptionMedicament> findByConsultationId(Long consultationId);

    // ✅ AJOUTER CETTE MÉTHODE
    @Query("SELECT pm FROM PrescriptionMedicament pm " +
            "JOIN pm.consultation c " +
            "JOIN c.assure a " +
            "WHERE (:numPolice IS NULL OR a.numeroPolice = :numPolice) " +
            "AND (:codeInte IS NULL OR c.codeInte = :codeInte) " +
            "AND (:codeRisq IS NULL OR c.codeRisq = :codeRisq) " +
            "AND (:codeMemb IS NULL OR a.codeMemb = :codeMemb) " +
            "ORDER BY c.dateConsultation DESC")
    List<PrescriptionMedicament> findByCriteres(
            @Param("numPolice") String numPolice,
            @Param("codeInte") String codeInte,
            @Param("codeRisq") String codeRisq,
            @Param("codeMemb") String codeMemb);
}
