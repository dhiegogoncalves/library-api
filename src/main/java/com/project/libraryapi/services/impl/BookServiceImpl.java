package com.project.libraryapi.services.impl;

import java.util.Optional;

import com.project.libraryapi.api.exceptions.BusinessException;
import com.project.libraryapi.models.entities.Book;
import com.project.libraryapi.models.repositories.BookRepository;
import com.project.libraryapi.services.BookService;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;

    @Override
    public Optional<Book> getById(Long id) {
        return this.bookRepository.findById(id);
    }

    @Override
    public Book save(Book book) {
        if (bookRepository.existsByIsbn(book.getIsbn())) {
            throw new BusinessException("Isbn já cadastrado");
        }
        return bookRepository.save(book);
    }

    @Override
    public Book update(Book book) {
        if (book == null || book.getId() == null) {
            throw new IllegalArgumentException("Book id can't be null");
        }
        return this.bookRepository.save(book);
    }

    @Override
    public void delete(Book book) {
        if (book == null || book.getId() == null) {
            throw new IllegalArgumentException("Book id can't be null");
        }
        this.bookRepository.delete(book);
    }
}
