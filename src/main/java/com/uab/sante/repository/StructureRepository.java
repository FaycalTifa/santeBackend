package com.uab.sante.repository;

import com.uab.sante.entities.Structure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StructureRepository extends JpaRepository<Structure, Long> {

    Optional<Structure> findByCodeStructure(String codeStructure);

    List<Structure> findByTypeAndActifTrue(Structure.TypeStructure type);

    List<Structure> findByActifTrueOrderByNomAsc();



    List<Structure> findByNomContainingIgnoreCaseAndActifTrue(String nom);
}
