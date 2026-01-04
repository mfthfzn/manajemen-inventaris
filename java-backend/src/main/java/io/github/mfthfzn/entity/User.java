package io.github.mfthfzn.entity;

import io.github.mfthfzn.enums.UserType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "users"
)
@Getter
@Setter
@NoArgsConstructor
@ToString
public class User {

  @Id
  private String email;

  private String password;

  @Embedded
  private Name name;

  @Enumerated(EnumType.STRING)
  private UserType role;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @ManyToOne
  @JoinColumn(
          name = "store_id",
          referencedColumnName = "id"
  )
  private Store store;

}
