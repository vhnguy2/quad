package com.quad.model;

public class ConversationMessage {
  private String message;
  private String userId;
  private String imageUrl;

  // Required default constructor for Firebase object mapping
  private ConversationMessage() {
  }

  public ConversationMessage(String userId, String imageUrl, String message) {
    this.message = message;
    this.userId = userId;
    this.imageUrl = imageUrl;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public String getMessage() {
    return message;
  }

  public String getUserId() {
    return userId;
  }
}
