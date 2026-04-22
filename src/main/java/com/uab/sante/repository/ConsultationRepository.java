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
    @Query("SELECT c FROM Consultation c WHERE c.structure.id = :structureId AND c.statut IN :statuts ORDER BY c.dateConsultation DESC")
    List<Consultation> findByStructureIdAndStatutInOrderByDateConsultationDesc(@Param("structureId") Long structureId, @Param("statuts") List<String> statuts);

    // Dossiers en attente de validation
    @Query("SELECT c FROM Consultation c WHERE c.validationUab = false AND c.statut = 'COMPLET' ORDER BY c.dateConsultation DESC")
    List<Consultation> findDossiersEnAttenteValidation();

    // Dossiers hors délai
    @Query("SELECT c FROM Consultation c WHERE c.dateTransmission IS NOT NULL AND c.dateTransmission < :limite")
    List<Consultation> findDossiersHorsDelai(@Param("limite") LocalDate limite);

    // Comptage par période
    @Query("SELECT COUNT(c) FROM Consultation c WHERE c.dateConsultation BETWEEN :debut AND :fin")
    Long countByDateConsultationBetween(@Param("debut") LocalDate debut, @Param("fin") LocalDate fin);

    // Somme des montants pris en charge par période
    @Query("SELECT SUM(c.montantPrisEnCharge) FROM Consultation c WHERE c.dateConsultation BETWEEN :debut AND :fin")
    Double sumMontantPrisEnChargeByDateConsultationBetween(@Param("debut") LocalDate debut, @Param("fin") LocalDate fin);
}
