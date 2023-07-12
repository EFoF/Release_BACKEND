package com.service.releasenote.domain.member.model;

public enum MemberLoginType {
    RELEASE_LOGIN(1, "LOCAL"),
    GOOGLE_LOGIN(2, "GOOGLE");

    private long id;
    private String type;

    MemberLoginType(long id, String type) {
        this.id = id;
        this.type = type;
    }

}
