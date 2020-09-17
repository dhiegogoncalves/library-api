package com.project.libraryapi.api.resources;

import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.libraryapi.api.dtos.BookDTO;
import com.project.libraryapi.api.exceptions.BusinessException;
import com.project.libraryapi.models.entities.Book;
import com.project.libraryapi.services.BookService;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest
@AutoConfigureMockMvc
class BookControllerTest {

    static String BOOK_API_URL = "/api/books";

    @Autowired
    MockMvc mockMvc;

    @MockBean
    BookService bookService;

    @Test
    @DisplayName("Deve obter informacoes de um livro")
    void getBookDetailsTest() throws Exception {
        Long id = 1l;
        Book book = Book.builder().id(1l).title("The legend").author("Test").isbn("123456").build();

        BDDMockito.given(bookService.getById(id)).willReturn(Optional.of(book));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(BOOK_API_URL.concat("/" + id))
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value(book.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("title").value(book.getTitle()))
                .andExpect(MockMvcResultMatchers.jsonPath("author").value(book.getAuthor()))
                .andExpect(MockMvcResultMatchers.jsonPath("isbn").value(book.getIsbn()));
    }

    @Test
    @DisplayName("Deve retornar resource not found quando o livro procurado não existir")
    void bookNotFoundTest() throws Exception {
        Long id = 1l;
        BDDMockito.given(bookService.getById(id)).willReturn(Optional.empty());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(BOOK_API_URL.concat("/" + id))
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @DisplayName("Deve criar um livro com sucesso")
    void createBookTest() throws Exception {
        BookDTO bookDTO = BookDTO.builder().title("The legend").author("Test").isbn("123456").build();
        Book savedBook = Book.builder().id(1l).title("The legend").author("Test").isbn("123456").build();

        BDDMockito.given(bookService.save(Mockito.any(Book.class))).willReturn(savedBook);

        String json = new ObjectMapper().writeValueAsString(savedBook);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(BOOK_API_URL)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content(json);

        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value(1l))
                .andExpect(MockMvcResultMatchers.jsonPath("title").value(bookDTO.getTitle()))
                .andExpect(MockMvcResultMatchers.jsonPath("author").value(bookDTO.getAuthor()))
                .andExpect(MockMvcResultMatchers.jsonPath("isbn").value(bookDTO.getIsbn()));
    }

    @Test
    @DisplayName("Deve lançar erro de validação quando não houver dados suficiente para criação do livro")
    void createInvalidBookTest() throws Exception {
        String json = new ObjectMapper().writeValueAsString(new BookDTO());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(BOOK_API_URL)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content(json);

        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("errors", Matchers.hasSize(3)));
    }

    @Test
    @DisplayName("Deve lançar erro ao tentar cadastrar um livro com isbn já utilizado por outro")
    void createBookWithDuplicatedIsbnTest() throws Exception {
        BookDTO bookDTO = BookDTO.builder().title("The legend").author("Test").isbn("123456").build();
        String json = new ObjectMapper().writeValueAsString(bookDTO);

        BDDMockito.given(bookService.save(Mockito.any(Book.class)))
                .willThrow(new BusinessException("Isbn já cadastrado"));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(BOOK_API_URL)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content(json);

        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("errors", Matchers.hasSize(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("errors[0]").value("Isbn já cadastrado"));
    }

    @Test
    @DisplayName("Deve atualizar um livro")
    void updateBookTest() throws Exception {
        Long id = 1l;
        BookDTO bookDTO = BookDTO.builder().id(1l).title("The legend2").author("Test2").isbn("1234562").build();
        String json = new ObjectMapper().writeValueAsString(bookDTO);

        Book book = Book.builder().id(1l).title("The legend").author("Test").isbn("123456").build();
        BDDMockito.given(bookService.getById(id)).willReturn(Optional.of(book));

        Book updatedBook = Book.builder().id(1l).title(bookDTO.getTitle()).author(bookDTO.getAuthor()).isbn("123456")
                .build();
        BDDMockito.given(bookService.update(book)).willReturn(updatedBook);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put(BOOK_API_URL.concat("/" + id))
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content(json);

        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value(id))
                .andExpect(MockMvcResultMatchers.jsonPath("title").value(bookDTO.getTitle()))
                .andExpect(MockMvcResultMatchers.jsonPath("author").value(bookDTO.getAuthor()))
                .andExpect(MockMvcResultMatchers.jsonPath("isbn").value(book.getIsbn()));
    }

    @Test
    @DisplayName("Deve retornar 404 ao tentar atualizar um livro inexistente")
    void updateInexistentBookTest() throws Exception {
        Long id = 1l;
        BookDTO bookDTO = BookDTO.builder().id(1l).title("The legend2").author("Test2").isbn("1234562").build();
        String json = new ObjectMapper().writeValueAsString(bookDTO);

        BDDMockito.given(bookService.getById(id)).willReturn(Optional.empty());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put(BOOK_API_URL.concat("/" + id))
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content(json);

        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @DisplayName("Deve deletar um livro")
    void deleteBookTest() throws Exception {
        Long id = 1l;
        Book book = Book.builder().id(1l).title("The legend").author("Test").isbn("123456").build();

        BDDMockito.given(bookService.getById(id)).willReturn(Optional.of(book));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete(BOOK_API_URL.concat("/" + id));

        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    @DisplayName("Deve retornar resource not found quando não encontrar o livro para deletar")
    void deleteInexistentBookTest() throws Exception {
        Long id = 1l;
        BDDMockito.given(bookService.getById(id)).willReturn(Optional.empty());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete(BOOK_API_URL.concat("/" + id));

        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isNotFound());
    }
}