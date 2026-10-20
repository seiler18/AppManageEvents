package cl.seiler.appmanageevents.servicio;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.UUID;

final class Codigos {

    private static final SecureRandom AZAR = new SecureRandom();
    // Sin 0/O ni 1/I: el código de orden se dicta por teléfono a soporte.
    private static final char[] ALFABETO = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ".toCharArray();
    private static final DateTimeFormatter AAMMDD = DateTimeFormatter.ofPattern("yyMMdd");

    private Codigos() {
    }

    /** buy_order de Webpay: «AME» + fecha + 8 caracteres, 17 en total (el máximo es 26). */
    static String orden(LocalDate hoy) {
        StringBuilder sb = new StringBuilder("AME").append(hoy.format(AAMMDD));
        for (int i = 0; i < 8; i++) {
            sb.append(ALFABETO[AZAR.nextInt(ALFABETO.length)]);
        }
        return sb.toString();
    }

    static String secreto() {
        byte[] bytes = new byte[16];
        AZAR.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    static String entrada() {
        return UUID.randomUUID().toString();
    }
}
