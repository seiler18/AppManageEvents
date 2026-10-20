package cl.seiler.appmanageevents.dominio;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(length = 5000)
    private String descripcion;

    /** Hora local de Chile: es la que se imprime en la entrada. */
    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "salon_id")
    private Salon salon;

    /** Pesos chilenos, sin decimales: es lo que acepta Webpay. */
    @Column(nullable = false)
    private int precio;

    @Column(name = "cupo_maximo", nullable = false)
    private int cupoMaximo;

    /**
     * Entradas pagadas más las retenidas por compras pendientes. Se mueve solo
     * con la fila del evento bloqueada (ver {@code EventoRepositorio.bloquear}).
     */
    @Column(name = "entradas_reservadas", nullable = false)
    private int entradasReservadas;

    @Column(name = "max_por_compra", nullable = false)
    private int maxPorCompra = 10;

    @Column(name = "imagen_url", length = 500)
    private String imagenUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoEvento estado = EstadoEvento.BORRADOR;

    @Column(name = "recordatorio_enviado", nullable = false)
    private boolean recordatorioEnviado;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn;

    public int getDisponibles() {
        return Math.max(0, cupoMaximo - entradasReservadas);
    }

    public boolean isGratuito() {
        return precio == 0;
    }

    public int getPorcentajeOcupacion() {
        return cupoMaximo == 0 ? 0 : Math.min(100, entradasReservadas * 100 / cupoMaximo);
    }

    public boolean estaPublicado() {
        return estado == EstadoEvento.PUBLICADO;
    }
}
