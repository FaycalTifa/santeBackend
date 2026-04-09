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

    Optional<Medicament> findByCode(String code);

    @Query("SELECT m FROM Medicament m WHERE LOWER(m.nom) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(m.code) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Medicament> search(@Param("keyword") String keyword);
}
