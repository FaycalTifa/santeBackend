package com.uab.sante.repository;

import com.uab.sante.entities.PoliceOracle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PoliceOracleRepository extends JpaRepository<PoliceOracle, String> {

    Optional<PoliceOracle> findByNumePoli(String numePoli);

    List<PoliceOracle> findByRaisSociContainingIgnoreCase(String nom);

    @Query("SELECT p FROM PoliceOracle p WHERE p.dateEcheance > :date")
    List<PoliceOracle> findPolicesNonEchues(@Param("date") LocalDate date);

    @Query(value = "SELECT NUMEPOLI, DATEECHE, RAISSOCI FROM VOTRE_TABLE_POLICES WHERE NUMEPOLI = :numPolice", nativeQuery = true)
    Optional<PoliceOracle> findPoliceByNumero(@Param("numPolice") String numPolice);
}
