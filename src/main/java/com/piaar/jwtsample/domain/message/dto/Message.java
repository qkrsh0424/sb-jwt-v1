package com.piaar.jwtsample.domain.message.dto;

import java.time.LocalDateTime;
import java.util.Date;

import lombok.Getter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

import lombok.Data;

@Getter
@ToString
public class Message {
    private HttpStatus status;
    private String statusMessage;
    private Integer statusCode;
    private String message;
    private String memo;
    private Object data;
    private LocalDateTime timestamp;

    public Message() {
        this.status = HttpStatus.BAD_REQUEST;
        this.statusMessage = HttpStatus.BAD_REQUEST.name();
        this.statusCode = HttpStatus.BAD_REQUEST.value();
        this.message = null;
        this.memo = null;
        this.data = null;
        this.timestamp = LocalDateTime.now();
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
        this.statusMessage = status.name();
        this.statusCode = status.value();
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
