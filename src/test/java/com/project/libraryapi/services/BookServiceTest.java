package com.project.libraryapi.services;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.project.libraryapi.api.exceptions.BusinessException;
import com.project.libraryapi.models.entities.Book;
import com.project.libraryapi.models.repositories.BookRepository;
import com.project.libraryapi.services.impl.BookServiceImpl;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
class BookServiceTest {

    BookService bookService;

    @MockBean
    BookRepository bookRepository;

    @BeforeEach
    void setUp() {
        this.bookService = new BookServiceImpl(bookRepository);
    }

    @Test
    @DisplayName("Deve filtrar livros pelas propriedades")
    void findBooksTest() {
        Book book = Book.builder().id(1l).title("The legend").author("Test").isbn("123456").build();
        List<Book> bookList = Arrays.asList(book);
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Book> page = new PageImpl<Book>(bookList, pageRequest, 1);

        Mockito.when(bookRepository.findAll(Mockito.any(Example.class), Mockito.any(PageRequest.class)))
                .thenReturn(page);

        Page<Book> resutl = bookService.find(book, pageRequest);

        Assertions.assertThat(resutl.getTotalElements()).isEqualTo(1);
        Assertions.assertThat(resutl.getContent()).isEqualTo(bookList);
        Assertions.assertThat(resutl.getPageable().getPageNumber()).isEqualTo(0);
        Assertions.assertThat(resutl.getPageable().getPageSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("Deve obter um livro por Id")
    void getByIdTest() {
        Long id = 1l;
        Book book = Book.builder().id(id).title("The legend").author("Test").isbn("123456").build();
        Mockito.when(bookRepository.findById(id)).thenReturn(Optional.of(book));

        Optional<Book> foundBook = bookService.getById(id);

        Assertions.assertThat(foundBook.isPresent()).isTrue();
        Assertions.assertThat(foundBook.get().getId()).isEqualTo(book.getId());
        Assertions.assertThat(foundBook.get().getTitle()).isEqualTo(book.getTitle());
        Assertions.assertThat(foundBook.get().getAuthor()).isEqualTo(book.getAuthor());
        Assertions.assertThat(foundBook.get().getIsbn()).isEqualTo(book.getIsbn());
    }

    @Test
    @DisplayName("Deve retornar vazio ao obter um livro por Id quando ele nao existe na base")
    void bookNotFoundByIdTest() {
        Long id = 1l;
        Mockito.when(bookRepository.findById(id)).thenReturn(Optional.empty());

        Optional<Book> book = bookService.getById(id);

        Assertions.assertThat(book.isPresent()).isFalse();
    }

    @Test
    @DisplayName("Deve salvar o livro")
    void saveBookTest() {
        Book book = Book.builder().title("The legend").author("Test").isbn("123456").build();

        Mockito.when(bookRepository.save(book))
                .thenReturn(Book.builder().id(1l).title("The legend").author("Test").isbn("123456").build());

        Book savedBook = bookService.save(book);

        Assertions.assertThat(savedBook.getId()).isNotNull();
        Assertions.assertThat(savedBook.getTitle()).isEqualTo("The legend");
        Assertions.assertThat(savedBook.getAuthor()).isEqualTo("Test");
        Assertions.assertThat(savedBook.getIsbn()).isEqualTo("123456");
    }

    @Test
    @DisplayName("Deve atualizar um livro")
    void updateBookTest() {
        Book updatingBook = Book.builder().id(1l).title("The legend").author("Test").isbn("123456").build();
        Book updatedBook = Book.builder().id(1l).title("The legend2").author("Test2").isbn("123456").build();

        Mockito.when(bookRepository.save(updatingBook)).thenReturn(updatedBook);

        Book book = bookService.update(updatingBook);

        Assertions.assertThat(book.getId()).isEqualTo(updatedBook.getId());
        Assertions.assertThat(book.getTitle()).isEqualTo(updatedBook.getTitle());
        Assertions.assertThat(book.getAuthor()).isEqualTo(updatedBook.getAuthor());
        Assertions.assertThat(book.getIsbn()).isEqualTo(updatedBook.getIsbn());
    }

    @Test
    @DisplayName("Deve ocorrer erro ao tentar atualizar um livro inexistente")
    void updateInvalidBookTest() {
        Book book = new Book();

        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> bookService.update(book));

        Mockito.verify(bookRepository, Mockito.never()).save(book);
    }

    @Test
    @DisplayName("Deve deletar um livro")
    void deleteBookTest() {
        Book book = Book.builder().id(1l).title("The legend").author("Test").isbn("123456").build();

        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> bookService.delete(book));

        Mockito.verify(bookRepository, Mockito.times(1)).delete(book);
    }

    @Test
    @DisplayName("Deve ocorrer erro ao tentar deletar um livro inexistente")
    void deleteInvalidBookTest() {
        Book book = new Book();

        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> bookService.delete(book));

        Mockito.verify(bookRepository, Mockito.never()).delete(book);
    }
}
