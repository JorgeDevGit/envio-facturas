package com.miempresa.facturas;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainApp {

    private static final String TOKEN = "vf_test_OKJV2MjUOY7UncNkcycWnjaFQzb5jjZybN8U0jWr9qs=";
    private static final Logger logger = LoggerFactory.getLogger( MainApp.class );
    public static void main( final String[] args ) {
        if ( args.length != 1 ) {
            logger.error( "Uso: java -jar facturas-processor.jar <nombre-json>" );
            System.exit( 1 );
        }
        final String jsonFileName = args[ 0 ];

        try {
            final Config config = ConfigLoader.load( );
            final VerifactuClient client = new VerifactuClient( config.baseUrl(), TOKEN );
            final InvoiceProcessor processor = new InvoiceProcessor( config, client );
            final int result = processor.process( jsonFileName );
            System.exit( result );
        } catch ( Exception e ) {
            logger.error( "Error en la aplicación: {}", e.getMessage(), e );
            System.exit(1 );
        }
    }
}
