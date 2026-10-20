package cl.seiler.appmanageevents.dominio;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
public class Entrada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** UUID aleatorio: es lo que lleva el QR, no se puede adivinar el del vecino. */
    @Column(nullable = false, length = 36)
    private String codigo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "orden_id")
    private Orden orden;

    @Column(name = "usada_en")
    private Instant usadaEn;

    public boolean isUsada() {
        return usadaEn != null;
    }

    /** Válida para entrar: orden pagada, evento no cancelado y sin usar. */
    public boolean isVigente() {
        return orden.estaPagada() && orden.getEvento().getEstado() != EstadoEvento.CANCELADO && !isUsada();
    }
}
