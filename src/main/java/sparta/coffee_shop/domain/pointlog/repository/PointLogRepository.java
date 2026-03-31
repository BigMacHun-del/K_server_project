package sparta.coffee_shop.domain.pointlog.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sparta.coffee_shop.domain.pointlog.entity.PointLog;

public interface PointLogRepository extends JpaRepository<PointLog,Long> {
}
