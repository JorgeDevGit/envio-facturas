package com.miempresa.facturas;

public class Config {
    private final String baseUrl;
    private final String inputDir;
    private final String processedDir;

    public Config(String baseUrl, String inputDir, String processedDir) {
        this.baseUrl = baseUrl;
        this.inputDir = inputDir;
        this.processedDir = processedDir;
    }

    public String getBaseUrl() { return baseUrl; }
    public String getInputDir() { return inputDir; }
    public String getProcessedDir() { return processedDir; }
}
