package food.service;

import food.data.FileDataStore;
import food.domain.Cart;
import food.domain.Credentials;
import food.domain.Customer;
import food.domain.Food;
import food.domain.Order;
import food.domain.OrderItem;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public class DefaultFoodDeliveryService implements FoodDeliveryService {

    private final FileDataStore dataStore;

    public DefaultFoodDeliveryService(FileDataStore fileDataStore) {
        this.dataStore = Objects.requireNonNull(fileDataStore);
    }

    @Override
    public Customer authenticate(Credentials credentials) throws AuthenticationException {
        if (credentials == null
                || credentials.getUserName() == null
                || credentials.getPassword() == null) {
            throw new AuthenticationException("Invalid credentials.");
        }
        return dataStore.getCustomers().stream()
                .filter(c -> credentials.getUserName().equals(c.getUserName())
                          && credentials.getPassword().equals(c.getPassword()))
                .findFirst()
                .orElseThrow(() -> new AuthenticationException("Authentication failed."));
    }

    @Override
    public List<Food> listAllFood() {
        return dataStore.getFoods();
    }

    @Override
    public void updateCart(Customer customer, Food food, int pieces) throws LowBalanceException {
        if (customer == null || food == null) {
            throw new IllegalArgumentException("Customer and food must be provided.");
        }
        if (pieces < 0) {
            throw new IllegalArgumentException("Pieces must be >= 0.");
        }

        Cart cart = customer.getCart();
        if (cart == null) {
            cart = Cart.getEmptyCart();
            customer.setCart(cart);
        }

        OrderItem existing = null;
        for (OrderItem oi : cart.getOrderItems()) {
            if (oi.getFood().getName().equals(food.getName())) {
                existing = oi;
                break;
            }
        }

        if (pieces == 0) {
            if (existing == null) {
                throw new IllegalArgumentException("Cannot remove non-existing item from cart.");
            }
            cart.getOrderItems().remove(existing);
            cart.setPrice(cart.getPrice().subtract(existing.getPrice()));
            return;
        }

        BigDecimal unit = nonNull(food.getPrice());
        BigDecimal newItemPrice = unit.multiply(BigDecimal.valueOf(pieces));

        BigDecimal cartTotal = nonNull(cart.getPrice());
        BigDecimal oldItemPrice = existing != null ? nonNull(existing.getPrice()) : BigDecimal.ZERO;
        BigDecimal prospectiveTotal = cartTotal.subtract(oldItemPrice).add(newItemPrice);

        if (prospectiveTotal.compareTo(nonNull(customer.getBalance())) > 0) {
            throw new LowBalanceException(
                    "With current cart content, adding " + pieces + " x " + food.getName()
                    + " would exceed available balance.");
        }

        OrderItem updated = new OrderItem(food, pieces, newItemPrice);
        if (existing != null) {
            cart.getOrderItems().remove(existing);
        }
        cart.getOrderItems().add(updated);
        cart.setPrice(prospectiveTotal);
    }

    @Override
    public Order createOrder(Customer customer) throws IllegalStateException {
        if (customer == null) {
            throw new IllegalArgumentException("Customer must be provided.");
        }
        Cart cart = customer.getCart();
        if (cart == null || cart.getOrderItems().isEmpty()) {
            throw new IllegalStateException("Cannot create order from empty cart.");
        }
        Order order = new Order(customer);

        Order persisted = dataStore.createOrder(order);
        customer.getOrders().add(persisted);

        BigDecimal newBalance = nonNull(customer.getBalance()).subtract(nonNull(persisted.getPrice()));
        customer.setBalance(newBalance);

        // Empty the cart
        cart.getOrderItems().clear();
        cart.setPrice(BigDecimal.ZERO);

        return persisted;
    }

    private static BigDecimal nonNull(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
