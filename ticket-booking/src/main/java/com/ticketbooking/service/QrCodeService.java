package com.ticketbooking.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class QrCodeService {

    @Value("${app.qr-code-size}")
    private int qrCodeSize;

    @Value("${app.upload-dir}")
    private String uploadDir;

    public String generateQrCode(String data, String bookingRef) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();

        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.MARGIN, 2);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, qrCodeSize, qrCodeSize, hints);

        Path qrDir = Paths.get(uploadDir, "qrcodes");
        Files.createDirectories(qrDir);

        String fileName = "QR_" + bookingRef + ".png";
        Path filePath = qrDir.resolve(fileName);

        MatrixToImageWriter.writeToPath(bitMatrix, "PNG", filePath);
        log.info("QR code generated: {}", filePath);

        return filePath.toString();
    }

    public byte[] generateQrCodeBytes(String data) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();

        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.MARGIN, 2);

        BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, qrCodeSize, qrCodeSize, hints);

        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", baos);
        return baos.toByteArray();
    }
}
