package com.quad.model;

public class ConversationMessage {
  private String message;
  private String userId;

  // Required default constructor for Firebase object mapping
  private ConversationMessage() {
  }

  public ConversationMessage(String userId, String message) {
    this.message = message;
    this.userId = userId;
  }

  public String getMessage() {
    return message;
  }

  public String getUserId() {
    return userId;
  }
}
