package com.uab.sante.repository;

import com.uab.sante.entities.ResultatExamen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResultatExamenRepository extends JpaRepository<ResultatExamen, Long> {

    List<ResultatExamen> findByPrescriptionExamenId(Long prescriptionExamenId);

    void deleteByPrescriptionExamenId(Long prescriptionExamenId);
}
