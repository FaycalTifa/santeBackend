package com.uab.sante.repository;

import com.uab.sante.entities.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByUtilisateurIdOrderByCreatedAtDesc(Long utilisateurId);

    List<AuditLog> findByEntiteAndEntiteIdOrderByCreatedAtDesc(String entite, Long entiteId);

    List<AuditLog> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime debut, LocalDateTime fin);
}
