package com.vivo.crm.casemanagement.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Entidade CaseNote - Representa uma nota/comentário do caso
 * Mapeado para TMF621 Note
 */
@Entity
@Table(name = "case_notes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaseNote {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "note_id")
    private String noteId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id")
    @ToString.Exclude
    private Case caseEntity;

    @Column(name = "text", columnDefinition = "TEXT")
    private String text;

    @Column(name = "author")
    private String author;

    @Column(name = "created_at")
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
    }
}
