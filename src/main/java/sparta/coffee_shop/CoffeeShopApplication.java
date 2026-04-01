package sparta.coffee_shop;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import sparta.coffee_shop.domain.menu.entity.Menu;
import sparta.coffee_shop.domain.menu.repository.MenuRepository;
import sparta.coffee_shop.domain.point.entity.Point;
import sparta.coffee_shop.domain.point.repository.PointRepository;
import sparta.coffee_shop.domain.user.entity.User;
import sparta.coffee_shop.domain.user.repository.UserRepository;

@EnableAsync
@EnableJpaAuditing
@SpringBootApplication
public class CoffeeShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoffeeShopApplication.class, args);
    }


    @Bean
    CommandLineRunner initData(
            UserRepository userRepository,
            PointRepository pointRepository,
            MenuRepository menuRepository
    ) {
        return args -> {
            // 유저 + 포인트 초기화 (중복 방지)
            if (userRepository.findByUserKey("aaaa-aaaa").isEmpty()) {
                User user = userRepository.save(new User("aaaa-aaaa", "테스트유저"));
                pointRepository.save(new Point(user));
            }

            // 메뉴 초기화 (중복 방지)
            if (menuRepository.count() == 0) {
                menuRepository.save(new Menu("아메리카노", 3000));
                menuRepository.save(new Menu("카페라떼", 4000));
                menuRepository.save(new Menu("바닐라라떼", 4500));
                menuRepository.save(new Menu("카푸치노", 4000));
                menuRepository.save(new Menu("에스프레소", 2500));
            }
        };
    }
}
