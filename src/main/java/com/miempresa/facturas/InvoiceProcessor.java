package com.miempresa.facturas;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class InvoiceProcessor {
    private static final Logger logger = LoggerFactory.getLogger(InvoiceProcessor.class);

    // 1) Rutas "hardcodeadas" en atributos estáticos
    private static final Path PENDING_DIR   = Paths.get("./facturas/pendientes");
    private static final Path PROCESSED_DIR = Paths.get("./facturas/procesadas");
    private static final Path ERROR_DIR     = Paths.get("./facturas/error");
    private static final String RESPONSE_TAG = "_respuesta";  // sufijo para el JSON de respuesta
    private static final String ERROR_TAG    = "_error.txt";  // sufijo para el .txt de error

    private final VerifactuClient client;

    public InvoiceProcessor( final VerifactuClient client ) {
        this.client = client;
    }

    public int process( final String fileName ) {
        final Path pending = PENDING_DIR.resolve( fileName );
        try {
            // 1) Leer el JSON original
            final String originalJson = Files.readString( pending, StandardCharsets.UTF_8 );

            // 2) Enviar y obtener la respuesta cruda
            final String responseJson = client.sendInvoice( originalJson );

            // 3) (Opcional) deserializar para loguear estado/uuid/url
            final ResponseDto dto = new ObjectMapper()
                    .readValue( responseJson, ResponseDto.class );
            logger.info("Factura {} enviada: estado={}, uuid={}, url={}",
                    fileName, dto.getEstado(), dto.getUuid(), dto.getUrl());

            // 4) Copiar el archivo original a "procesadas"
            final Path processedOriginal = PROCESSED_DIR.resolve( fileName );
            Files.copy( pending, processedOriginal, StandardCopyOption.REPLACE_EXISTING );

            // 5) Crear un nuevo JSON con la respuesta:
            final String base = fileName.endsWith(".json")
                    ? fileName.substring( 0, fileName.length() - 5 )
                    : fileName;
            final String respName = base + RESPONSE_TAG + ".json";
            final Path processedResponse = PROCESSED_DIR.resolve( respName );
            Files.writeString( processedResponse, responseJson, StandardCharsets.UTF_8 );

            // 6) Borrar el original de "pendientes"
            Files.delete( pending );

            logger.info("{} copiado y respuesta en {}", fileName, respName );
            return 0;

        } catch ( final RuntimeException e ) {
            logger.error( "Error HTTP enviando {}: {}", fileName, e.getMessage() );
            moveToError(fileName, e.getMessage());
            return 2;
        } catch ( final Exception e ) {
            logger.error("Error procesando {}: {}", fileName, e.getMessage(), e);
            moveToError(fileName, e.getMessage());
            return 1;
        }
    }

    private void moveToError( final String fileName, final String errorMsg ) {
        try {
            // 1) Base sin extensión
            final String base = fileName.endsWith( ".json" )
                    ? fileName.substring(0, fileName.length() - 5)
                    : fileName;
            // 2) Timestamp único para este intento
            final String timestamp = LocalDateTime.now()
                    .format( DateTimeFormatter.ofPattern("yyyyMMddHHmmss" ) );

            // 3) Copia del JSON con sufijo _yyyyMMddHHmmss.json
            final String errJsonName = base + "_" + timestamp + ".json";
            Files.copy(
                    PENDING_DIR.resolve( fileName ),
                    ERROR_DIR.resolve( errJsonName ),
                    StandardCopyOption.REPLACE_EXISTING
            );

            // 4) Creación del .txt con sufijo _yyyyMMddHHmmss_error.txt
            final String errTxtName = base + "_" + timestamp + "_error.txt";
            final Path txtPath = ERROR_DIR.resolve( errTxtName );
            final String content = "Error procesando " + fileName + ":\n" + errorMsg;
            Files.writeString(txtPath, content, StandardCharsets.UTF_8);

            // 5) Borrar el original de pendientes
            Files.delete( PENDING_DIR.resolve( fileName ) );

            logger.warn( "Factura {} movida a error como {} y log en {}",
                    fileName, errJsonName, errTxtName );

        } catch (Exception ex) {
            logger.error( "No se pudo mover {} a error: {}", fileName, ex.getMessage(), ex );
        }
    }

}
