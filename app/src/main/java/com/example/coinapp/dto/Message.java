package com.example.coinapp.dto;



public class Message {
    private String role;
    private String content; // prompt

    // 기본 생성자
    public Message() {
    }

    // 전체 필드 생성자
    public Message(String role, String content) {
        this.role = role;
        this.content = content;
    }

    // Getter/Setter
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
