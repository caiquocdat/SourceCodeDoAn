package com.example.mangxahoiute.models;

public class ModelQrCode {
    String email,QRCode;

    public ModelQrCode() {
    }

    public ModelQrCode(String email, String QRCode) {
        this.email = email;
        this.QRCode = QRCode;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getQRCode() {
        return QRCode;
    }

    public void setQRCode(String QRCode) {
        this.QRCode = QRCode;
    }
}
