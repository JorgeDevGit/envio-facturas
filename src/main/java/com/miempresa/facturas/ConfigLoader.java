package com.miempresa.facturas;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ConfigLoader {
    public static Config load( ) throws Exception {
        // Lee siempre de ./config/config.txt
        final Path cfg = Path.of("config", "config.txt");
        final List< String > lines = Files.readAllLines( cfg, StandardCharsets.UTF_8 );
        if ( lines.size() < 2 ) {
            throw new IllegalArgumentException( "config.txt debe tener al menos 2 líneas" );
        }
        return new Config(
                lines.get( 0 ).trim(),
                lines.get( 1 ).trim()
        );
    }
}
