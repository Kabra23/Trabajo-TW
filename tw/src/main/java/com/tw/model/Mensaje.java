package com.tw.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mensajes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Mensaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El asunto no puede estar vacio")
    @Column(nullable = false)
    private String asunto;

    @NotBlank(message = "El contenido no puede estar vacio")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String contenido;

    @Builder.Default
    private LocalDateTime fecha = LocalDateTime.now();

    /** false = no leido, true = leido */
    @Builder.Default
    private Boolean leido = false;

    /** true = eliminado por el destinatario (no aparece en su bandeja) */
    @Builder.Default
    private Boolean eliminadoDestinatario = false;

    /** true = eliminado por el remitente (no aparece en su enviados) */
    @Builder.Default
    private Boolean eliminadoRemitente = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "remitente_id", nullable = false)
    private Usuario remitente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destinatario_id", nullable = false)
    private Usuario destinatario;
}
