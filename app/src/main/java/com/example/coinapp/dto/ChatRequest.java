package com.example.coinapp.dto;



import java.util.ArrayList;
import java.util.List;

public class ChatRequest {

    private String model;
    private List<Message> messages;
    private Double top_p;

    // 기본 생성자
    public ChatRequest() {
        // Lombok 대신 직접 작성
    }

    // 원하는 파라미터만 받는 생성자
    public ChatRequest(String model, String prompt) {
        this.model = model;
        this.messages = new ArrayList<>();
        this.messages.add(new Message("user", prompt));
        this.top_p = 0.3;
    }

    // Getter/Setter
    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public Double getTop_p() {
        return top_p;
    }

    public void setTop_p(Double top_p) {
        this.top_p = top_p;
    }
}
