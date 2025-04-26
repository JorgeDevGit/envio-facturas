package com.miempresa.facturas;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public class InvoiceProcessor {
    private static final Logger logger = LoggerFactory.getLogger(InvoiceProcessor.class);
    private final Config config;
    private final VerifactuClient client;

    public InvoiceProcessor( final Config config, final VerifactuClient client) {
        this.config = config;
        this.client = client;
    }

    public int process( final String jsonFileName ) {
        final Path inputPath = Path.of( config.inputDir(), jsonFileName );
        try {
            // 1) Leer el JSON original
            final String originalJson = Files.readString(inputPath, StandardCharsets.UTF_8);

            // 2) Enviar y obtener la respuesta cruda
            final String responseJson = client.sendInvoice( originalJson);

            // 3) Deserializar para extraer estado/uuid/url
            final ResponseDto dto = new ObjectMapper().readValue(responseJson, ResponseDto.class);

            logger.info("Factura {} enviada: estado={}, uuid={}, url={}",
                    jsonFileName, dto.getEstado(), dto.getUuid(), dto.getUrl());

            // 4) Sobrescribir el archivo con la respuesta JSON
            Files.writeString( inputPath, responseJson, StandardCharsets.UTF_8 );

            // 5) Mover el fichero ya modificado
            moveFile( inputPath, jsonFileName );

            return 0;
        } catch ( final RuntimeException e ) {
            logger.error(" Error HTTP al enviar {}: {}", jsonFileName, e.getMessage());
            moveFileWithStatus( jsonFileName, e.getMessage() );
            return 2;
        } catch (Exception e) {
            logger.error("Error procesando {}: {}", jsonFileName, e.getMessage(), e);
            return 1;
        }
    }

    private void moveFile( final Path source, final String newName ) {
        try {
            Files.move( source, Path.of( config.processedDir(), newName ), StandardCopyOption.REPLACE_EXISTING );
        } catch ( final Exception ex) {
            logger.error( "Error moviendo archivo {}: {}", source, ex.getMessage(), ex );
        }
    }

    private void moveFileWithStatus(final String fileName, final String status) {
        try {
            // Nombre base sin extensión
            final String base = fileName.contains(".")
                    ? fileName.substring( 0, fileName.lastIndexOf('.') )
                    : fileName;
            // Construye el nuevo nombre, e.g. factura_400.json
            final String newName = base + "_" + status.replaceAll("\\D+", "") + ".json";

            // Origen: carpeta de pendientes
            final Path source = Path.of( config.inputDir(), fileName );
            // Destino: carpeta de error
            final Path target = Path.of( config.errorDir(), newName );

            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);

            logger.warn(" Archivo con error movido: {} → {}", source, target );
        } catch (final Exception ex) {
            logger.error("Error moviendo archivo con status {}: {}", fileName, ex.getMessage(), ex);
        }
    }

}
