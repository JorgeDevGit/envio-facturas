package com.miempresa.facturas;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ConfigLoader {
    public static Config load(String path) throws Exception {
        List<String> lines = Files.readAllLines(Path.of(path), StandardCharsets.UTF_8);
        if (lines.size() < 3) {
            throw new IllegalArgumentException("config.txt debe tener al menos 3 líneas");
        }
        return new Config(
            lines.get(0).trim(),
            lines.get(1).trim(),
            lines.get(2).trim()
        );
    }
}
