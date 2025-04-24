package com.miempresa.facturas;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public class InvoiceProcessor {
    private static final Logger logger = LoggerFactory.getLogger(InvoiceProcessor.class);
    private final Config config;
    private final VerifactuClient client;

    public InvoiceProcessor(Config config, VerifactuClient client) {
        this.config = config;
        this.client = client;
    }

    public int process(String jsonFileName) {
        Path inputPath = Path.of(config.getInputDir(), jsonFileName);
        try {
            String json = Files.readString(inputPath, StandardCharsets.UTF_8);
            ResponseDto dto = client.sendInvoice(json);
            logger.info("Factura {} enviada: estado={}, uuid={}, url={}",
                        jsonFileName, dto.getEstado(), dto.getUuid(), dto.getUrl());
            moveFile(inputPath, jsonFileName);
            return 0;
        } catch (RuntimeException e) {
            logger.error("Error HTTP al enviar {}: {}", jsonFileName, e.getMessage());
            moveFileWithStatus(jsonFileName, e.getMessage());
            return 2;
        } catch (Exception e) {
            logger.error("Error procesando {}: {}", jsonFileName, e.getMessage(), e);
            return 1;
        }
    }

    private void moveFile(Path source, String newName) {
        try {
            Files.move(source,
                       Path.of(config.getProcessedDir(), newName),
                       StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception ex) {
            logger.error("Error moviendo archivo {}: {}", source, ex.getMessage(), ex);
        }
    }

    private void moveFileWithStatus(String fileName, String status) {
        try {
            String base = fileName.contains(".") ?
                          fileName.substring(0, fileName.lastIndexOf('.')) : fileName;
            String newName = base + "_" + status.replaceAll("\\D+", "") + ".json";
            Files.move(Path.of(config.getInputDir(), fileName),
                       Path.of(config.getProcessedDir(), newName),
                       StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception ex) {
            logger.error("Error moviendo archivo con status {}: {}", fileName, ex.getMessage(), ex);
        }
    }
}
