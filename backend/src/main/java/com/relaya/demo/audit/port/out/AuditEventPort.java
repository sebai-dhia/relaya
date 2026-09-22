package com.relaya.demo.audit.port.out;

import com.relaya.demo.audit.domain.AuditEvent;
import java.util.List;
import java.util.UUID;

public interface AuditEventPort {

	void recordEvent(AuditEvent event);

	List<AuditEvent> findByIntakeId(UUID tenantId, UUID intakeId);

	List<AuditEvent> findByTenantId(UUID tenantId);
}