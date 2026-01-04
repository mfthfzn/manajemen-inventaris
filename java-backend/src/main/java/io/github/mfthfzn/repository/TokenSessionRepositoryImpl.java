package io.github.mfthfzn.repository;

import io.github.mfthfzn.entity.TokenSession;
import io.github.mfthfzn.entity.User;
import jakarta.persistence.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
public class TokenSessionRepositoryImpl implements TokenSessionRepository {

  private EntityManagerFactory entityManagerFactory;

  public TokenSessionRepositoryImpl(EntityManagerFactory entityManagerFactory) {
    this.entityManagerFactory = entityManagerFactory;
  }

  @Override
  public void saveTokenSession(TokenSession tokenSession) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    EntityTransaction transaction = entityManager.getTransaction();
    try (entityManager) {
      transaction.begin();

      User userReference = entityManager.getReference(User.class, tokenSession.getUser().getEmail());
      tokenSession.setUser(userReference);

      entityManager.persist(tokenSession);

      transaction.commit();

    } catch (Exception exception) {
      if (transaction.isActive()) transaction.rollback();
      log.error(exception.getMessage());
      throw new PersistenceException(exception);
    }
  }

  @Override
  public Optional<TokenSession> findTokenSessionByEmail(String email) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    EntityTransaction transaction = entityManager.getTransaction();
    try (entityManager) {

      transaction.begin();

      Optional<TokenSession> tokenSession = Optional.ofNullable(entityManager.find(TokenSession.class, email));

      transaction.commit();

      return tokenSession;
    } catch (Exception exception) {
      if (transaction.isActive()) transaction.rollback();
      log.error(exception.getMessage());
      throw new PersistenceException(exception);
    }
  }

  @Override
  public void removeTokenSession(TokenSession tokenSession) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    EntityTransaction transaction = entityManager.getTransaction();
    try(entityManager) {

      transaction.begin();
      entityManager.createQuery("DELETE FROM TokenSession t WHERE t.email = :email")
              .setParameter("email", tokenSession.getEmail())
              .executeUpdate();
      transaction.commit();

    } catch (Exception exception) {
      if (transaction.isActive()) transaction.rollback();
      log.error(exception.getMessage());
      throw new PersistenceException(exception);
    }
  }
}
