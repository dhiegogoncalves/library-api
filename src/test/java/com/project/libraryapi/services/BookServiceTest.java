package com.project.libraryapi.services;

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
}
