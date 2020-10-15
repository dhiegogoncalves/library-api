package com.project.libraryapi.api.resources;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.libraryapi.api.dtos.LoanFilterDTO;
import com.project.libraryapi.api.dtos.ReturnedLoanDTO;
import com.project.libraryapi.api.exceptions.BusinessException;
import com.project.libraryapi.models.entities.Book;
import com.project.libraryapi.models.entities.Loan;
import com.project.libraryapi.services.BookService;
import com.project.libraryapi.services.LoanService;

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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest(controllers = LoanController.class)
@AutoConfigureMockMvc
class LoanControllerTest {

    static String LOAN_API_URL = "/api/loans";

    @Autowired
    MockMvc mockMvc;

    @MockBean
    LoanService loanService;

    @MockBean
    BookService bookService;

    @Test
    @DisplayName("Deve filtrar emprestimos")
    void findLoansTest() throws Exception {
        LoanFilterDTO loanFilterDTO = LoanFilterDTO.builder().isbn("123456").customer("user").build();
        Book book = Book.builder().id(1l).title("The legend").author("Test").isbn("123456").build();
        Loan loan = Loan.builder().id(1l).book(book).customer("user").loanDate(LocalDate.now()).build();

        BDDMockito.given(loanService.find(Mockito.any(LoanFilterDTO.class), Mockito.any(Pageable.class)))
                .willReturn(new PageImpl<Loan>(Arrays.asList(loan), PageRequest.of(0, 100), 1));

        String queryString = String.format("?isbn=%s&customer=%s&page=0&size=100", loanFilterDTO.getIsbn(),
                loanFilterDTO.getCustomer());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(LOAN_API_URL.concat(queryString))
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("content", Matchers.hasSize(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("totalElements").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("pageable.pageSize").value(100))
                .andExpect(MockMvcResultMatchers.jsonPath("pageable.pageNumber").value(0));
    }

    @Test
    @DisplayName("Deve realizar um emprestimo")
    void createLoanTest() throws Exception {
        LoanFilterDTO loanDTO = LoanFilterDTO.builder().isbn("123456").customer("user").build();
        String json = new ObjectMapper().writeValueAsString(loanDTO);

        BDDMockito.given(bookService.getBookByIsbn(loanDTO.getIsbn()))
                .willReturn(Optional.of(Book.builder().isbn("123456").build()));

        Book book = Book.builder().id(1l).title("The legend").author("Test").isbn("123456").build();
        Loan loan = Loan.builder().id(1l).customer("user").book(book).loanDate(LocalDate.now()).build();
        BDDMockito.given(loanService.save(Mockito.any(Loan.class))).willReturn(loan);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(LOAN_API_URL)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON).content(json);

        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.content().string("1"));
    }

    @Test
    @DisplayName("Deve retornar erro ao tentar fazer emprestimo de um livro inexistente")
    void invalidIsbnCreateLoanTest() throws Exception {
        LoanFilterDTO loanDTO = LoanFilterDTO.builder().isbn("123456").customer("user").build();
        String json = new ObjectMapper().writeValueAsString(loanDTO);

        BDDMockito.given(bookService.getBookByIsbn(loanDTO.getIsbn())).willReturn(Optional.empty());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(LOAN_API_URL)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON).content(json);

        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("errors", Matchers.hasSize(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("errors[0]").value("Book not found"));
    }

    @Test
    @DisplayName("Deve retornar erro ao tentar fazer emprestimo de um livro emprestado")
    void loanedBookErrorOnCreateLoanTest() throws Exception {
        LoanFilterDTO loanDTO = LoanFilterDTO.builder().isbn("123456").customer("user").build();
        String json = new ObjectMapper().writeValueAsString(loanDTO);

        BDDMockito.given(bookService.getBookByIsbn(loanDTO.getIsbn()))
                .willReturn(Optional.of(Book.builder().isbn("123456").build()));

        BDDMockito.given(loanService.save(Mockito.any(Loan.class)))
                .willThrow(new BusinessException("Book already loaned"));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(LOAN_API_URL)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON).content(json);

        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("errors", Matchers.hasSize(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("errors[0]").value("Book already loaned"));
    }

    @Test
    @DisplayName("Deve retorna um livro")
    void returnBookTest() throws Exception {
        ReturnedLoanDTO returnedLoanDTO = ReturnedLoanDTO.builder().returned(true).build();
        String json = new ObjectMapper().writeValueAsString(returnedLoanDTO);

        Loan loan = Loan.builder().id(1l).build();
        BDDMockito.given(loanService.getById(Mockito.anyLong())).willReturn(Optional.of(loan));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.patch(LOAN_API_URL.concat("/1"))
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON).content(json);

        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
        Mockito.verify(loanService, Mockito.times(1)).update(loan);
    }

    @Test
    @DisplayName("Deve retorna 404 quando tentar devolver um livro inexistente")
    void returnInexistentBookTest() throws Exception {
        ReturnedLoanDTO returnedLoanDTO = ReturnedLoanDTO.builder().returned(true).build();
        String json = new ObjectMapper().writeValueAsString(returnedLoanDTO);

        BDDMockito.given(loanService.getById(Mockito.anyLong())).willReturn(Optional.empty());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.patch(LOAN_API_URL.concat("/1"))
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON).content(json);

        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isNotFound());
    }
}
