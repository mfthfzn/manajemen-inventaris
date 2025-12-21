package io.github.mfthfzn.repository;

import io.github.mfthfzn.entity.TokenSession;
import io.github.mfthfzn.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;

import java.time.LocalDateTime;

public class LoginRepositoryImpl implements LoginRepository {

  private EntityManagerFactory entityManagerFactory;

  public LoginRepositoryImpl(EntityManagerFactory entityManagerFactory) {
    this.entityManagerFactory = entityManagerFactory;
  }

  @Override
  public User findUserByEmail(String email) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    EntityTransaction transaction = entityManager.getTransaction();
    try {
      transaction.begin();
      User user = entityManager.find(User.class, email);
      transaction.commit();
      return user;
    } catch (NoResultException error) {
      return null;
    } finally {
      entityManager.close();
    }
  }

  @Override
  public boolean setTokenSession(String email, String token) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    EntityTransaction transaction = entityManager.getTransaction();

    try {
      transaction.begin();
      User user = entityManager.find(User.class, email);

      TokenSession tokenSession = new TokenSession();
      tokenSession.setUser(user);
      tokenSession.setToken(token);
      tokenSession.setExpiredAt(LocalDateTime.now().plusDays(1));

      entityManager.persist(tokenSession);

      transaction.commit();

      return true;
    } catch (Exception exception) {
      return false;
    }
  }

  @Override
  public TokenSession findTokenByEmail(String email) {
    return null;
  }
}
