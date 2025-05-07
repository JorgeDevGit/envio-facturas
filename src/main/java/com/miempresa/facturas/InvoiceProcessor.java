package com.miempresa.facturas;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class InvoiceProcessor {
    private static final Logger logger = LoggerFactory.getLogger(InvoiceProcessor.class);

    // 1) Rutas "hardcodeadas" en atributos estáticos
    private final Path pendingDir;
    private final Path processedDir;
    private final Path errorDir;
    private static final String RESPONSE_TAG = "_respuesta";  // sufijo para el JSON de respuesta
    private static final String ERROR_TAG    = "_error.txt";  // sufijo para el .txt de error

    private final VerifactuClient client;

    public InvoiceProcessor( final String appPath, final VerifactuClient client ) {
        this.client = client;
        this.pendingDir = Paths.get( appPath.concat("/facturas/pendientes" ) );
        this.processedDir = Paths.get( appPath.concat("/facturas/procesadas" ) );
        this.errorDir = Paths.get( appPath.concat("/facturas/error" ) );
    }

    public int process( final String fileName ) {
        logger.info( "\n===== INICIO PROCESO FACTURA {} =====", fileName );
        final Path pending = pendingDir.resolve( fileName );
        try {
            // 1) Leer el JSON original
            final String originalJson = Files.readString( pending, StandardCharsets.UTF_8 );

            // 2) Enviar y obtener la respuesta cruda
            final String responseJson = client.callRestService( originalJson );

            logger.info( "Json con la respuesta: {}", responseJson );

            // 3) Copiar el archivo original a "procesadas"
            final Path processedOriginal = processedDir.resolve( fileName );
            Files.copy( pending, processedOriginal, StandardCopyOption.REPLACE_EXISTING );

            // 4) Crear un nuevo JSON con la respuesta:
            final String base = fileName.endsWith(".json")
                    ? fileName.substring( 0, fileName.length() - 5 )
                    : fileName;
            final String respName = base + RESPONSE_TAG + ".json";
            final Path processedResponse = processedDir.resolve( respName );
            Files.writeString( processedResponse, responseJson, StandardCharsets.UTF_8 );

            // 5) Borrar el original de "pendientes"
            Files.delete( pending );

            logger.info( "{} copiado y respuesta en {}", fileName, respName );
            logger.info( "----- PROCESADO CORRECTO: {} -----", fileName );
            logger.info( "\n===== FIN PROCESO FACTURA {} =====\n", fileName );
            return 0;

        } catch ( final RuntimeException e ) {
            logger.error( "Error HTTP enviando {}: {}", fileName, e.getMessage() );
            moveToError(fileName, e.getMessage());
            logger.info( "\n===== FIN PROCESO FACTURA {} =====\n", fileName );
            return 2;
        } catch ( final Exception e ) {
            logger.error("Error procesando {}: {}", fileName, e.getMessage(), e);
            moveToError(fileName, e.getMessage());
            logger.info( "\n===== FIN PROCESO FACTURA {} =====\n", fileName );
            return 1;
        }
    }

    private void moveToError( final String fileName, final String errorMsg ) {
        try {
            // 1) Base sin extensión
            final String base = fileName.endsWith( ".json" )
                    ? fileName.substring(0, fileName.length() - 5)
                    : fileName;
            // 2) Timestamp unico para este intento
            final String timestamp = LocalDateTime.now()
                    .format( DateTimeFormatter.ofPattern("yyyyMMddHHmmss" ) );

            // 3) Copia del JSON original a la carpeta error
            Files.copy(
                    pendingDir.resolve( fileName ),
                    errorDir.resolve( fileName ),
                    StandardCopyOption.REPLACE_EXISTING
            );

            // 4) Creacion del .txt con sufijo _yyyyMMddHHmmss_error.txt
            final String errTxtName = base + "_" + timestamp + ERROR_TAG;
            final Path txtPath = errorDir.resolve( errTxtName );
            final String content = "Error procesando " + fileName + ":\n" + errorMsg;
            Files.writeString( txtPath, content, StandardCharsets.UTF_8 );

            // 5) Borrar el original de pendientes
            final String errJsonName = base + "_" + timestamp + ".json";
            Files.delete( pendingDir.resolve( fileName ) );

            logger.warn( "Factura {} movida a /error como {} y log en {}",
                    fileName, errJsonName, errTxtName );

        } catch ( final Exception ex ) {
            logger.error( "No se pudo mover {} a /error: {}", fileName, ex.getMessage(), ex );
        }
    }

}
