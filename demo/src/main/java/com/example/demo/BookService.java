package com.example.demo;

import java.util.List;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.jooq.Tables;
import com.example.demo.jooq.tables.pojos.Book;

@Service
public class BookService {

	@Autowired
	DSLContext context;

	public List<Book> getBooks() {
		return context.selectFrom(Tables.BOOK).fetchInto(Book.class);
	}

	@Transactional
	public void insertBook(Book book) throws Exception {
		context.insertInto(Tables.BOOK, Tables.BOOK.AUTHOR, Tables.BOOK.AUTHOR)
				.values(book.getTitle(), book.getAuthor()).execute();
		throw new Exception();
	}
}