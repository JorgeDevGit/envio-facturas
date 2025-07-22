# Facturas Processor

Proyecto Java 17 para procesar facturas JSON y enviarlas al servicio Verifactu.

## Estructura del proyecto

- `VerifactuClient`: encapsula llamadas REST.
- `InvoiceProcessor`: maneja lectura JSON, llamada al servicio y movimiento de archivos.
- `MainApp`: punto de entrada con lógica mínima.

## Requisitos

- Java 17
- Maven 3.6+
- Variable de entorno `VERIFACTU_TOKEN` con el bearer token.

## Configuración

1. URL base del servicio (p.ej. `https://prewww2.aeat.es/wlpl/TIKE-CONT`)

## Compilación

```bash
mvn clean package
```

## Uso

```bash
facturas-processor-1.0.0.exe <rutaBase> <serviceUrl> <jsonFile>
facturas-processor-1.0.0.exe "C:\Proyectos\facturas" https://api.verifacti.com/verifactu/create A000106.json
facturas-processor-1.0.0.exe "C:\Proyectos\facturas" https://api.verifacti.com/verifactu/status A000340.json

```
