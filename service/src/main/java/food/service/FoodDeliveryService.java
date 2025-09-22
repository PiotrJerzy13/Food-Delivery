package food.service;

import java.util.List;

import food.domain.Customer;
import food.domain.Food;
import food.domain.Order;
import food.domain.Credentials;

public interface FoodDeliveryService {
    Customer authenticate(Credentials credentials) throws AuthenticationException;

    List<Food> listAllFood();

    void updateCart(Customer customer, Food food, int pieces) throws LowBalanceException;

    Order createOrder(Customer customer) throws IllegalStateException;

}
