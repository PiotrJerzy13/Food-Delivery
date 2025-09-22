// FoodReaderTest.java
package food.data;

import food.domain.Food;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class FoodReaderTest {

    private FoodReader foodReader;

    @BeforeEach
    void setUp() {
        foodReader = new FoodReader();
    }

    @Test
    void shouldReadFoodsFromFile(@TempDir Path tempDir) throws IOException {
        // Given
        Path foodFile = tempDir.resolve("foods.csv");
        String content = """
                Pizza Margherita,300,Classic pizza with tomato and mozzarella,12.99
                Hamburger,450,Beef patty with lettuce and tomato,8.50
                Caesar Salad,200,Fresh lettuce with caesar dressing,7.25
                """;
        Files.writeString(foodFile, content);

        // When
        List<Food> foods = foodReader.read(foodFile.toString());

        // Then
        assertThat(foods).hasSize(3);

        Food pizza = foods.get(0);
        assertThat(pizza.getName()).isEqualTo("Pizza Margherita");
        assertThat(pizza.getCalorie()).isEqualByComparingTo(new BigDecimal("300"));
        assertThat(pizza.getDescription()).isEqualTo("Classic pizza with tomato and mozzarella");
        assertThat(pizza.getPrice()).isEqualByComparingTo(new BigDecimal("12.99"));

        Food hamburger = foods.get(1);
        assertThat(hamburger.getName()).isEqualTo("Hamburger");
        assertThat(hamburger.getCalorie()).isEqualByComparingTo(new BigDecimal("450"));
        assertThat(hamburger.getPrice()).isEqualByComparingTo(new BigDecimal("8.50"));

        Food salad = foods.get(2);
        assertThat(salad.getName()).isEqualTo("Caesar Salad");
        assertThat(salad.getCalorie()).isEqualByComparingTo(new BigDecimal("200"));
        assertThat(salad.getPrice()).isEqualByComparingTo(new BigDecimal("7.25"));
    }

    @Test
    void shouldReturnEmptyListForEmptyFile(@TempDir Path tempDir) throws IOException {
        // Given
        Path emptyFile = tempDir.resolve("empty.csv");
        Files.writeString(emptyFile, "");

        // When
        List<Food> foods = foodReader.read(emptyFile.toString());

        // Then
        assertThat(foods).isEmpty();
    }

    @Test
    void shouldHandleFoodWithCommasInDescription(@TempDir Path tempDir) throws IOException {
        // Given
        Path foodFile = tempDir.resolve("foods.csv");
        Files.writeString(foodFile, "Special Dish,500,\"Dish with cheese, tomato, and herbs\",15.99");

        // When
        List<Food> foods = foodReader.read(foodFile.toString());

        // Then
        assertThat(foods).hasSize(1);
        Food food = foods.get(0);
        assertThat(food.getName()).isEqualTo("Special Dish");
        assertThat(food.getDescription()).isEqualTo("\"Dish with cheese, tomato, and herbs\"");
    }
}