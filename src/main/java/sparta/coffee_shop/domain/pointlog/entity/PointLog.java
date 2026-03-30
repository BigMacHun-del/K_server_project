package sparta.coffee_shop.domain.pointlog.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sparta.coffee_shop.domain.point.entity.Point;
import sparta.coffee_shop.domain.user.entity.User;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "points_log")
public class PointLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "points_id", nullable = false)
    private Point points;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private PointLogType type;

    @Column(nullable = false)
    private int amount;

    @Column(name = "balance_after", nullable = false)
    private long balanceAfter;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public PointLog(User user, Point points, PointLogType type, int amount, long balanceAfter) {
        this.user = user;
        this.points = points;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
    }
}
