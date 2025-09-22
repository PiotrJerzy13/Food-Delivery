package food.application.view;

import java.math.BigDecimal;
import java.util.List;

import food.domain.Credentials;
import food.domain.Customer;
import food.domain.Food;
import food.domain.Order;
import food.values.FoodSelection;

public interface View {
    Credentials readCredentials();

    void printWelcomeMessage(Customer customer);

    void printAllFoods(List<Food> foods);

    FoodSelection readFoodSelection(List<Food> foods);

    void printAddedToCart(Food food, int pieces);

    void printErrorMessage(String message);

    void printOrderCreatedStatement(Order order, BigDecimal balance);
}
