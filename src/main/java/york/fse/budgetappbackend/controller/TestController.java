package york.fse.budgetappbackend.controller;

import org.springframework.web.bind.annotation.*;
import york.fse.budgetappbackend.model.TestEntity;
import york.fse.budgetappbackend.repository.TestRepository;

@RestController
@RequestMapping("/api/test")
public class TestController {

    private final TestRepository testRepository;

    public TestController(TestRepository testRepository) {
        this.testRepository = testRepository;
    }

    @PostMapping
    public TestEntity createTest(@RequestBody TestEntity testEntity) {
        return testRepository.save(testEntity);
    }

    @GetMapping
    public Iterable<TestEntity> getAllTests() {
        return testRepository.findAll();
    }
}
