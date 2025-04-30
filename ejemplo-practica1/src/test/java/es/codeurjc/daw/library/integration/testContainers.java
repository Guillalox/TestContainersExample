
@SpringBootTest
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BookServiceIntegrationTest {

    @Container
    private static final MySQLContainer<?> mysql =
            new MySQLContainer<>(DockerImageName.parse("mysql:5.7.34"))
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }

    @Autowired
    private BookService bookService;

    @BeforeEach
    void cleanup() {
        bookService.findAll().forEach(b -> bookService.delete(b.getId()));
    }

    @Test
    public void testSaveAndFindBook() {
        Book book = new Book("Clean Code", "Robert C. Martin");
        bookService.save(book);

        Optional<Book> result = bookService.findById(book.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("Clean Code");
    }

    @Test
    public void testBookExistsById() {
        Book book = new Book("Effective Java", "Joshua Bloch");
        bookService.save(book);

        boolean exists = bookService.exist(book.getId());

        assertThat(exists).isTrue();
    }

    @Test
    public void testDeleteBook() {
        Book book = new Book("Refactoring", "Martin Fowler");
        bookService.save(book);

        bookService.delete(book.getId());

        assertThat(bookService.findById(book.getId())).isEmpty();
    }

    @Test
    public void testFindAllBooks() {
        bookService.save(new Book("Book 1", "Author 1"));
        bookService.save(new Book("Book 2", "Author 2"));

        List<Book> books = bookService.findAll();

        assertThat(books).hasSizeGreaterThanOrEqualTo(2);
    }



}