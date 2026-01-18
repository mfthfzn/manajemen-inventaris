package io.github.mfthfzn.service;

import io.github.mfthfzn.dto.JwtPayload;
import io.github.mfthfzn.dto.LoginResponse;

public interface TokenService {

  String generateAccessToken(LoginResponse loginResponse);

  String generateAccessToken(JwtPayload jwtPayload);

  String generateRefreshToken(LoginResponse loginResponse);

  void saveRefreshToken(LoginResponse loginResponse);

  void verifyRefreshToken(String token);

  void verifyAccessToken(String token);

  JwtPayload getUserFromToken(String token);

  String getRefreshToken(String token);

  void removeRefreshToken(String email);

}
