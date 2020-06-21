package de.florianbeetz.ma.rest.inventory;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;
import org.springframework.data.domain.Page;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.LinkRelation;

import java.util.List;
import java.util.function.BiFunction;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

public class PagingUtil {

    /** link relation for the first page */
    public static final LinkRelation FIRST_RELATION = LinkRelation.of("first");
    /** link relation for the previous page */
    public static final LinkRelation PREVIOUS_RELATION = LinkRelation.of("previous");
    /** link relation for the next page */
    public static final LinkRelation NEXT_RELATION = LinkRelation.of("next");
    /** link relation for the last page */
    public static final LinkRelation LAST_RELATION = LinkRelation.of("last");

    private PagingUtil() {}

    /**
     * Creates a paginated {@link CollectionModel} of the given items and adds links for navigating the pages.
     *
     * @param   items
     *          the items to use.
     *
     * @param   page
     *          the page where the items originate from.
     *
     * @param   currentPage
     *          the current page.
     *
     * @param   size
     *          the size of the pages.
     *
     * @param   invocationTarget
     *          a function accepting a page and a page size to generate invocation targets for page links.
     *
     * @param   <T>
     *          type of the resources of the page.
     *
     * @return  the created {@link CollectionModel}.
     */
    public static <T> CollectionModel<T> getCollection(List<T> items, Page<?> page, int currentPage, int size, BiFunction<Integer, Integer, Object> invocationTarget) {
        val collectionModel = new CollectionModel<>(items);

        collectionModel.add(linkTo(invocationTarget.apply(currentPage, size)).withSelfRel());
        collectionModel.add(linkTo(invocationTarget.apply(0, size)).withRel(FIRST_RELATION));
        if (currentPage > 0) {
            collectionModel.add(linkTo(invocationTarget.apply(currentPage - 1, size)).withRel(PREVIOUS_RELATION));
        }
        if (currentPage < page.getTotalPages() - 1) {
            collectionModel.add(linkTo(invocationTarget.apply(currentPage + 1, size)).withRel(NEXT_RELATION));
        }
        collectionModel.add(linkTo(invocationTarget.apply(Math.max(page.getTotalPages() - 1, 0), size)).withRel(LAST_RELATION));
        return collectionModel;
    }

}
