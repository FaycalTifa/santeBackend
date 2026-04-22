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

    // ✅ Méthode pour gérer les doublons
    @Query("SELECT e FROM Examen e WHERE UPPER(e.code) = UPPER(:code)")
    List<Examen> findByCodeIgnoreCaseList(@Param("code") String code);


    // ✅ Rechercher les examens autorisés (validation = 'NON')
    List<Examen> findByActifTrueAndValidationNotOrderByNomAsc(String validation);

    // ✅ Rechercher les examens par nom avec filtre validation
    List<Examen> findByNomContainingIgnoreCaseAndActifTrueAndValidationNot(String nom, String validation);

    @Query("SELECT e FROM Examen e WHERE LOWER(e.nom) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(e.code) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Examen> search(@Param("keyword") String keyword);

    @Query("SELECT e FROM Examen e WHERE UPPER(e.nom) = UPPER(:nom)")
    List<Examen> findByNomIgnoreCaseList(@Param("nom") String nom);
}
