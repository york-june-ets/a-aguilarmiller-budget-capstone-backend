package york.fse.budgetappbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BudgetAppBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BudgetAppBackendApplication.class, args);
    }

}
