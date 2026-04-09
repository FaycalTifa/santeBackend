package com.uab.sante.repository;

import com.uab.sante.entities.TauxCouverture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TauxCouvertureRepository extends JpaRepository<TauxCouverture, Long> {

    List<TauxCouverture> findByActifTrueOrderByTauxPourcentageAsc();

    @Query("SELECT t FROM TauxCouverture t WHERE t.actif = true ORDER BY t.tauxPourcentage ASC")
    List<TauxCouverture> findAllActive();

    // Recherche par numero_police (sans relation JPA)
    @Query("SELECT t FROM TauxCouverture t WHERE t.numeroPolice = :numeroPolice AND t.actif = true AND (t.dateFin IS NULL OR t.dateFin >= CURRENT_DATE)")
    Optional<TauxCouverture> findActiveByNumeroPolice(@Param("numeroPolice") String numeroPolice);

    Optional<TauxCouverture> findByCode(String code);
}
