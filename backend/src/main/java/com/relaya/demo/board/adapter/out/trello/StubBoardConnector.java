package com.relaya.demo.board.adapter.out.trello;

import com.relaya.demo.analysis.domain.AnalysisDraft;
import com.relaya.demo.board.domain.WriteStatus;
import com.relaya.demo.board.port.out.BoardConnector;
import com.relaya.demo.board.port.out.BoardWriteContext;
import com.relaya.demo.board.port.out.BoardWriteResult;
import com.relaya.demo.tenant.port.out.TenantConfigPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;

@Component
public class StubBoardConnector implements BoardConnector {

	private static final Logger log = LoggerFactory.getLogger(StubBoardConnector.class);

	private final TenantConfigPort tenantConfigPort;

	public StubBoardConnector(TenantConfigPort tenantConfigPort) {
		this.tenantConfigPort = Objects.requireNonNull(tenantConfigPort, "tenantConfigPort must not be null");
	}

	@Override
	public BoardWriteResult writeCard(BoardWriteContext context) {
		Objects.requireNonNull(context, "context must not be null");

		String boardId = tenantConfigPort.findConfigValue(context.tenantId(), "TRELLO_BOARD_ID").orElse("STUB");
		AnalysisDraft draft = context.draft();

		log.info("Executing board write in STUB mode for tenant: {}, client: {}, boardId: {}",
				context.tenantId(), context.clientLabel(), boardId);

		String cardTitle = "[" + context.clientLabel() + "] Website Delivery Handoff";
		StringBuilder description = new StringBuilder();
		description.append("### Approved Delivery Scope\n\n");
		description.append("**Idempotency Key:** `").append(context.idempotencyKey().value()).append("`\n\n");
		description.append("#### Deliverables Checklist:\n");
		draft.getDeliverables().forEach(d -> description.append("- [ ] ").append(d.text()).append("\n"));
		description.append("\n#### Proposed Actionable Tasks:\n");
		draft.getProposedTasks().forEach(t -> description.append("- [ ] ").append(t.title())
				.append(" (").append(t.estimateDays()).append(" days)\n"));

		log.debug("Simulated Trello Card Payload:\nTitle: {}\n{}", cardTitle, description);

		String stubCardId = "stub-card-" + UUID.randomUUID();
		String stubCardUrl = "https://trello.com/c/" + stubCardId;

		return new BoardWriteResult(WriteStatus.STUB, stubCardId, stubCardUrl, null);
	}
}