package com.miempresa.facturas;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainApp {
    private static final Logger logger = LoggerFactory.getLogger(MainApp.class);

    public static void main(String[] args) {
        if (args.length != 1) {
            logger.error("Uso: java -jar facturas-processor.jar <nombre-json>");
            System.exit(1);
        }
        String jsonFileName = args[0];

        try {
            Config config = ConfigLoader.load("config.txt");
            String token = System.getenv("VERIFACTU_TOKEN");
            if (token == null || token.isBlank()) {
                throw new IllegalStateException("Variable VERIFACTU_TOKEN no definida");
            }

            VerifactuClient client = new VerifactuClient(config.getBaseUrl(), token);
            InvoiceProcessor processor = new InvoiceProcessor(config, client);
            int result = processor.process(jsonFileName);
            System.exit(result);
        } catch (Exception e) {
            logger.error("Error en la aplicación: {}", e.getMessage(), e);
            System.exit(1);
        }
    }
}
