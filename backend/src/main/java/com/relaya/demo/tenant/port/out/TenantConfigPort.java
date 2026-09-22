package com.relaya.demo.tenant.port.out;

import java.util.Optional;
import java.util.UUID;

public interface TenantConfigPort {

	Optional<String> findConfigValue(UUID tenantId, String configKey);
}