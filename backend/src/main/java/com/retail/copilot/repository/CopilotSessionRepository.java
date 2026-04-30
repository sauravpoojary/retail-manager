package com.retail.copilot.repository;

import com.retail.copilot.model.CopilotSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CopilotSessionRepository extends JpaRepository<CopilotSession, UUID> {
}
