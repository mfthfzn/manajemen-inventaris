package io.github.mfthfzn.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.github.mfthfzn.dto.JwtPayload;
import io.github.mfthfzn.dto.LoginResponse;
import io.github.mfthfzn.entity.Token;
import io.github.mfthfzn.entity.User;
import io.github.mfthfzn.exception.AccessTokenExpiredException;
import io.github.mfthfzn.exception.RefreshTokenExpiredException;
import io.github.mfthfzn.repository.TokenRepositoryImpl;
import jakarta.persistence.PersistenceException;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

public class TokenServiceImpl implements TokenService {

  private final TokenRepositoryImpl tokenRepository;

  private final String SECRET_KEY = "apple river mountain trust candle yellow piano fossil dream coffee stone ladder";

  Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);

  public TokenServiceImpl(TokenRepositoryImpl tokenRepository) {
    this.tokenRepository = tokenRepository;
  }

  @Override
  public String generateAccessToken(LoginResponse loginResponse) {
    User user = loginResponse.getUser();
    return JWT.create()
            .withSubject(user.getEmail())
            .withClaim("role", user.getRole().toString())
            .withClaim("name", user.getName())
            .withClaim("store_id", user.getStore().getId())
            .withClaim("store_name", user.getStore().getName())
            .withIssuedAt(Instant.now())
            .withExpiresAt(Instant.now().plus(Duration.ofMinutes(1)))
            .sign(algorithm);
  }

  @Override
  public String generateAccessToken(JwtPayload jwtPayload) {
    return JWT.create()
            .withSubject(jwtPayload.getEmail())
            .withClaim("role", jwtPayload.getRole())
            .withClaim("name", jwtPayload.getName())
            .withClaim("store_id", jwtPayload.getStoreId())
            .withClaim("store_name", jwtPayload.getStoreName())
            .withIssuedAt(Instant.now())
            .withExpiresAt(Instant.now().plus(Duration.ofMinutes(5)))
            .sign(algorithm);
  }

  @Override
  public String generateRefreshToken(LoginResponse loginResponse) {
    User user = loginResponse.getUser();
    return JWT.create()
            .withSubject(user.getEmail())
            .withClaim("role", user.getRole().toString())
            .withClaim("name", user.getName())
            .withClaim("store_id", user.getStore().getId())
            .withClaim("store_name", user.getStore().getName())
            .withIssuedAt(Instant.now())
            .withExpiresAt(Instant.now().plus(Duration.ofMinutes(5)))
            .sign(algorithm);
  }

  @Override
  public void saveRefreshToken(LoginResponse loginResponse) {
    Token token = new Token();
    token.setUser(loginResponse.getUser());
    token.setToken(loginResponse.getRefreshToken());
    tokenRepository.saveToken(token);
  }

  @Override
  public void verifyRefreshToken(String token) {
    try {
      JWT.require(algorithm)
              .build()
              .verify(token);
    } catch (TokenExpiredException tokenExpiredException) {
      throw new RefreshTokenExpiredException("Refresh token expired");
    } catch (JWTVerificationException jwtVerificationException) {
      throw  new JWTVerificationException("Refresh token invalid");
    }
  }

  @Override
  public void verifyAccessToken(String token) {
    try {
      JWT.require(algorithm)
              .build()
              .verify(token);
    } catch (AccessTokenExpiredException accessTokenExpiredException) {
      throw new AccessTokenExpiredException("Access token expired");
    } catch (JWTVerificationException jwtVerificationException) {
      throw  new JWTVerificationException("Access Token invalid.");
    }
  }

  @Override
  public JwtPayload getUserFromToken(String token) {

    DecodedJWT decodedJWT = JWT.decode(token);

    JwtPayload jwtPayload = new JwtPayload();

    jwtPayload.setStoreId(decodedJWT.getClaim("store_id").asInt());
    jwtPayload.setStoreName(decodedJWT.getClaim("store_name").asString());
    jwtPayload.setEmail(decodedJWT.getSubject());
    jwtPayload.setRole(decodedJWT.getClaim("role").asString());
    jwtPayload.setName(decodedJWT.getClaim("name").asString());
    return jwtPayload;
  }

  @Override
  public String getRefreshToken(String email) {
    try {
      Optional<Token> refreshToken = tokenRepository.findRefreshToken(email);
      return refreshToken.map(Token::getToken).orElse(null);
    } catch (PersistenceException persistenceException) {
      throw new PersistenceException(persistenceException);
    }
  }

  @Override
  public void removeRefreshToken(String email) {
    try {
      tokenRepository.removeToken(email);
    } catch (PersistenceException persistenceException) {
      throw new PersistenceException(persistenceException);
    }
  }

}
