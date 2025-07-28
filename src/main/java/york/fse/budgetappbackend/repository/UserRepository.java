package york.fse.budgetappbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import york.fse.budgetappbackend.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}