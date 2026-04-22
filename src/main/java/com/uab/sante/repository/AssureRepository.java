// repository/AssureRepository.java
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

    Optional<Assure> findByCodeMemb(String codeMemb);

    // ✅ NOUVELLE MÉTHODE: Recherche par numéro police, codeInte ET codeRisq
    @Query("SELECT a FROM Assure a WHERE a.numeroPolice = :numPolice AND a.codeInte = :codeInte AND a.codeRisq = :codeRisq")
    Optional<Assure> findByNumeroPoliceAndCodeInteAndCodeRisq(
            @Param("numPolice") String numPolice,
            @Param("codeInte") String codeInte,
            @Param("codeRisq") String codeRisq);

    boolean existsByNumeroPolice(String numeroPolice);

    Optional<Assure> findByTelephone(String telephone);
}
