// repository/PrescriptionExamenRepository.java
package com.uab.sante.repository;

import com.uab.sante.entities.PrescriptionExamen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescriptionExamenRepository extends JpaRepository<PrescriptionExamen, Long> {

    // ========== MÉTHODES EXISTANTES ==========

    List<PrescriptionExamen> findByLaboratoireIdAndRealiseFalse(Long laboratoireId);

    List<PrescriptionExamen> findByConsultationAssureNumeroPolice(String numeroPolice);

    List<PrescriptionExamen> findByRealiseTrue();

    List<PrescriptionExamen> findByLaboratoireIdAndPayeFalseAndRealiseFalse(Long laboratoireId);

    List<PrescriptionExamen> findByLaboratoireIdAndPayeTrueAndRealiseFalse(Long laboratoireId);

    List<PrescriptionExamen> findByLaboratoireIdAndRealiseTrue(Long laboratoireId);

    @Query("SELECT pe FROM PrescriptionExamen pe WHERE pe.consultation.assure.numeroPolice = :numeroPolice AND pe.realise = false")
    List<PrescriptionExamen> findByConsultationAssureNumeroPoliceAndRealiseFalse(@Param("numeroPolice") String numeroPolice);

    List<PrescriptionExamen> findByConsultationId(Long consultationId);

    List<PrescriptionExamen> findByConsultationIdAndRealiseTrue(Long consultationId);

    @Query("SELECT pe FROM PrescriptionExamen pe WHERE pe.consultation.assure.numeroPolice = :numeroPolice AND pe.realise = true")
    List<PrescriptionExamen> findByConsultationAssureNumeroPoliceAndRealiseTrue(@Param("numeroPolice") String numeroPolice);

    List<PrescriptionExamen> findByConsultationIdAndRealiseFalse(Long consultationId);

    @Query("SELECT pe FROM PrescriptionExamen pe WHERE pe.laboratoire.id = :laboratoireId AND pe.realise = false")
    List<PrescriptionExamen> findEnAttenteRealisationByLaboratoire(@Param("laboratoireId") Long laboratoireId);

    @Query("SELECT pe FROM PrescriptionExamen pe WHERE pe.realise = true AND pe.interpretation IS NULL")
    List<PrescriptionExamen> findRealisesSansInterpretation();

    // ========== MÉTHODES POUR LA VALIDATION UAB ==========

    @Query("SELECT pe FROM PrescriptionExamen pe " +
            "WHERE pe.validationUab = 'EN_ATTENTE' " +
            "ORDER BY pe.datePrescription DESC")
    List<PrescriptionExamen> findExamensEnAttenteValidationUAB();

    @Query("SELECT pe FROM PrescriptionExamen pe " +
            "WHERE pe.validationUab = 'OUI' AND pe.paye = false AND pe.realise = false " +
            "ORDER BY pe.datePrescription DESC")
    List<PrescriptionExamen> findExamensValidesNonPayes();

    @Query("SELECT pe FROM PrescriptionExamen pe " +
            "WHERE pe.validationUab = 'OUI' AND pe.paye = true AND pe.realise = false " +
            "ORDER BY pe.datePrescription DESC")
    List<PrescriptionExamen> findExamensPayesNonRealises();

    // ✅ AJOUTER CETTE MÉTHODE POUR LABORATOIRESERVICE
    @Query("SELECT pe FROM PrescriptionExamen pe " +
            "WHERE pe.laboratoire.id = :laboratoireId " +
            "AND pe.validationUab = :validationUab " +
            "AND pe.paye = false " +
            "AND pe.realise = false " +
            "ORDER BY pe.datePrescription DESC")
    List<PrescriptionExamen> findByLaboratoireIdAndValidationUabAndPayeFalseAndRealiseFalse(
            @Param("laboratoireId") Long laboratoireId,
            @Param("validationUab") String validationUab);

    @Query("SELECT pe FROM PrescriptionExamen pe " +
            "WHERE pe.consultation.medecin.id = :medecinId " +
            "AND pe.validationUab = :validationUab " +
            "ORDER BY pe.datePrescription DESC")
    List<PrescriptionExamen> findByConsultationMedecinIdAndValidationUab(
            @Param("medecinId") Long medecinId,
            @Param("validationUab") String validationUab);

    @Query("SELECT pe FROM PrescriptionExamen pe " +
            "WHERE pe.consultation.id = :consultationId " +
            "AND pe.validationUab = :validationUab")
    List<PrescriptionExamen> findByConsultationIdAndValidationUab(
            @Param("consultationId") Long consultationId,
            @Param("validationUab") String validationUab);

    // repository/PrescriptionExamenRepository.java - Ajouter cette méthode

    // repository/PrescriptionExamenRepository.java
    /**
     * Rechercher des examens par CODEINTE, police, code risque et codeMemb (optionnel)
     */
    @Query("SELECT pe FROM PrescriptionExamen pe " +
            "JOIN pe.consultation c " +
            "JOIN c.assure a " +
            "WHERE (:numPolice IS NULL OR a.numeroPolice = :numPolice) " +
            "AND (:codeInte IS NULL OR c.codeInte = :codeInte) " +
            "AND (:codeRisq IS NULL OR c.codeRisq = :codeRisq) " +
            "AND (:codeMemb IS NULL OR a.codeMemb = :codeMemb) " +
            "ORDER BY c.dateConsultation DESC")
    List<PrescriptionExamen> findByCriteres(
            @Param("numPolice") String numPolice,
            @Param("codeInte") String codeInte,
            @Param("codeRisq") String codeRisq,
            @Param("codeMemb") String codeMemb);
    // repository/PrescriptionExamenRepository.java
    /**
     * Récupérer TOUS les examens d'un médecin avec filtres (quel que soit le statut)
     */
    // repository/PrescriptionExamenRepository.java
    // repository/PrescriptionExamenRepository.java
    /**
     * Récupérer TOUS les examens ayant fait l'objet d'une demande de validation UAB
     * (EN_ATTENTE, OUI, NON) pour un médecin avec filtres
     */
    @Query("SELECT pe FROM PrescriptionExamen pe " +
            "JOIN pe.consultation c " +
            "JOIN c.assure a " +
            "WHERE c.medecin.id = :medecinId " +
            "AND pe.validationUab IS NOT NULL " +
            "AND pe.validationUab != '' " +
            "AND (:numPolice IS NULL OR a.numeroPolice = :numPolice) " +
            "AND (:codeInte IS NULL OR c.codeInte = :codeInte) " +
            "AND (:codeRisq IS NULL OR c.codeRisq = :codeRisq) " +
            "AND (:codeMemb IS NULL OR a.codeMemb = :codeMemb) " +  // ✅ Ajouter codeMemb
            "ORDER BY pe.datePrescription DESC")
    List<PrescriptionExamen> findDemandesValidationWithFilters(
            @Param("medecinId") Long medecinId,
            @Param("numPolice") String numPolice,
            @Param("codeInte") String codeInte,
            @Param("codeRisq") String codeRisq,
            @Param("codeMemb") String codeMemb);  // ✅ Ajouter paramètre
}
