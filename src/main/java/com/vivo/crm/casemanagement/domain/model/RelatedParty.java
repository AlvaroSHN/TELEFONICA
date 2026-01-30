package com.vivo.crm.casemanagement.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Entidade RelatedParty - Representa uma parte relacionada ao caso
 * Mapeado para TMF621 RelatedParty
 */
@Entity
@Table(name = "case_related_parties")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RelatedParty {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id")
    @ToString.Exclude
    private Case caseEntity;

    @Column(name = "party_type")
    private String referredType; // Account, Contact

    @Column(name = "party_id")
    private String partyId;

    @Column(name = "party_name")
    private String name;

    @Column(name = "role")
    private String role;

    @Column(name = "created_at")
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
    }
}
