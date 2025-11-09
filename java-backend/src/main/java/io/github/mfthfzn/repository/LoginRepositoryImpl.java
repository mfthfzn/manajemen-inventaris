package io.github.mfthfzn.repository;

import io.github.mfthfzn.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;

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
}
