package com.example.model;

import java.io.Serializable;

public class ItemModel implements Serializable {
    private String id;
    private String title;
    private String category;
    private String type; // "LOST" or "FOUND"
    private String date;
    private String location;
    private String description;
    private String observations;
    private String status; // "WAITING_PICKUP", "DELIVERED", "SEARCHING", "RESOLVED"
    private String registeredByEmail;
    private String registeredByName;
    private String holdingLocation;
    private int imageRes;
    private long timestamp;
    private String photoPath;
    private String protocolCode;

    public ItemModel() {
    }

    public ItemModel(String id, String title, String category, String type, String date,
                     String location, String description, String observations, String status,
                     String registeredByEmail, String registeredByName, String holdingLocation,
                     int imageRes, long timestamp) {
        this(id, title, category, type, date, location, description, observations, status,
             registeredByEmail, registeredByName, holdingLocation, imageRes, timestamp, null, null);
    }

    public ItemModel(String id, String title, String category, String type, String date,
                     String location, String description, String observations, String status,
                     String registeredByEmail, String registeredByName, String holdingLocation,
                     int imageRes, long timestamp, String photoPath, String protocolCode) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.type = type;
        this.date = date;
        this.location = location;
        this.description = description;
        this.observations = observations;
        this.status = status;
        this.registeredByEmail = registeredByEmail;
        this.registeredByName = registeredByName;
        this.holdingLocation = holdingLocation;
        this.imageRes = imageRes;
        this.timestamp = timestamp;
        this.photoPath = photoPath;
        this.protocolCode = protocolCode;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getObservations() {
        return observations != null && !observations.isEmpty() ? observations : "Nenhuma observação adicional.";
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRegisteredByEmail() {
        return registeredByEmail;
    }

    public void setRegisteredByEmail(String registeredByEmail) {
        this.registeredByEmail = registeredByEmail;
    }

    public String getRegisteredByName() {
        return registeredByName;
    }

    public void setRegisteredByName(String registeredByName) {
        this.registeredByName = registeredByName;
    }

    public String getHoldingLocation() {
        if ("LOST".equalsIgnoreCase(type)) {
            return holdingLocation != null ? holdingLocation : "";
        }
        return holdingLocation != null && !holdingLocation.isEmpty() ? holdingLocation : "Secretaria";
    }

    public void setHoldingLocation(String holdingLocation) {
        this.holdingLocation = holdingLocation;
    }

    public int getImageRes() {
        return imageRes;
    }

    public void setImageRes(int imageRes) {
        this.imageRes = imageRes;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public String getProtocolCode() {
        return protocolCode;
    }

    public void setProtocolCode(String protocolCode) {
        this.protocolCode = protocolCode;
    }

    public String getDisplayProtocolCode() {
        if (protocolCode != null && !protocolCode.trim().isEmpty()) {
            return protocolCode;
        }
        if (id != null && !id.trim().isEmpty()) {
            String cleanId = id.toUpperCase().trim();
            if (cleanId.startsWith("FMP-")) {
                return cleanId;
            }
            return "FMP-" + cleanId;
        }
        return "FMP-" + System.currentTimeMillis();
    }
}
