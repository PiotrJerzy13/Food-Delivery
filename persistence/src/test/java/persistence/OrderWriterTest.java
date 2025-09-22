// OrderWriterTest.java
package food.data;

import food.domain.Food;
import food.domain.Order;
import food.domain.OrderItem;
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

class OrderWriterTest {

    private OrderWriter orderWriter;
    private Order testOrder;
    private Food testFood1;
    private Food testFood2;

    @BeforeEach
    void setUp() {
        orderWriter = new OrderWriter();

        testFood1 = new Food.Builder()
                .name("Pizza")
                .calorie(new BigDecimal("300"))
                .description("Delicious pizza")
                .price(new BigDecimal("10.99"))
                .build();

        testFood2 = new Food.Builder()
                .name("Burger")
                .calorie(new BigDecimal("450"))
                .description("Tasty burger")
                .price(new BigDecimal("8.50"))
                .build();

        // Assuming OrderItem has constructors or builders - adjust as needed
        OrderItem orderItem1 = new OrderItem(testFood1, 2, new BigDecimal("21.98"));
        OrderItem orderItem2 = new OrderItem(testFood2, 1, new BigDecimal("8.50"));

        testOrder = new Order(
                1L,
                123L,
                List.of(orderItem1, orderItem2),
                new BigDecimal("30.48"),
                LocalDateTime.of(2025, 1, 15, 14, 30)
        );
    }

    @Test
    void shouldWriteOrdersToFile(@TempDir Path tempDir) throws IOException {
        // Given
        Path outputFile = tempDir.resolve("orders.csv");
        List<Order> orders = List.of(testOrder);

        // When
        orderWriter.writeOrders(orders, outputFile.toString());

        // Then
        assertThat(outputFile).exists();
        String content = Files.readString(outputFile);

        String[] lines = content.split("\n");
        assertThat(lines).hasSize(2);

        // First order item line
        assertThat(lines[0]).isEqualTo("1,123,Pizza,2,21.98,15/01/2025 14:30,30.48");
        // Second order item line
        assertThat(lines[1]).isEqualTo("1,123,Burger,1,8.50,15/01/2025 14:30,30.48");
    }

    @Test
    void shouldWriteMultipleOrders(@TempDir Path tempDir) throws IOException {
        // Given
        Path outputFile = tempDir.resolve("multiple_orders.csv");

        OrderItem singleItem = new OrderItem(testFood1, 1, new BigDecimal("10.99"));

        Order secondOrder = new Order(
                2L,
                456L,
                List.of(singleItem),
                new BigDecimal("10.99"),
                LocalDateTime.of(2025, 1, 16, 12, 15)
        );

        List<Order> orders = List.of(testOrder, secondOrder);

        // When
        orderWriter.writeOrders(orders, outputFile.toString());

        // Then
        String content = Files.readString(outputFile);
        String[] lines = content.split("\n");
        assertThat(lines).hasSize(3);

        // Check that both orders are written correctly
        assertThat(lines[0]).contains("1,123,Pizza");
        assertThat(lines[1]).contains("1,123,Burger");
        assertThat(lines[2]).contains("2,456,Pizza");
    }

    @Test
    void shouldAppendOrderToExistingFile(@TempDir Path tempDir) throws IOException {
        // Given
        Path outputFile = tempDir.resolve("existing_orders.csv");
        Files.writeString(outputFile, "0,999,Existing Item,1,5.00,01/01/2025 10:00,5.00");

        // When
        orderWriter.appendOrder(testOrder, outputFile.toString());

        // Then
        String content = Files.readString(outputFile);
        String[] lines = content.split("\n");
        assertThat(lines).hasSize(3);

        // Original line should remain
        assertThat(lines[0]).isEqualTo("0,999,Existing Item,1,5.00,01/01/2025 10:00,5.00");
        // New order items should be appended
        assertThat(lines[1]).contains("1,123,Pizza");
        assertThat(lines[2]).contains("1,123,Burger");
    }

    @Test
    void shouldAppendOrderToNewFile(@TempDir Path tempDir) throws IOException {
        // Given
        Path outputFile = tempDir.resolve("new_orders.csv");

        // When
        orderWriter.appendOrder(testOrder, outputFile.toString());

        // Then
        assertThat(outputFile).exists();
        String content = Files.readString(outputFile);
        String[] lines = content.split("\n");
        assertThat(lines).hasSize(2);

        assertThat(lines[0]).contains("1,123,Pizza");
        assertThat(lines[1]).contains("1,123,Burger");
    }

    @Test
    void shouldHandleEmptyOrderItems(@TempDir Path tempDir) throws IOException {
        // Given
        Path outputFile = tempDir.resolve("empty_order.csv");
        Order emptyOrder = new Order(
                1L,
                123L,
                List.of(),
                BigDecimal.ZERO,
                LocalDateTime.now()
        );

        // When
        orderWriter.writeOrders(List.of(emptyOrder), outputFile.toString());

        // Then
        String content = Files.readString(outputFile);
        assertThat(content).isEmpty();
    }
}