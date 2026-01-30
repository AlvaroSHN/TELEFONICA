package com.vivo.crm.casemanagement.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Entidade Case - Representa um ticket/caso de atendimento
 * Mapeado para o padrão TMF621 (TroubleTicket)
 */
@Entity
@Table(name = "cases")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Case {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "case_id")
    private String caseId;

    @Column(name = "protocol", unique = true)
    private String protocol;

    // === Tipificação ===
    @Column(name = "ticket_type")
    private String ticketType;

    @Column(name = "category")
    private String category;

    @Column(name = "subcategory")
    private String subcategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority")
    private CasePriority priority;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity")
    private CaseSeverity severity;

    // === Informações do Cliente ===
    @Column(name = "customer_id")
    private String customerId;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "customer_segment")
    private String customerSegment;

    // === Estado ===
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private CaseStatus status;

    // === Conteúdo ===
    @Column(name = "subject")
    private String subject;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "resolution", columnDefinition = "TEXT")
    private String resolution;

    // === Canal de Origem ===
    @Column(name = "channel")
    private String channel;

    @Column(name = "channel_name")
    private String channelName;

    // === Salesforce ===
    @Column(name = "salesforce_case_id")
    private String salesforceCaseId;

    @Column(name = "salesforce_case_number")
    private String salesforceCaseNumber;

    // === Características Customizadas (TMF TicketCharacteristic) ===
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ticket_characteristics", columnDefinition = "TEXT")
    @Builder.Default
    private Map<String, String> ticketCharacteristics = new HashMap<>();

    // === Notas ===
    @OneToMany(mappedBy = "caseEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CaseNote> notes = new ArrayList<>();

    // === Partes Relacionadas ===
    @OneToMany(mappedBy = "caseEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RelatedParty> relatedParties = new ArrayList<>();

    // === Auditoria ===
    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(name = "resolved_by")
    private String resolvedBy;

    @Version
    private Long version;

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        if (this.status == null) {
            this.status = CaseStatus.NEW;
        }
        if (this.priority == null) {
            this.priority = CasePriority.MEDIUM;
        }
        if (this.protocol == null) {
            this.protocol = generateProtocol();
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

    private String generateProtocol() {
        return "VIVO-" + System.currentTimeMillis();
    }

    public void addNote(CaseNote note) {
        notes.add(note);
        note.setCaseEntity(this);
    }

    public void addRelatedParty(RelatedParty party) {
        relatedParties.add(party);
        party.setCaseEntity(this);
    }
}
