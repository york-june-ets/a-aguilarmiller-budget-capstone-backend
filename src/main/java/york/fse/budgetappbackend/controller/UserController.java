package york.fse.budgetappbackend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import york.fse.budgetappbackend.util.PasswordUtil;


import york.fse.budgetappbackend.dto.LoginRequestDTO;
import york.fse.budgetappbackend.model.User;
import york.fse.budgetappbackend.repository.UserRepository;
import york.fse.budgetappbackend.dto.UserUpdateDTO;

import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody LoginRequestDTO request) {
        Optional<User> optionalUser = userRepository.findByEmail(request.getEmail());

        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = optionalUser.get();
        String hashedInput = PasswordUtil.hashPassword(request.getPassword());

        if (!user.getPassword().equals(hashedInput)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(user);
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        if (user.getEmail() == null || user.getPassword() == null) {
            return ResponseEntity.badRequest().build();
        }

        // Hash password
        user.setPassword(PasswordUtil.hashPassword(user.getPassword()));

        User saved = userRepository.save(user);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateUser(@PathVariable Long id, @RequestBody UserUpdateDTO dto) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = optionalUser.get();

        if (dto.getEmail() != null) user.setEmail(dto.getEmail());
        if (dto.getPassword() != null) {
            String hashed = PasswordUtil.hashPassword(dto.getPassword());
            user.setPassword(hashed);
        }
        if (dto.getFirstName() != null) user.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null) user.setLastName(dto.getLastName());
        if (dto.getPhone() != null) user.setPhone(dto.getPhone());

        userRepository.save(user);
        return ResponseEntity.ok("User updated");
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        Optional<User> optionalUser = userRepository.findById(id);

        if (optionalUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(optionalUser.get());
    }

}
