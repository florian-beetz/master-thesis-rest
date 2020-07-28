package de.florianbeetz.ma.rest.inventory.api.v1;

import de.florianbeetz.ma.rest.inventory.data.ItemEntity;
import de.florianbeetz.ma.rest.inventory.data.ItemRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureTestDatabase
@AutoConfigureMockMvc
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ItemRepository itemRepository;

    @Test
    void findAllItemsShouldReturnAllItems() throws Exception {
        itemRepository.save(new ItemEntity(null, "Item 1", null, 3.99));
        itemRepository.save(new ItemEntity(null, "Item 2", null, 5));

        mockMvc.perform(get("/api/v1/item/"))
               .andExpect(status().isOk())
               .andExpect(content().contentType(new MediaType("application", "hal+json")))
               .andExpect(jsonPath("$._embedded.itemList", hasSize(2)))
               .andExpect(jsonPath("$._embedded.itemList[0].title", is("Item 1")))
               .andExpect(jsonPath("$._embedded.itemList[0].price", is(3.99)))
               .andExpect(jsonPath("$._embedded.itemList[1].title", is("Item 2")))
               .andExpect(jsonPath("$._embedded.itemList[1].price", is(5d)));
    }

    @Test
    void nonExistentItemReturns404() throws Exception {
        mockMvc.perform(get("/api/v1/item/1"))
               .andExpect(status().isNotFound());
    }
}