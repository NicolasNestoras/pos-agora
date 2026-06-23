package com.nikos.retail.common.dto;

import java.time.OffsetDateTime;

public class ErrorResponse{
    private OffsetDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;

    public ErrorResponse(){}
    public ErrorResponse(int status, String error, String message, String path){

        timestamp = OffsetDateTime.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;

    }


    public OffsetDateTime getTimestamp(){
        return timestamp;
    }

    public int getStatus(){
        return status;
    }

    public String getMessage(){
        return message;
    }

    public String getError(){
        return error;
    }

    public String getPath(){
        return path;
    }
}
