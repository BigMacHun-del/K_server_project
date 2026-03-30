package sparta.coffee_shop.domain.point.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sparta.coffee_shop.common.BaseEntity;
import sparta.coffee_shop.domain.user.entity.User;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "points")
public class Point extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private long balance;

    @Version
    private int version;

    public Point(User user) {
        this.user = user;
        this.balance = 0L;
    }

    public void charge(long amount) {
        this.balance += amount;
    }

    public void deduct(long amount) {
        if (this.balance < amount) {
            throw new IllegalArgumentException("포인트 잔액이 부족합니다.");
        }
        this.balance -= amount;
    }
}
