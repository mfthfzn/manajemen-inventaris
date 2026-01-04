package io.github.mfthfzn.repository;

import io.github.mfthfzn.entity.TokenSession;
import io.github.mfthfzn.entity.User;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TokenSessionRepository {

  void saveTokenSession(TokenSession tokenSession);

  Optional<TokenSession> findTokenSessionByEmail(String email);

  void removeTokenSession(TokenSession tokenSession);

}
