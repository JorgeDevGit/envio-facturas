package com.miempresa.facturas;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

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

    private final VerifactuClient client;

    public InvoiceProcessor(final String appPath, final VerifactuClient client) {
        this.client = client;
        this.pendingDir = Paths.get(appPath.concat("/facturas/pendientes"));
        this.processedDir = Paths.get(appPath.concat("/facturas/procesadas"));
        this.errorDir = Paths.get(appPath.concat("/facturas/error"));
    }

    /**
     * Procesa una factura JSON según el código HTTP:
     * - 200 → mueve factura + response a 'procesadas'
     * - 400 → mueve factura + response a 'error'
     * - 500 → deja factura en 'pendientes', descarta la response
     * - cualquier otra excepción (timeout, red, etc.) → deja factura en 'pendientes'
     *
     * @param fileName el fichero JSON dentro de 'pendientes'
     * @return 0 si OK, 2 si Bad Request, 3 factura no encontrada, 1 en resto de errores
     */
    public int process(final String fileName) {
        logger.info("===== INICIO PROCESO FACTURA {} =====", fileName);
        final Path pending = pendingDir.resolve(fileName);

        if ( Files.notExists( pending ) ) {
            logger.warn("Factura {} no encontrada en pendientes: se omite el procesamiento.", fileName );
            logger.info("===== FIN PROCESO FACTURA {} =====", fileName);
            return 3;
        }

        final String base = fileName.endsWith(".json")
                ? fileName.substring(0, fileName.length() - 5)
                : fileName;

        try {
            // 1) Leer JSON original
            final String originalJson = Files.readString(pending, StandardCharsets.UTF_8);

            // 2) Enviar y capturar excepciones HTTP
            final String responseJson = client.callRestService(originalJson);

            // Si no lanza excepción, asumimos 200
            move( pending, processedDir, fileName );
            write( processedDir, base + RESPONSE_TAG + ".json", responseJson );
            logger.info( "200 OK: factura y respuesta movidas a 'procesadas'" );
            return 0;

        } catch ( final HttpClientErrorException.BadRequest badReq ) {
            // 400: Bad Request
            final String resp = badReq.getResponseBodyAsString();
            moveToError(pending, base, resp, "Bad Request 400");
            logger.warn( "400 Bad Request: factura y respuesta movidas a 'error': {}", badReq.getMessage() );
            return 2;

        } catch (final HttpServerErrorException serverErr) {
            // 500: Server Error
            logger.error( "500 Server Error: factura queda en 'pendientes': {}", serverErr.getMessage() );
            return 1;

        } catch (final Exception ex) {
            // timeout, red, parseos, IO…
            logger.error( "NO RESPONSE / EXCEPCION: factura queda en 'pendientes': {}", ex.getMessage() );
            return 1;

        } finally {
            logger.info( "===== FIN PROCESO FACTURA {} =====", fileName );
        }
    }

    private void move(final Path src, final Path targetDir, final String fileName) {
        try {
            Files.createDirectories(targetDir);
            Files.copy(src, targetDir.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
            Files.delete(src);
        } catch (final Exception e) {
            logger.error("Error moviendo {} a {}: {}", fileName, targetDir, e.getMessage());
        }
    }

    private void write(final Path dir, final String name, final String content) {
        try {
            Files.createDirectories(dir);
            Files.writeString(dir.resolve(name), content, StandardCharsets.UTF_8);
        } catch (final Exception e) {
            logger.error("Error escribiendo {} en {}: {}", name, dir, e.getMessage());
        }
    }

    private void moveToError( final Path pendingFile, final String baseName, final String errorMsg,
                             final String errorLabel ) {
        try {
            Files.createDirectories( errorDir );
            // timestamp único
            final String timestamp = LocalDateTime.now()
                    .format( DateTimeFormatter.ofPattern( "yyyyMMddHHmmss" ) );

            // 1) Copiamos el JSON original con timestamp
            final String errorJsonName = baseName + ".json";
            Files.copy( pendingFile, errorDir.resolve( errorJsonName ), StandardCopyOption.REPLACE_EXISTING );

            // 2) Creamos el .txt con el mismo timestamp y el mensaje de error
            final String txtName = baseName + "_" + timestamp + "_error.txt";
            final String content = errorLabel + " procesando " + baseName + ".json:\n" + errorMsg;
            Files.writeString( errorDir.resolve( txtName ), content, StandardCharsets.UTF_8 );

            // 3) Borramos el original de pendientes
            Files.deleteIfExists( pendingFile );

        } catch ( final Exception e ) {
            logger.error( "No se pudo mover o loguear error de {}: {}", baseName, e.getMessage(), e );
        }

    }
}
