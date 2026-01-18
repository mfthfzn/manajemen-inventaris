package io.github.mfthfzn.repository;

import io.github.mfthfzn.entity.Token;

import java.util.Optional;

public interface TokenRepository {

  void saveToken(Token tokenSession);

  Optional<Token> findRefreshToken(String email);

  void removeToken(String email);

}
