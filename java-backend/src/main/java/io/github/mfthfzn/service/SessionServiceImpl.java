package io.github.mfthfzn.service;

import io.github.mfthfzn.dto.SessionRequest;
import io.github.mfthfzn.dto.SessionResponse;
import io.github.mfthfzn.entity.TokenSession;
import io.github.mfthfzn.entity.User;
import io.github.mfthfzn.repository.TokenSessionRepositoryImpl;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceException;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

public class SessionServiceImpl implements SessionService {

  private final TokenSessionRepositoryImpl tokenSessionRepository;

  public SessionServiceImpl(TokenSessionRepositoryImpl tokenSessionRepository) {
    this.tokenSessionRepository = tokenSessionRepository;
  }

  @Override
  public String generateToken(User user) {
    String token = UUID.randomUUID().toString();
    LocalDateTime expiredAt = LocalDateTime.now().plusDays(1);

    TokenSession tokenSession = new TokenSession();
    tokenSession.setToken(token);
    tokenSession.setExpiredAt(expiredAt);
    tokenSession.setUser(user);

    tokenSessionRepository.saveTokenSession(tokenSession);
    return tokenSession.getToken();
  }

  @Override
  public SessionResponse getSession(SessionRequest sessionRequest) {
    try {
      Optional<TokenSession> session = tokenSessionRepository.findTokenSessionByEmail(sessionRequest.getEmail());
      SessionResponse sessionResponse = new SessionResponse();
      if (session.isPresent()) {
        TokenSession tokenSession = session.get();
        if (sessionRequest.getToken().equals(tokenSession.getToken())) {
          LocalDateTime expiredAt = tokenSession.getExpiredAt();

          sessionResponse.setExpired(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).isAfter(expiredAt));
        } else {
          sessionResponse.setExpired(true);
        }
      } else {
        sessionResponse.setExpired(true);
      }
      return sessionResponse;
    } catch (PersistenceException exception) {
      throw new PersistenceException("Terjadi kesalahan pada server database.");
    }
  }

  @Override
  public SessionResponse removeSession(SessionRequest sessionRequest) {
    try {
      Optional<TokenSession> tokenSession = tokenSessionRepository.findTokenSessionByEmail(sessionRequest.getEmail());
      SessionResponse sessionResponse = new SessionResponse();
      if (tokenSession.isEmpty()) {
        sessionResponse.setRemoved(false);
      } else {
        tokenSessionRepository.removeTokenSession(tokenSession.get());
        sessionResponse.setRemoved(true);
      }

      return sessionResponse;
    } catch (PersistenceException exception) {
      throw new PersistenceException("Terjadi kesalahan pada server database.");
    }
  }

}
