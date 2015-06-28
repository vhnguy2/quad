package com.quad.model;

public class TypingIndicator {
  private String userId;

  private TypingIndicator() {
  }

  public TypingIndicator(String userId) {
    this.userId = userId;
  }

  public String getUserId() {
    return userId;
  }
}
