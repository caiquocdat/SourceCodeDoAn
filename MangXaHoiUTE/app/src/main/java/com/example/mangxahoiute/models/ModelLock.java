package com.example.mangxahoiute.models;

public class ModelLock {
    String reasonLock;
    String timestamp;
    String status;

    public ModelLock() {
    }

    public ModelLock(String reasonLock, String timestamp, String status) {
        this.reasonLock = reasonLock;
        this.timestamp = timestamp;
        this.status = status;
    }

    public String getReasonLock() {
        return reasonLock;
    }

    public void setReasonLock(String reasonLock) {
        this.reasonLock = reasonLock;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
