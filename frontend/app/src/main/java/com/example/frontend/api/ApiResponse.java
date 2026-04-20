package com.example.frontend.api;

import com.google.gson.annotations.SerializedName;

/**
 * Modelo padrão de resposta da API
 * Toda resposta do backend segue este padrão
 * Tanto sucesso quanto erro
 */
public class ApiResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("data")
    private Object data;

    @SerializedName("message")
    private String message;

    @SerializedName("error")
    private String error;

    @SerializedName("details")
    private Object details;

    @SerializedName("timestamp")
    private String timestamp;

    @SerializedName("status")
    private String status;

    @SerializedName("user_id")
    private String userId;

    // Construtores
    public ApiResponse() {}

    public ApiResponse(boolean success, Object data, String message) {
        this.success = success;
        this.data = data;
        this.message = message;
    }

    // Getters e Setters
    public boolean isSuccess() {
        // O backend agora devolve "status": "success" ou success booleano
        if (status != null && status.equals("success")) return true;
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getUserId() {
        return userId;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Object getDetails() {
        return details;
    }

    public void setDetails(Object details) {
        this.details = details;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "ApiResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", error='" + error + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
