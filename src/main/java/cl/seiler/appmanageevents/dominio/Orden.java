package cl.seiler.appmanageevents.dominio;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Orden {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** buy_order de Webpay: único por comercio, máximo 26 caracteres. */
    @Column(nullable = false, length = 26)
    private String codigo;

    /**
     * session_id de Webpay. Es aleatorio y no se muestra: cuando Webpay avisa
     * un abandono por tiempo solo manda buy_order y session_id, y sin este
     * secreto cualquiera que conociera el código podría anular la compra ajena.
     */
    @Column(name = "id_sesion", nullable = false, length = 61)
    private String idSesion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evento_id")
    private Evento evento;

    @Column(nullable = false)
    private int cantidad;

    @Column(name = "precio_unitario", nullable = false)
    private int precioUnitario;

    @Column(nullable = false)
    private int monto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoOrden estado = EstadoOrden.PENDIENTE;

    @Column(name = "token_webpay", length = 100)
    private String tokenWebpay;

    @Column(name = "codigo_autorizacion", length = 20)
    private String codigoAutorizacion;

    @Column(name = "tipo_pago", length = 10)
    private String tipoPago;

    private Integer cuotas;

    @Column(name = "tarjeta_ultimos4", length = 4)
    private String tarjetaUltimos4;

    @Column(name = "creada_en", nullable = false)
    private Instant creadaEn;

    @Column(name = "expira_en", nullable = false)
    private Instant expiraEn;

    @Column(name = "pagada_en")
    private Instant pagadaEn;

    @OneToMany(mappedBy = "orden", cascade = CascadeType.ALL)
    @OrderBy("id")
    private List<Entrada> entradas = new ArrayList<>();

    public boolean estaPagada() {
        return estado == EstadoOrden.PAGADA;
    }

    public String getDescripcionTipoPago() {
        if (tipoPago == null) {
            return "";
        }
        return switch (tipoPago) {
            case "VD" -> "Débito";
            case "VP" -> "Prepago";
            case "VN" -> "Crédito";
            case "VC", "SI", "S2", "NC" -> "Crédito en cuotas";
            default -> tipoPago;
        };
    }
}
