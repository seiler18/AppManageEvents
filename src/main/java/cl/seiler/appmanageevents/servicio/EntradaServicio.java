package cl.seiler.appmanageevents.servicio;

import cl.seiler.appmanageevents.dominio.Entrada;
import cl.seiler.appmanageevents.dominio.EstadoEvento;
import cl.seiler.appmanageevents.repositorio.EntradaRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.Optional;

@Service
public class EntradaServicio {

    public enum Veredicto { VALIDA, YA_USADA, NO_VIGENTE, NO_EXISTE }

    public record Validacion(Veredicto veredicto, Entrada entrada) {
    }

    private final EntradaRepositorio entradas;
    private final Clock reloj;

    public EntradaServicio(EntradaRepositorio entradas, Clock reloj) {
        this.entradas = entradas;
        this.reloj = reloj;
    }

    @Transactional(readOnly = true)
    public Optional<Entrada> buscar(String codigo) {
        return entradas.findByCodigo(codigo);
    }

    /** Lo que ve la puerta antes de dejar pasar: no marca nada. */
    @Transactional(readOnly = true)
    public Validacion revisar(String codigo) {
        return entradas.findByCodigo(codigo.trim())
                .map(e -> new Validacion(veredicto(e), e))
                .orElse(new Validacion(Veredicto.NO_EXISTE, null));
    }

    /**
     * Marca la entrada como usada con un UPDATE condicionado a que siga sin
     * usar: si dos puertas leen el mismo QR a la vez, solo una lo consigue.
     */
    @Transactional
    public Validacion marcarUsada(String codigo) {
        Optional<Entrada> encontrada = entradas.findByCodigo(codigo.trim());
        if (encontrada.isEmpty()) {
            return new Validacion(Veredicto.NO_EXISTE, null);
        }
        Entrada e = encontrada.get();
        Veredicto v = veredicto(e);
        if (v == Veredicto.VALIDA) {
            if (entradas.marcarUsada(e.getId(), reloj.instant()) == 0) {
                return new Validacion(Veredicto.YA_USADA, e);
            }
            e.setUsadaEn(reloj.instant());
        }
        return new Validacion(v, e);
    }

    private Veredicto veredicto(Entrada e) {
        if (!e.getOrden().estaPagada() || e.getOrden().getEvento().getEstado() == EstadoEvento.CANCELADO) {
            return Veredicto.NO_VIGENTE;
        }
        return e.isUsada() ? Veredicto.YA_USADA : Veredicto.VALIDA;
    }
}
