package controllers.mariem;

import com.google.zxing.WriterException;
import services.mariem.QRCodeService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.io.IOException;

@RestController
public class QRCodeController {

    @GetMapping(value = "/api/qrcode", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> generateQRCode(
            @RequestParam String depart,       // Nom du paramètre simplifié
            @RequestParam String destination,
            @RequestParam String date,
            @RequestParam double prix,         // Type numérique pour le prix
            @RequestParam(defaultValue = "250") int width,
            @RequestParam(defaultValue = "250") int height)
            throws IOException, WriterException {

        // Formatage professionnel avec labels en français
        String qrCodeText = String.format(
                "Voyage:\nDépart: %s\nDestination: %s\nDate: %s\nPrix: %.2f€",
                depart, destination, date, prix);

        byte[] qrCodeImage = QRCodeService.generateQRCodeImage(qrCodeText, width, height);
        return ResponseEntity.ok().body(qrCodeImage);
    }
}