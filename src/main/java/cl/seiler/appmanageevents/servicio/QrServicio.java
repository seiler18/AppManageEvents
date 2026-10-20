package cl.seiler.appmanageevents.servicio;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;

@Service
public class QrServicio {

    public byte[] png(String contenido, int lado) {
        try {
            var matriz = new QRCodeWriter().encode(contenido, BarcodeFormat.QR_CODE, lado, lado,
                    Map.of(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M, EncodeHintType.MARGIN, 1));
            var salida = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matriz, "PNG", salida);
            return salida.toByteArray();
        } catch (WriterException e) {
            throw new IllegalArgumentException("No se pudo generar el QR", e);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
