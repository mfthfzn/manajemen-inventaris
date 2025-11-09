package io.github.mfthfzn.dto;

import lombok.Getter;
import lombok.Setter;

public class LoginResponse {

  @Getter
  @Setter
  private String response;

  public LoginResponse() {
  }

  public LoginResponse(String response) {
    this.response = response;
  }
}
