package es.codeurjc.daw.library.integration;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;      
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;            
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import es.codeurjc.daw.library.Application;
import es.codeurjc.daw.library.model.Book;
import es.codeurjc.daw.library.model.Shop;
import es.codeurjc.daw.library.service.BookService;
import es.codeurjc.daw.library.service.ShopService;

// Indicamos que esta clase utilizará Testcontainers para las pruebas
@Testcontainers
@SpringBootTest(classes = Application.class) // Esta anotación carga el contexto de Spring Boot para la prueba (permite el uso de Servicios, Repositorios...).
@ExtendWith(SpringExtension.class) // Permite la integración de Spring con JUnit 5.
public class BookServiceIntegrationTest {

    // Se crea un contenedor estático de MySQL usando Testcontainers
    @Container
    private static final MySQLContainer<?> mysql =
            new MySQLContainer<>(DockerImageName.parse("mysql:5.7.34"))
                    .withDatabaseName("testdb")     // Se define el nombre de la base de datos
                    .withUsername("test")          // Se define el nombre de usuario para acceder a la base de datos
                    .withPassword("test");         // Se define la contraseña para la base de datos


    // Método que configura las propiedades dinámicas para conectar Spring Boot a MySQL dentro del contenedor
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);   // URL de la base de datos que proporciona Testcontainers
        registry.add("spring.datasource.username", mysql::getUsername);  // Nombre de usuario de la base de datos
        registry.add("spring.datasource.password", mysql::getPassword);  // Contraseña de la base de datos
    }

    @Autowired
    private BookService bookService;

    @Autowired
    private ShopService shopService;


    @BeforeEach
    void cleanup() {
        bookService.findAll().forEach(b -> bookService.delete(b.getId()));
        shopService.findAll().forEach(s -> shopService.delete(s.getId()));
    }

    private Shop createShop(String name, String address) {
        Shop shop = new Shop();
        shop.setName(name);
        shop.setAddress(address);
        shopService.save(shop);
        return shop;
    }

    private Book createBook(String title, String author) {
        Shop shop = createShop("Default Shop", "Default Address");
        Shop shop2 = createShop("Default Shop 2", "Default Address 2");
        List<Shop> shops = new ArrayList<>();
        shops.add(shop);
        shops.add(shop2);

        Book book = new Book(title, author, shops);
        return book;
    }


    @Test
    public void testSaveAndFindBook() {
        Book book = createBook("Clean Code", "Robert C. Martin");
        bookService.save(book);

        Optional<Book> result = bookService.findById(book.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("Clean Code");    
    }

    @Test
    public void testBookExistsById() {
        Book book = createBook("Effective Java", "Joshua Bloch");
        bookService.save(book);

        boolean exists = bookService.exist(book.getId());

        assertThat(exists).isTrue();
    }

    @Test
    public void testDeleteBook() {
        Book book = createBook("Refactoring", "Martin Fowler");
        bookService.save(book);

        boolean exists = bookService.exist(book.getId());

        assertThat(exists).isTrue();

        bookService.delete(book.getId());

        assertThat(bookService.findById(book.getId())).isEmpty();
    }

    @Test
    public void testFindAllBooks() {
        bookService.save(createBook("Book 1", "Author 1"));
        bookService.save(createBook("Book 2", "Author 2"));

        List<Book> books = bookService.findAll();

        assertThat(books).hasSizeGreaterThanOrEqualTo(2);
    }
}



