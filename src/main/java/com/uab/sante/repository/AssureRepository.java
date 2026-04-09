package com.uab.sante.repository;

import com.uab.sante.entities.Assure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AssureRepository extends JpaRepository<Assure, Long> {

    Optional<Assure> findByNumeroPolice(String numeroPolice);

    boolean existsByNumeroPolice(String numeroPolice);

    Optional<Assure> findByTelephone(String telephone);


    @Query("SELECT a FROM Assure a WHERE a.numeroPolice = :numeroPolice OR a.nom LIKE %:search% OR a.prenom LIKE %:search%")
    Optional<Assure> searchByPoliceOrNom(@Param("numeroPolice") String numeroPolice, @Param("search") String search);
}
