package com.uab.sante.repository;

import com.uab.sante.entities.PoliceTaux;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PoliceTauxRepository extends JpaRepository<PoliceTaux, Long> {

    @Query("SELECT pt FROM PoliceTaux pt WHERE pt.police.numeroPolice = :numeroPolice AND pt.actif = true AND pt.dateDebut <= :date AND (pt.dateFin IS NULL OR pt.dateFin >= :date)")
    Optional<PoliceTaux> findActiveByPoliceAndDate(@Param("numeroPolice") String numeroPolice, @Param("date") LocalDate date);

    default Optional<PoliceTaux> findActiveByPolice(String numeroPolice) {
        return findActiveByPoliceAndDate(numeroPolice, LocalDate.now());
    }

    List<PoliceTaux> findByPoliceNumeroPoliceOrderByDateDebutDesc(String numeroPolice);

    @Query("SELECT pt FROM PoliceTaux pt WHERE pt.actif = true AND pt.dateDebut <= :date AND (pt.dateFin IS NULL OR pt.dateFin >= :date)")
    List<PoliceTaux> findAllActiveAtDate(@Param("date") LocalDate date);
}
