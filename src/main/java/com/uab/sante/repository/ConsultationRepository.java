package com.uab.sante.repository;

import com.uab.sante.entities.Consultation;
import com.uab.sante.entities.TauxCouverture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConsultationRepository extends JpaRepository<Consultation, Long> {

    // Recherche par numéro de police de l'assuré
    List<Consultation> findByAssureNumeroPoliceOrderByDateConsultationDesc(String numeroPolice);

    // ✅ AJOUTER CETTE MÉTHODE
    List<Consultation> findByStructureId(Long structureId);

    // ✅ AJOUTER CETTE MÉTHODE (pour les statistiques par structure)
    List<Consultation> findByStructureIdOrderByDateConsultationDesc(Long structureId);

    /**
     * ✅ Trouver le dernier numéro de feuille commençant par un préfixe
     */
    @Query("SELECT c.numeroFeuille FROM Consultation c WHERE c.numeroFeuille LIKE :prefix ORDER BY c.numeroFeuille DESC")
    String findTopByNumeroFeuilleStartingWithOrderByNumeroFeuilleDesc(@Param("prefix") String prefix);


    // Recherche par statut
    List<Consultation> findByStatutOrderByDateConsultationDesc(String statut);

    // repository/ConsultationRepository.java
    List<Consultation> findByStatut(String statut);

    @Query("SELECT c FROM Consultation c WHERE c.assure.numeroPolice = :numeroPolice AND c.statut = :statut")
    List<Consultation> findByAssureNumeroPoliceAndStatut(@Param("numeroPolice") String numeroPolice, @Param("statut") String statut);

    // Recherche par médecin et statuts
    List<Consultation> findByMedecinIdAndStatutInOrderByDateConsultationDesc(Long medecinId, List<String> statuts);

    // ✅ NOUVELLE MÉTHODE : Recherche avec filtres
    // repository/ConsultationRepository.java
    // repository/ConsultationRepository.java
    // repository/ConsultationRepository.java
    // repository/ConsultationRepository.java
    @Query("SELECT c FROM Consultation c " +
            "LEFT JOIN FETCH c.assure a " +
            "WHERE (:medecinId IS NULL OR c.medecin.id = :medecinId) " +  // ← Ajouter cette ligne
            "AND c.statut IN :statuts " +
            "AND (:numPolice IS NULL OR a.numeroPolice = :numPolice) " +
            "AND (:codeInte IS NULL OR a.codeInte = :codeInte) " +
            "AND (:codeRisq IS NULL OR a.codeRisq = :codeRisq) " +
            "ORDER BY c.dateConsultation DESC")
    List<Consultation> findByMedecinIdAndStatutInWithFilters(
            @Param("medecinId") Long medecinId,
            @Param("statuts") List<String> statuts,
            @Param("numPolice") String numPolice,
            @Param("codeInte") String codeInte,
            @Param("codeRisq") String codeRisq);


    // repository/ConsultationRepository.java
    // repository/ConsultationRepository.java
    /**
     * Récupérer les consultations d'une structure spécifique avec filtres
     * (Pour que le médecin ne voie que les consultations de sa propre structure)
     */
    @Query("SELECT c FROM Consultation c " +
            "LEFT JOIN FETCH c.assure a " +
            "WHERE c.structure.id = :structureId " +           // ✅ Filtrer par structure
            "AND (:medecinId IS NULL OR c.medecin.id = :medecinId) " +
            "AND c.statut IN :statuts " +
            "AND (:numPolice IS NULL OR a.numeroPolice = :numPolice) " +
            "AND (:codeInte IS NULL OR a.codeInte = :codeInte) " +
            "AND (:codeRisq IS NULL OR a.codeRisq = :codeRisq) " +
            "AND (:codeMemb IS NULL OR a.codeMemb = :codeMemb) " +
            "ORDER BY c.dateConsultation DESC")
    List<Consultation> findByStructureIdAndMedecinIdAndStatutInWithFilters(
            @Param("structureId") Long structureId,
            @Param("medecinId") Long medecinId,
            @Param("statuts") List<String> statuts,
            @Param("numPolice") String numPolice,
            @Param("codeInte") String codeInte,
            @Param("codeRisq") String codeRisq,
            @Param("codeMemb") String codeMemb);
    // Recherche par structure et statuts


    // ==================== DOSSIERS EN ATTENTE DE VALIDATION UAB ====================

    /**
     * ✅ Récupérer les consultations en attente de validation UAB
     * Utilise validationUabBool = false ou null
     */
    @Query("SELECT c FROM Consultation c " +
            "WHERE (c.validationUabBool IS NULL OR c.validationUabBool = false) " +
            "AND c.statut = 'PAYEE_CAISSE' " +
            "ORDER BY c.dateConsultation DESC")
    List<Consultation> findDossiersEnAttenteValidation();

    /**
     * ✅ Récupérer les consultations validées par UAB
     */
    @Query("SELECT c FROM Consultation c " +
            "WHERE c.validationUabBool = true " +
            "AND c.statut = 'VALIDEE_UAB' " +
            "ORDER BY c.validationUabDate DESC")
    List<Consultation> findDossiersValides();

    /**
     * ✅ Récupérer les consultations rejetées par UAB
     */
    @Query("SELECT c FROM Consultation c " +
            "WHERE c.validationUabBool = false " +
            "AND c.statut = 'REJETEE_UAB' " +
            "ORDER BY c.dateConsultation DESC")
    List<Consultation> findDossiersRejetes();

    /**
     * ✅ Récupérer les consultations par statut de validation
     */
    @Query("SELECT c FROM Consultation c " +
            "WHERE (:validationUabBool IS NULL OR c.validationUabBool = :validationUabBool) " +
            "ORDER BY c.dateConsultation DESC")
    List<Consultation> findByValidationUabBool(@Param("validationUabBool") Boolean validationUabBool);
}
