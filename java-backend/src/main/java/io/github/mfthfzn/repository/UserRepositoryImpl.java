package io.github.mfthfzn.repository;

import io.github.mfthfzn.entity.TokenSession;
import io.github.mfthfzn.entity.User;
import jakarta.persistence.*;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
public class UserRepositoryImpl implements UserRepository {

  private EntityManagerFactory entityManagerFactory;

  public UserRepositoryImpl(EntityManagerFactory entityManagerFactory) {
    this.entityManagerFactory = entityManagerFactory;
  }

  @Override
  public Optional<User> findUserByEmail(String email) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    EntityTransaction transaction = entityManager.getTransaction();
    try(entityManager) {
      transaction.begin();
      User user = entityManager.find(User.class, email);
      transaction.commit();
      return Optional.ofNullable(user);
    } catch (Exception exception) {
      if (transaction.isActive()) transaction.rollback();
      log.error(exception.getMessage());
      throw new PersistenceException(exception);
    }
  }

}
