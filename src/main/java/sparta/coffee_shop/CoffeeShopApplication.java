package sparta.coffee_shop;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import sparta.coffee_shop.domain.point.entity.Point;
import sparta.coffee_shop.domain.point.repository.PointRepository;
import sparta.coffee_shop.domain.user.entity.User;
import sparta.coffee_shop.domain.user.repository.UserRepository;

@SpringBootApplication
public class CoffeeShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoffeeShopApplication.class, args);
    }


    //임시 데이터 삽입
    @Bean
    CommandLineRunner init(UserRepository userRepository, PointRepository pointRepository) {
        return args -> {
            if (userRepository.findByUserKey("aaaa-aaaa").isEmpty()) {
                User user = new User("aaaa-aaaa", "테스트유저");
                userRepository.save(user);
                pointRepository.save(new Point(user));
            }
        };
    }
}
