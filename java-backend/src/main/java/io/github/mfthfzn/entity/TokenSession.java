package io.github.mfthfzn.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Table(name = "token_sessions")
@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
public class TokenSession {

  @Id
  private String email;

  private String token;

  @Column(name = "expired_at")
  private LocalDateTime expiredAt;

  @OneToOne
  @MapsId
  @JoinColumn(name = "email", referencedColumnName = "email")
  private User user;
}
