package de.florianbeetz.ma.rest.order.client.inventory;

import java.util.Iterator;
import java.util.NoSuchElementException;

import lombok.val;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

public class PagedIterator<T> implements Iterator<T> {

    private final String url;
    private final RestTemplate template;
    private final ParameterizedTypeReference<CollectionModel<T>> reference;

    private CollectionModel<T> currentList = null;
    private Iterator<T> currentIterator = null;

    public PagedIterator(String url, RestTemplate template, ParameterizedTypeReference<CollectionModel<T>> reference) {
        this.url = url;
        this.template = template;
        this.reference = reference;
    }

    @Override
    public boolean hasNext() {
        if (currentIterator == null || !currentIterator.hasNext()) {
            if (!fetchNext()) {
                return false;
            } else {
                currentIterator = currentList.getContent().iterator();
            }
        }

        return currentIterator.hasNext();
    }

    @Override
    public T next() {
        if (currentIterator == null || !currentIterator.hasNext()) {
            if (!fetchNext()) {
                throw new NoSuchElementException();
            } else {
                currentIterator = currentList.getContent().iterator();
            }
        }

        return currentIterator.next();
    }

    private boolean fetchNext() {
        String nextUrl;
        if (currentList == null) {
            nextUrl = this.url;
        } else {
            val link = currentList.getLink("next");
            if (link.isEmpty()) { // no next page
                return false;
            }
            nextUrl = link.get().getHref();
        }

        val response = template.exchange(nextUrl, HttpMethod.GET, null, reference);
        currentList = response.getBody();
        return true;
    }
}
