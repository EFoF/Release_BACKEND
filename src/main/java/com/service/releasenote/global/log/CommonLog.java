package com.service.releasenote.global.log;

import java.time.LocalDateTime;

public class CommonLog {

    private String content;
    private String type;
    private LocalDateTime time;

    public CommonLog(String content, String type, LocalDateTime time) {
        this.content = content;
        this.type = type;
        this.time = time;
    }

    public String getContent() {
        return content;
    }

    public java.lang.String getType() {
        return type;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }
}
