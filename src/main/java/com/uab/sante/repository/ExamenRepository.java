package com.uab.sante.repository;

import com.uab.sante.entities.Examen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamenRepository extends JpaRepository<Examen, Long> {

    List<Examen> findByActifTrueOrderByNomAsc();

    List<Examen> findByCategorieAndActifTrue(Examen.CategorieExamen categorie);

    List<Examen> findByNomContainingIgnoreCaseAndActifTrue(String nom);

    Optional<Examen> findByCode(String code);

    @Query("SELECT e FROM Examen e WHERE LOWER(e.nom) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(e.code) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Examen> search(@Param("keyword") String keyword);
}
