package es.codeurjc.daw.library.mocks;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.codeurjc.daw.library.model.Book;
import es.codeurjc.daw.library.model.Shop;
import es.codeurjc.daw.library.service.BookService;
import es.codeurjc.daw.library.service.ShopService;
import es.codeurjc.daw.library.repository.BookRepository;
import es.codeurjc.daw.library.repository.ShopRepository;
import org.mockito.InjectMocks;

@ExtendWith(MockitoExtension.class)
public class BookServiceWithMockedRepoTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private ShopRepository shopRepository;

    @InjectMocks
    private BookService bookService;

    @InjectMocks
    private ShopService shopService;

    private Book book;
    private Shop shop1;
    private Shop shop2;

    @BeforeEach
    void setUp() {
        shop1 = new Shop();
        shop1.setId(1L);
        shop1.setName("Shop 1");

        shop2 = new Shop();
        shop2.setId(2L);
        shop2.setName("Shop 2");

        book = new Book("Clean Code", "Robert C. Martin", List.of(shop1, shop2));
        book.setId(1L);
    }

    @Test
    public void testSaveAndFindBook() {
        when(bookRepository.save(any(Book.class))).thenReturn(book);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        bookService.save(book);
        Optional<Book> found = bookService.findById(1L);

        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Clean Code");
    }

    @Test
    public void testBookExistsById() {
        when(bookRepository.existsById(1L)).thenReturn(true);

        boolean exists = bookService.exist(1L);

        assertThat(exists).isTrue();
    }

    @Test
    public void testDeleteBook() {
        doNothing().when(bookRepository).deleteById(1L);
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        bookService.delete(1L);
        Optional<Book> result = bookService.findById(1L);

        assertThat(result).isEmpty();
    }

    @Test
    public void testFindAllBooks() {
        Book book2 = new Book("Effective Java", "Joshua Bloch", List.of(shop1));
        book2.setId(2L);

        when(bookRepository.findAll()).thenReturn(List.of(book, book2));

        List<Book> allBooks = bookService.findAll();

        assertThat(allBooks).hasSize(2);
    }
}