package com.uab.sante.repository;

import com.uab.sante.entities.TauxCouverture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TauxCouvertureRepository extends JpaRepository<TauxCouverture, Long> {
    Optional<TauxCouverture> findByCode(String code);
}
