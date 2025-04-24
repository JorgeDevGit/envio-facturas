package com.miempresa.facturas;

public class ResponseDto {
    private String estado;
    private String uuid;
    private String url;
    private String qr;

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getQr() { return qr; }
    public void setQr(String qr) { this.qr = qr; }
}
