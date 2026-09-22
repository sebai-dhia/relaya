package com.relaya.demo.admin.service;

import com.relaya.demo.analysis.adapter.out.persistence.SpringDataUsageLedgerRepository;
import com.relaya.demo.analysis.adapter.out.persistence.UsageLedgerJpaEntity;
import com.relaya.demo.analysis.domain.AnalysisDraft;
import com.relaya.demo.analysis.port.out.AnalysisDraftRepositoryPort;
import com.relaya.demo.analysis.port.out.UsageLedgerPort;
import com.relaya.demo.board.domain.BoardWrite;
import com.relaya.demo.board.port.out.BoardWriteRepositoryPort;
import com.relaya.demo.intake.domain.Intake;
import com.relaya.demo.intake.port.out.IntakeRepositoryPort;
import com.relaya.demo.review.domain.Approval;
import com.relaya.demo.review.port.out.ApprovalRepositoryPort;
import com.relaya.demo.user.SpringDataUserRepository;
import com.relaya.demo.user.UserJpaEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AdminService {

	private static final BigDecimal MONTHLY_BUDGET = new BigDecimal("5.00");

	private final UsageLedgerPort usageLedgerPort;
	private final SpringDataUsageLedgerRepository usageRepo;
	private final BoardWriteRepositoryPort boardWriteRepo;
	private final ApprovalRepositoryPort approvalRepo;
	private final AnalysisDraftRepositoryPort draftRepo;
	private final IntakeRepositoryPort intakeRepo;
	private final SpringDataUserRepository userRepo;
	private final PasswordEncoder passwordEncoder;

	public AdminService(
			UsageLedgerPort usageLedgerPort,
			SpringDataUsageLedgerRepository usageRepo,
			BoardWriteRepositoryPort boardWriteRepo,
			ApprovalRepositoryPort approvalRepo,
			AnalysisDraftRepositoryPort draftRepo,
			IntakeRepositoryPort intakeRepo,
			SpringDataUserRepository userRepo,
			PasswordEncoder passwordEncoder
	) {
		this.usageLedgerPort = Objects.requireNonNull(usageLedgerPort, "usageLedgerPort must not be null");
		this.usageRepo = Objects.requireNonNull(usageRepo, "usageRepo must not be null");
		this.boardWriteRepo = Objects.requireNonNull(boardWriteRepo, "boardWriteRepo must not be null");
		this.approvalRepo = Objects.requireNonNull(approvalRepo, "approvalRepo must not be null");
		this.draftRepo = Objects.requireNonNull(draftRepo, "draftRepo must not be null");
		this.intakeRepo = Objects.requireNonNull(intakeRepo, "intakeRepo must not be null");
		this.userRepo = Objects.requireNonNull(userRepo, "userRepo must not be null");
		this.passwordEncoder = Objects.requireNonNull(passwordEncoder, "passwordEncoder must not be null");
	}

	@Transactional(readOnly = true)
	public UsageSummary getUsageSummary(UUID tenantId) {
		YearMonth currentMonth = YearMonth.now(ZoneOffset.UTC);
		Instant startOfMonth = currentMonth.atDay(1).atStartOfDay(ZoneOffset.UTC).toInstant();

		BigDecimal totalSpent = usageLedgerPort.getMonthToDateCostUsd(tenantId, currentMonth);
		BigDecimal remaining = MONTHLY_BUDGET.subtract(totalSpent).max(BigDecimal.ZERO);

		List<UsageLedgerJpaEntity> entries = usageRepo.findAllByTenantIdAndCalledAtGreaterThanEqual(tenantId, startOfMonth);
		int totalTokens = entries.stream().mapToInt(e -> e.getPromptTokens() + e.getCompletionTokens()).sum();

		Map<String, List<UsageLedgerJpaEntity>> byModel = entries.stream()
				.collect(Collectors.groupingBy(UsageLedgerJpaEntity::getModel));

		List<ModelUsageBreakdown> breakdowns = new ArrayList<>();
		byModel.forEach((model, list) -> {
			int calls = list.size();
			int prompt = list.stream().mapToInt(UsageLedgerJpaEntity::getPromptTokens).sum();
			int completion = list.stream().mapToInt(UsageLedgerJpaEntity::getCompletionTokens).sum();
			BigDecimal cost = list.stream().map(UsageLedgerJpaEntity::getEstimatedCostUsd).reduce(BigDecimal.ZERO, BigDecimal::add);
			breakdowns.add(new ModelUsageBreakdown(model, calls, prompt, completion, cost));
		});

		return new UsageSummary(currentMonth.toString(), totalSpent, MONTHLY_BUDGET, remaining, totalTokens, breakdowns);
	}

	@Transactional(readOnly = true)
	public List<FailedWriteSummary> getFailedWrites(UUID tenantId) {
		List<BoardWrite> failedWrites = boardWriteRepo.findFailedWrites(tenantId);
		List<FailedWriteSummary> summaries = new ArrayList<>();

		for (BoardWrite write : failedWrites) {
			String clientLabel = "Unknown";
			Optional<Approval> appOpt = approvalRepo.findById(tenantId, write.getApprovalId());
			if (appOpt.isPresent()) {
				Optional<AnalysisDraft> draftOpt = draftRepo.findById(tenantId, appOpt.get().getDraftId());
				if (draftOpt.isPresent()) {
					Optional<Intake> intakeOpt = intakeRepo.findById(tenantId, draftOpt.get().getIntakeId());
					if (intakeOpt.isPresent()) {
						clientLabel = intakeOpt.get().getClientLabel();
					}
				}
			}
			summaries.add(new FailedWriteSummary(
					write.getId(),
					write.getApprovalId(),
					clientLabel,
					write.getStatus().name(),
					write.getErrorDetails(),
					write.getCreatedAt()
			));
		}
		return summaries;
	}

	@Transactional(readOnly = true)
	public List<UserJpaEntity> listUsers(UUID tenantId) {
		return userRepo.findByTenantId(tenantId);
	}

	@Transactional
	public UserJpaEntity createUser(UUID tenantId, String email, String password, String role) {
		if (userRepo.findByTenantIdAndEmail(tenantId, email).isPresent()) {
			throw new IllegalStateException("User already exists with email: " + email);
		}
		UserJpaEntity user = UserJpaEntity.builder()
				.id(UUID.randomUUID())
				.tenantId(tenantId)
				.email(email)
				.passwordHash(passwordEncoder.encode(password))
				.role(role != null ? role : "ROLE_REVIEWER")
				.active(true)
				.createdAt(Instant.now())
				.build();
		return userRepo.save(user);
	}

	@Transactional
	public UserJpaEntity setUserActiveStatus(UUID tenantId, UUID userId, boolean active) {
		UserJpaEntity user = userRepo.findById(userId)
				.orElseThrow(() -> new NoSuchElementException("User not found: " + userId));
		if (!user.getTenantId().equals(tenantId)) {
			throw new NoSuchElementException("User not found in tenant: " + userId);
		}
		user.setActive(active);
		return userRepo.save(user);
	}

	public record UsageSummary(
			String billingCycle,
			BigDecimal totalSpentUsd,
			BigDecimal budgetLimitUsd,
			BigDecimal remainingUsd,
			int totalTokens,
			List<ModelUsageBreakdown> breakdownByModel
	) {}

	public record ModelUsageBreakdown(
			String model,
			int calls,
			int promptTokens,
			int completionTokens,
			BigDecimal costUsd
	) {}

	public record FailedWriteSummary(
			UUID boardWriteId,
			UUID approvalId,
			String intakeClientLabel,
			String status,
			String errorDetails,
			Instant createdAt
	) {}
}