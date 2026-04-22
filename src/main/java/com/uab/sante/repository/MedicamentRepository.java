package com.uab.sante.repository;

import com.uab.sante.entities.Medicament;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicamentRepository extends JpaRepository<Medicament, Long> {

    List<Medicament> findByActifTrueOrderByNomAsc();

    List<Medicament> findByNomContainingIgnoreCaseAndActifTrue(String nom);


    // ✅ Méthode qui retourne une liste (pour gérer les doublons)
    @Query("SELECT m FROM Medicament m WHERE UPPER(m.nom) = UPPER(:nom)")
    List<Medicament> findByNomIgnoreCaseList(@Param("nom") String nom);


    // ✅ Rechercher les médicaments SANS exclusion (exclusion = 'NON')
    List<Medicament> findByActifTrueAndExclusionNotOrderByNomAsc(String exclusion);

    // ✅ Rechercher les médicaments autorisés (exclusion = 'NON')
    default List<Medicament> findMedicamentsAutorises() {
        return findByActifTrueAndExclusionNotOrderByNomAsc("OUI");
    }


    // ✅ Recherche avec filtre d'exclusion
    List<Medicament> findByNomContainingIgnoreCaseAndActifTrueAndExclusionNot(String nom, String exclusion);

    Optional<Medicament> findByCode(String code);

    // ✅ Rechercher par nom exact (pour éviter les doublons)
    Medicament findByNomIgnoreCase(String nom);

    @Query("SELECT m FROM Medicament m WHERE LOWER(m.nom) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(m.code) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Medicament> search(@Param("keyword") String keyword);
}
