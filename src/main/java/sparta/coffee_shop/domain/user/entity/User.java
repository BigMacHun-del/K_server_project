package sparta.coffee_shop.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sparta.coffee_shop.common.entity.BaseEntity;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_users_user_key", columnList = "user_key")
        }
)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_key", nullable = false, unique = true, length = 100)
    private String userKey;

    @Column(nullable = false, length = 50)
    private String nickname;

    public User(String userKey, String nickname) {
        this.userKey = userKey;
        this.nickname = nickname;
    }
}