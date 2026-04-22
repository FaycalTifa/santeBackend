// repository/PlafonnementConsultationRepository.java
package com.uab.sante.repository;

import com.uab.sante.entities.PlafonnementConsultation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlafonnementConsultationRepository extends JpaRepository<PlafonnementConsultation, Long> {

    Optional<PlafonnementConsultation> findByCodeInteAndTypeConsultationAndActifTrue(String codeInte, String typeConsultation);

    List<PlafonnementConsultation> findByCodeInteAndActifTrue(String codeInte);

    List<PlafonnementConsultation> findByActifTrueOrderByCodeInte();
}
