package com.miempresa.facturas;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MainApp {

    public static void main( final String[] args ) {
        if ( args.length != 4 ) {
            System.err.println("Uso: facturas-processor-1.0.0.exe <rutaBase> <serviceUrl> <jsonFile> <token>");
            System.exit(1);
        }
        final String baseDir = args[0];
        final String serviceUrl = args[1];
        final String jsonFileName = args[2];
        final String token = args[3];

        // Configure logging antes de cualquier logger
        configureLogging(baseDir);

        try {
            final VerifactuClient client = new VerifactuClient( serviceUrl, token );
            final InvoiceProcessor processor = new InvoiceProcessor( baseDir, client );
            final int result = processor.process( jsonFileName );
            System.exit( result );
        } catch ( final Exception e ) {
            System.exit(1 );
        }
    }

    /**
     * Ajusta Logback para que escriba en "{baseDir}/logs".
     * Resetea el contexto de logging y recarga logback.xml usando esa carpeta.
     */
    private static void configureLogging(final String baseDir) {
        // 0) Asegurarnos de que la carpeta logs/ existe
        final Path logsPath = Paths.get(baseDir, "logs");
        try {
            Files.createDirectories(logsPath);
        } catch ( final IOException ioe ) {
            System.err.println("No pude crear la carpeta de logs en: " + logsPath);
            ioe.printStackTrace();
            System.exit(1);
        }

        // 1) Fijar la propiedad para el XML de Logback (usa ${LOG_DIR} en logback.xml)
        System.setProperty("LOG_DIR", logsPath.toString());

        // 2) Ubicación del logback.xml externo
        final File externalConfig = Paths.get( baseDir, "logback.xml" ).toFile();
        if ( !externalConfig.exists() ) {
            System.err.println( "No encuentro logback.xml en: " + externalConfig.getAbsolutePath() );
            System.exit( 1 );
        }

        // 3) Resetea y recarga Logback desde ese fichero externo
        final LoggerContext ctx = ( LoggerContext ) LoggerFactory.getILoggerFactory();
        ctx.reset();
        final JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext( ctx );
        try {
            configurator.doConfigure(externalConfig);
        } catch ( final Exception e ) {
            e.printStackTrace();
            System.exit( 1 );
        }
    }



}
