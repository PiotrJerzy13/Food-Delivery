
// CustomerReaderTest.java
package food.data;

import food.domain.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class CustomerReaderTest {

    private CustomerReader customerReader;

    @BeforeEach
    void setUp() {
        customerReader = new CustomerReader();
    }

    @Test
    void shouldReadCustomersFromFile(@TempDir Path tempDir) throws IOException {
        // Given
        Path customerFile = tempDir.resolve("customers.csv");
        String content = """
                john_doe,password123,1,John Doe,100.50
                jane_smith,secret456,2,Jane Smith,250.75
                bob_wilson,pass789,3,Bob Wilson,0.00
                """;
        Files.writeString(customerFile, content);

        // When
        List<Customer> customers = customerReader.read(customerFile.toString());

        // Then
        assertThat(customers).hasSize(3);

        Customer firstCustomer = customers.get(0);
        assertThat(firstCustomer.getUserName()).isEqualTo("john_doe");
        assertThat(firstCustomer.getPassword()).isEqualTo("password123");
        assertThat(firstCustomer.getId()).isEqualTo(1L);
        assertThat(firstCustomer.getName()).isEqualTo("John Doe");
        assertThat(firstCustomer.getBalance()).isEqualByComparingTo(new BigDecimal("100.50"));
        assertThat(firstCustomer.getCart()).isNotNull();

        Customer secondCustomer = customers.get(1);
        assertThat(secondCustomer.getUserName()).isEqualTo("jane_smith");
        assertThat(secondCustomer.getId()).isEqualTo(2L);
        assertThat(secondCustomer.getBalance()).isEqualByComparingTo(new BigDecimal("250.75"));

        Customer thirdCustomer = customers.get(2);
        assertThat(thirdCustomer.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldReturnEmptyListForEmptyFile(@TempDir Path tempDir) throws IOException {
        // Given
        Path emptyFile = tempDir.resolve("empty.csv");
        Files.writeString(emptyFile, "");

        // When
        List<Customer> customers = customerReader.read(emptyFile.toString());

        // Then
        assertThat(customers).isEmpty();
    }

    @Test
    void shouldHandleSingleCustomer(@TempDir Path tempDir) throws IOException {
        // Given
        Path customerFile = tempDir.resolve("single_customer.csv");
        Files.writeString(customerFile, "admin,admin123,999,Administrator,1000.00");

        // When
        List<Customer> customers = customerReader.read(customerFile.toString());

        // Then
        assertThat(customers).hasSize(1);
        Customer customer = customers.get(0);
        assertThat(customer.getUserName()).isEqualTo("admin");
        assertThat(customer.getId()).isEqualTo(999L);
        assertThat(customer.getBalance()).isEqualByComparingTo(new BigDecimal("1000.00"));
    }
}