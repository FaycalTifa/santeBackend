package com.uab.sante.repository;

import com.uab.sante.entities.PrescriptionExamen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescriptionExamenRepository extends JpaRepository<PrescriptionExamen, Long> {

    // ✅ AJOUTER CES MÉTHODES
    List<PrescriptionExamen> findByLaboratoireIdAndRealiseFalse(Long laboratoireId);

    List<PrescriptionExamen> findByConsultationAssureNumeroPolice(String numeroPolice);
    List<PrescriptionExamen> findByRealiseTrue();

    // ✅ Ajouter ces méthodes
    List<PrescriptionExamen> findByLaboratoireIdAndPayeFalseAndRealiseFalse(Long laboratoireId);

    List<PrescriptionExamen> findByLaboratoireIdAndPayeTrueAndRealiseFalse(Long laboratoireId);

    List<PrescriptionExamen> findByLaboratoireIdAndRealiseTrue(Long laboratoireId);

    @Query("SELECT pe FROM PrescriptionExamen pe WHERE pe.consultation.assure.numeroPolice = :numeroPolice AND pe.realise = false")
    List<PrescriptionExamen> findByConsultationAssureNumeroPoliceAndRealiseFalse(@Param("numeroPolice") String numeroPolice);

    // ✅ AJOUTER CETTE MÉTHODE
    List<PrescriptionExamen> findByConsultationId(Long consultationId);

    // ✅ AJOUTER CES MÉTHODES
    List<PrescriptionExamen> findByConsultationIdAndRealiseTrue(Long consultationId);

    @Query("SELECT pe FROM PrescriptionExamen pe WHERE pe.consultation.assure.numeroPolice = :numeroPolice AND pe.realise = true")
    List<PrescriptionExamen> findByConsultationAssureNumeroPoliceAndRealiseTrue(@Param("numeroPolice") String numeroPolice);
    // ✅ Méthode pour les examens non réalisés
    // Méthodes existantes
    List<PrescriptionExamen> findByConsultationIdAndRealiseFalse(Long consultationId);




    @Query("SELECT pe FROM PrescriptionExamen pe WHERE pe.laboratoire.id = :laboratoireId AND pe.realise = false")
    List<PrescriptionExamen> findEnAttenteRealisationByLaboratoire(@Param("laboratoireId") Long laboratoireId);

    @Query("SELECT pe FROM PrescriptionExamen pe WHERE pe.realise = true AND pe.interpretation IS NULL")
    List<PrescriptionExamen> findRealisesSansInterpretation();
}
