package de.florianbeetz.ma.rest.order.client.inventory;

import java.util.Collections;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AutoConfigureTestDatabase
@EnableAutoConfiguration(exclude = {SecurityAutoConfiguration.class})
class PagedIteratorTest {

    private static final String PAGE_URL = "http://localhost/page";

    private static final ParameterizedTypeReference<CollectionModel<String>> TYPE_REFERENCE = new ParameterizedTypeReference<>() {};

    @Mock
    private RestTemplate restTemplate;

    @Test
    void pagedIteratorReturnsItemsOfSinglePage() {
        CollectionModel<String> page = new CollectionModel<>(Collections.singletonList("first"),
                new Link(PAGE_URL, "first"), new Link(PAGE_URL, "last"));

        Mockito.when(restTemplate.exchange(PAGE_URL, HttpMethod.GET, null, TYPE_REFERENCE))
               .thenReturn(new ResponseEntity<>(page, HttpStatus.OK));

        PagedIterator<String> iterator = new PagedIterator<>(PAGE_URL, restTemplate, TYPE_REFERENCE);

        assertTrue(iterator.hasNext());
        assertEquals("first", iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    void pagedIteratorReturnsItemsOfMultiplePages() {
        CollectionModel<String> page1 = new CollectionModel<>(Collections.singletonList("first"),
                new Link(PAGE_URL, "first"), new Link(PAGE_URL + "?page=1", "next"), new Link(PAGE_URL + "?page=1", "last"));
        CollectionModel<String> page2 = new CollectionModel<>(Collections.singletonList("second"),
                new Link(PAGE_URL, "first"), new Link(PAGE_URL, "previous"), new Link(PAGE_URL + "?page=1", "last"));

        Mockito.when(restTemplate.exchange(PAGE_URL, HttpMethod.GET, null, TYPE_REFERENCE))
               .thenReturn(new ResponseEntity<>(page1, HttpStatus.OK));
        Mockito.when(restTemplate.exchange(PAGE_URL + "?page=1", HttpMethod.GET, null, TYPE_REFERENCE))
               .thenReturn(new ResponseEntity<>(page2, HttpStatus.OK));

        PagedIterator<String> iterator = new PagedIterator<>(PAGE_URL, restTemplate, TYPE_REFERENCE);

        assertTrue(iterator.hasNext());
        assertEquals("first", iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals("second", iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    void emptyPagedIteratorHasNoElements() {
        CollectionModel<String> page = new CollectionModel<>(Collections.emptyList(),
                new Link(PAGE_URL, "first"), new Link(PAGE_URL, "last"));

        Mockito.when(restTemplate.exchange(PAGE_URL, HttpMethod.GET, null, TYPE_REFERENCE))
               .thenReturn(new ResponseEntity<>(page, HttpStatus.OK));

        PagedIterator<String> iterator = new PagedIterator<>(PAGE_URL, restTemplate, TYPE_REFERENCE);

        assertFalse(iterator.hasNext());
        assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    void pagedIteratorThrowsNoSuchElementExceptionWhenMovedToEnd() {
        CollectionModel<String> page = new CollectionModel<>(Collections.singletonList("first"),
                new Link(PAGE_URL, "first"), new Link(PAGE_URL, "last"));

        Mockito.when(restTemplate.exchange(PAGE_URL, HttpMethod.GET, null, TYPE_REFERENCE))
               .thenReturn(new ResponseEntity<>(page, HttpStatus.OK));

        PagedIterator<String> iterator = new PagedIterator<>(PAGE_URL, restTemplate, TYPE_REFERENCE);
        iterator.next();

        assertFalse(iterator.hasNext());
        assertThrows(NoSuchElementException.class, iterator::next);
    }

}