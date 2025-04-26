# Facturas Processor

Proyecto Java 17 para procesar facturas JSON y enviarlas al servicio Verifactu.

## Estructura del proyecto

- `ConfigLoader`: carga configuración desde `config/config.txt`.
- `Config`: representa la configuración leída.
- `VerifactuClient`: encapsula llamadas REST.
- `InvoiceProcessor`: maneja lectura JSON, llamada al servicio y movimiento de archivos.
- `ResponseDto`: mapea la respuesta JSON.
- `MainApp`: punto de entrada con lógica mínima.

## Requisitos

- Java 17
- Maven 3.6+
- Variable de entorno `VERIFACTU_TOKEN` con el bearer token.

## Configuración

Crear un archivo `config/config.txt` en el directorio de ejecución con 3 líneas:

1. URL base del servicio (p.ej. `https://prewww2.aeat.es/wlpl/TIKE-CONT`)
2. Carpeta de entrada (donde se ubican los JSON)
3. Carpeta de procesados (donde mover los JSON tras procesarlos)

Ejemplo `config.txt`:

```
https://prewww2.aeat.es/wlpl/TIKE-CONT
/data/input
/data/processed
```

## Compilación

```bash
mvn clean package
```

## Uso

```bash
export VERIFACTU_TOKEN=tu_token
java -jar target/facturas-processor-1.0.0.jar fact1.json
```
