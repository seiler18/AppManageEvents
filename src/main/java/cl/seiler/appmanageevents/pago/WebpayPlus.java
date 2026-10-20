package cl.seiler.appmanageevents.pago;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Map;

/**
 * Cliente de la API REST v1.2 de Webpay Plus. Se habla HTTP directo en vez de
 * usar el SDK de Transbank: son cuatro llamadas JSON y así no se arrastra una
 * dependencia más que actualizar junto con Spring.
 *
 * @see <a href="https://www.transbankdevelopers.cl/referencia/webpay">Referencia Webpay Plus</a>
 */
@Component
public class WebpayPlus implements PasarelaPago {

    private static final Logger log = LoggerFactory.getLogger(WebpayPlus.class);
    private static final String RUTA = "/rswebpaytransaction/api/webpay/v1.2/transactions";

    private final RestClient http;

    public WebpayPlus(TransbankProperties tbk) {
        HttpClient cliente = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        JdkClientHttpRequestFactory fabrica = new JdkClientHttpRequestFactory(cliente);
        fabrica.setReadTimeout(Duration.ofSeconds(30));
        this.http = RestClient.builder()
                .baseUrl(tbk.urlApi() + RUTA)
                .requestFactory(fabrica)
                .defaultHeader("Tbk-Api-Key-Id", tbk.codigoComercio())
                .defaultHeader("Tbk-Api-Key-Secret", tbk.apiKey())
                .build();
        log.info("Webpay Plus en ambiente de {}", tbk.esProduccion() ? "PRODUCCIÓN" : "integración");
    }

    @Override
    public TransaccionIniciada crear(String ordenCompra, String idSesion, int monto, String urlRetorno) {
        Map<String, Object> cuerpo = Map.of(
                "buy_order", ordenCompra,
                "session_id", idSesion,
                "amount", monto,
                "return_url", urlRetorno);
        RespuestaCreacion r = llamar("crear", () -> http.post()
                .contentType(MediaType.APPLICATION_JSON)
                .body(cuerpo)
                .retrieve()
                .body(RespuestaCreacion.class));
        return new TransaccionIniciada(r.token(), r.url());
    }

    @Override
    public ResultadoPago confirmar(String token) {
        return llamar("confirmar", () -> http.put()
                .uri("/{token}", token)
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(RespuestaTransaccion.class)).aResultado();
    }

    @Override
    public ResultadoPago consultar(String token) {
        return llamar("consultar", () -> http.get()
                .uri("/{token}", token)
                .retrieve()
                .body(RespuestaTransaccion.class)).aResultado();
    }

    @Override
    public void reembolsar(String token, int monto) {
        llamar("reembolsar", () -> http.post()
                .uri("/{token}/refunds", token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("amount", monto))
                .retrieve()
                .toBodilessEntity());
    }

    private <T> T llamar(String operacion, java.util.function.Supplier<T> llamada) {
        try {
            T respuesta = llamada.get();
            if (respuesta == null) {
                throw new PagoException("Webpay respondió vacío al " + operacion, null);
            }
            return respuesta;
        } catch (RestClientException e) {
            throw new PagoException("Webpay falló al " + operacion + ": " + e.getMessage(), e);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record RespuestaCreacion(String token, String url) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record RespuestaTransaccion(
            String status,
            @JsonProperty("response_code") Integer responseCode,
            Integer amount,
            @JsonProperty("buy_order") String buyOrder,
            @JsonProperty("session_id") String sessionId,
            @JsonProperty("authorization_code") String authorizationCode,
            @JsonProperty("payment_type_code") String paymentTypeCode,
            @JsonProperty("installments_number") Integer installmentsNumber,
            @JsonProperty("card_detail") Tarjeta cardDetail) {

        ResultadoPago aResultado() {
            return new ResultadoPago(status, responseCode, amount, buyOrder, sessionId, authorizationCode,
                    paymentTypeCode, installmentsNumber, cardDetail == null ? null : cardDetail.cardNumber());
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Tarjeta(@JsonProperty("card_number") String cardNumber) {
    }
}
