package york.fse.budgetappbackend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CategoryController {

    @GetMapping("/api/categories")
    public List<String> getCategories() {
        return List.of(
                "Groceries",
                "Dining & Drinks",
                "Entertainment",
                "Utilities",
                "Rent/Mortgage",
                "Transportation",
                "Health",
                "Savings",
                "Miscellaneous"
        );
    }
}
