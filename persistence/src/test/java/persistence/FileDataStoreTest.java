// FileDataStoreTest.java
package food.data;

import food.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class FileDataStoreTest {

    private FileDataStore dataStore;
    private Path tempDir;

    @BeforeEach
    void setUp(@TempDir Path tempDir) throws IOException {
        this.tempDir = tempDir;
        this.dataStore = new FileDataStore(tempDir.toString());

        // Create test data files
        createTestCustomersFile();
        createTestFoodsFile();
    }

    private void createTestCustomersFile() throws IOException {
        Path customersFile = tempDir.resolve("customers.csv");
        String content = """
                john_doe,password123,1,John Doe,100.50
                jane_smith,secret456,2,Jane Smith,250.75
                """;
        Files.writeString(customersFile, content);
    }

    private void createTestFoodsFile() throws IOException {
        Path foodsFile = tempDir.resolve("foods.csv");
        String content = """
                Pizza,300,Delicious pizza,12.99
                Burger,450,Tasty burger,8.50
                """;
        Files.writeString(foodsFile, content);
    }

    @Test
    void shouldInitializeDataStore() {
        // When
        dataStore.init();

        // Then
        List<Customer> customers = dataStore.getCustomers();
        assertThat(customers).hasSize(2);
        assertThat(customers.get(0).getName()).isEqualTo("John Doe");
        assertThat(customers.get(1).getName()).isEqualTo("Jane Smith");

        List<Food> foods = dataStore.getFoods();
        assertThat(foods).hasSize(2);
        assertThat(foods.get(0).getName()).isEqualTo("Pizza");
        assertThat(foods.get(1).getName()).isEqualTo("Burger");

        List<Order> orders = dataStore.getOrders();
        assertThat(orders).isEmpty();
    }

    @Test
    void shouldCreateOrderWithGeneratedId() {
        // Given
        dataStore.init();

        Food pizza = dataStore.getFoods().get(0);
        OrderItem orderItem = new OrderItem(pizza, 2, new BigDecimal("25.98"));

        Order order = new Order(
                null,
                1L,
                List.of(orderItem),
                new BigDecimal("25.98"),
                LocalDateTime.now()
        );

        // When
        Order createdOrder = dataStore.createOrder(order);

        // Then
        assertThat(createdOrder.getOrderId()).isEqualTo(0L);
        assertThat(dataStore.getOrders()).hasSize(1);
        assertThat(dataStore.getOrders().get(0)).isEqualTo(createdOrder);
    }

    @Test
    void shouldAddOrderToCustomer() {
        // Given
        dataStore.init();

        Food pizza = dataStore.getFoods().get(0);
        OrderItem orderItem = new OrderItem(pizza, 1, pizza.getPrice());

        Order order = new Order(
                null,
                1L,
                List.of(orderItem),
                pizza.getPrice(),
                LocalDateTime.now()
        );

        // When
        dataStore.createOrder(order);

        // Then
        Customer customer = dataStore.getCustomers().stream()
                .filter(c -> c.getId() == 1L)
                .findFirst()
                .orElseThrow();

        assertThat(customer.getOrders()).hasSize(1);
        assertThat(customer.getOrders().get(0)).isEqualTo(order);
    }

    @Test
    void shouldIncrementOrderIds() {
        // Given
        dataStore.init();

        Food pizza = dataStore.getFoods().get(0);
        OrderItem orderItem = new OrderItem(pizza, 1, pizza.getPrice());

        Order order1 = new Order(
                null,
                1L,
                List.of(orderItem),
                pizza.getPrice(),
                LocalDateTime.now()
        );

        Order order2 = new Order(
                null,
                2L,
                List.of(orderItem),
                pizza.getPrice(),
                LocalDateTime.now()
        );

        // When
        Order createdOrder1 = dataStore.createOrder(order1);
        Order createdOrder2 = dataStore.createOrder(order2);

        // Then
        assertThat(createdOrder1.getOrderId()).isEqualTo(0L);
        assertThat(createdOrder2.getOrderId()).isEqualTo(1L);
    }

    @Test
    void shouldThrowExceptionForNullOrder() {
        // Given
        dataStore.init();

        // When & Then
        assertThatThrownBy(() -> dataStore.createOrder(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("order is null");
    }

    @Test
    void shouldWriteOrdersToFile() throws IOException {
        // Given
        dataStore.init();

        Food pizza = dataStore.getFoods().get(0);
        OrderItem orderItem = new OrderItem(pizza, 1, pizza.getPrice());

        Order order = new Order(
                null,
                1L,
                List.of(orderItem),
                pizza.getPrice(),
                LocalDateTime.of(2025, 1, 15, 14, 30)
        );

        dataStore.createOrder(order);

        // When
        dataStore.writeOrders();

        // Then
        Path ordersFile = tempDir.resolve("orders.csv");
        assertThat(ordersFile).exists();

        String content = Files.readString(ordersFile);
        assertThat(content).contains("0,1,Pizza,1,12.99,15/01/2025 14:30,12.99");
    }

    @Test
    void shouldHandleTrailingSlashInFolderPath(@TempDir Path tempDir) throws IOException {
        // Given
        String pathWithSlash = tempDir.toString() + "/";
        FileDataStore dataStoreWithSlash = new FileDataStore(pathWithSlash);

        // Create test files
        Files.writeString(tempDir.resolve("customers.csv"), "user,pass,1,Name,100.00");
        Files.writeString(tempDir.resolve("foods.csv"), "Food,300,Description,10.00");

        // When
        dataStoreWithSlash.init();

        // Then
        assertThat(dataStoreWithSlash.getCustomers()).hasSize(1);
        assertThat(dataStoreWithSlash.getFoods()).hasSize(1);
    }

    @Test
    void shouldNotAddOrderToNonExistentCustomer() {
        // Given
        dataStore.init();

        Food pizza = dataStore.getFoods().get(0);
        OrderItem orderItem = new OrderItem(pizza, 1, pizza.getPrice());

        Order order = new Order(
                null,
                999L, // Non-existent customer
                List.of(orderItem),
                pizza.getPrice(),
                LocalDateTime.now()
        );

        // When
        Order createdOrder = dataStore.createOrder(order);

        // Then
        assertThat(createdOrder.getOrderId()).isEqualTo(0L);
        assertThat(dataStore.getOrders()).hasSize(1);

        // Verify no customer has the order
        boolean orderFoundInCustomers = dataStore.getCustomers().stream()
                .anyMatch(customer -> customer.getOrders().contains(order));
        assertThat(orderFoundInCustomers).isFalse();
    }
}