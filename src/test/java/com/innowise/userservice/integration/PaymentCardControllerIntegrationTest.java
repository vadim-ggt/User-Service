package com.innowise.userservice.integration;

import com.innowise.userservice.domain.dao.PaymentCardRepository;
import com.innowise.userservice.web.dto.card.CreateCardDto;
import com.innowise.userservice.web.dto.card.GetCardDto;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.userservice.domain.dao.UserRepository;
import com.innowise.userservice.web.dto.user.CreateUserDto;
import com.innowise.userservice.web.dto.user.GetUserDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Objects;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)

public class PaymentCardControllerIntegrationTest extends BaseIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentCardRepository paymentCardRepository;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    @AfterEach
    void cleanup() {
        paymentCardRepository.deleteAll();
        userRepository.deleteAll();
        Objects.requireNonNull(cacheManager.getCache("cards")).clear();
    }

    private Long createUser() throws Exception {
        CreateUserDto dto = new CreateUserDto();
        dto.setName("Ivan");
        dto.setSurname("Ivanov");
        dto.setEmail("ivan_" + System.nanoTime() + "@mail.com"); // для поиска с айди юзера

        MvcResult result = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn();

        GetUserDto user = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                GetUserDto.class
        );
        return user.getId();
    }


    private CreateCardDto createCardDto() {
        CreateCardDto dto = new CreateCardDto();
        dto.setNumber("400000000000" + System.nanoTime());
        dto.setHolder("IVAN IVANOV");
        dto.setExpirationDate("12/30");
        return dto;
    }

    @Test
    void test_createCard_Success() throws Exception {
        Long userId = createUser();

        mockMvc.perform(post("/api/cards/user/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createCardDto())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.holder").value("IVAN IVANOV")) // не очень поня
                .andExpect(jsonPath("$.expirationDate").value("12/30"))
                .andExpect(jsonPath("$.active").value(true));

        assertEquals(1, paymentCardRepository.count());
    }

    @Test
    void getCardById_success() throws Exception {
        Long userId = createUser();

        MvcResult result = mockMvc.perform(post("/api/cards/user/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createCardDto())))
                .andReturn();

        Long cardId = objectMapper
                .readValue(result.getResponse().getContentAsString(), GetCardDto.class)
                .getId();

        mockMvc.perform(get("/api/cards/{id}", cardId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(cardId));
    }

    @Test
    void getCardById_notFound() throws Exception {
        mockMvc.perform(get("/api/cards/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateCard_success() throws Exception {
        Long userId = createUser();

        Long cardId = objectMapper.readValue(
                mockMvc.perform(post("/api/cards/user/{userId}", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createCardDto())))
                        .andReturn()
                        .getResponse()
                        .getContentAsString(),
                GetCardDto.class
        ).getId();

        CreateCardDto update = new CreateCardDto();
        update.setNumber("9999888877776666");
        update.setHolder("NEW HOLDER");
        update.setExpirationDate("11/29");

        mockMvc.perform(put("/api/cards/{id}", cardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value("9999888877776666"));
    }

    @Test
    void patchActive_success() throws Exception {
        Long userId = createUser();

        Long cardId = objectMapper.readValue(
                mockMvc.perform(post("/api/cards/user/{userId}", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createCardDto())))
                        .andReturn()
                        .getResponse()
                        .getContentAsString(),
                GetCardDto.class
        ).getId();

        mockMvc.perform(patch("/api/cards/{id}/active", cardId)
                        .param("active", "false"))
                .andExpect(status().isNoContent());

        assertFalse(
                paymentCardRepository.findById(cardId).orElseThrow().getActive()
        );
    }

    @Test
    void deleteCard_success() throws Exception {
        Long userId = createUser();

        Long cardId = objectMapper.readValue(
                mockMvc.perform(post("/api/cards/user/{userId}", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createCardDto())))
                        .andReturn()
                        .getResponse()
                        .getContentAsString(),
                GetCardDto.class
        ).getId();

        mockMvc.perform(delete("/api/cards/{id}", cardId))
                .andExpect(status().isNoContent());

        assertEquals(0, paymentCardRepository.count());
    }


    @Test
    void getAllCards_success() throws Exception {
        Long userId = createUser();

        mockMvc.perform(post("/api/cards/user/{userId}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createCardDto())));

        mockMvc.perform(post("/api/cards/user/{userId}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createCardDto())));

        mockMvc.perform(get("/api/cards")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1));
    }


    @Test
    void searchCards_byUserId_success() throws Exception {
        Long userId1 = createUser();

        mockMvc.perform(post("/api/cards/user/{userId}", userId1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createCardDto())));

        Long userId2 = createUser();
        CreateCardDto dto2 = createCardDto();
        dto2.setNumber("9999888877776666");

        mockMvc.perform(post("/api/cards/user/{userId}", userId2)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto2)));

        mockMvc.perform(get("/api/cards/search")
                        .param("userId", userId1.toString())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void searchCards_emptyResult() throws Exception {
        mockMvc.perform(get("/api/cards/search")
                        .param("holder", "NOT_EXISTS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void searchCards_ByFilterUserName() throws Exception {
        CreateUserDto userDto = new CreateUserDto();
        userDto.setName("John");
        userDto.setSurname("Doe");
        userDto.setEmail("john_" + System.nanoTime() + "@mail.com");

        MvcResult userResult = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated())
                .andReturn();

        Long userId = objectMapper.readValue(
                userResult.getResponse().getContentAsString(),
                GetUserDto.class
        ).getId();

        CreateCardDto cardDto = createCardDto();
        cardDto.setNumber("1111222233334444");
        mockMvc.perform(post("/api/cards/user/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardDto)))
                .andExpect(status().isCreated());

        CreateUserDto userDto2 = new CreateUserDto();
        userDto2.setName("Mike");
        userDto2.setSurname("Smith");
        userDto2.setEmail("mike_" + System.nanoTime() + "@mail.com");

        MvcResult userResult2 = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto2)))
                .andExpect(status().isCreated())
                .andReturn();

        Long userId2 = objectMapper.readValue(
                userResult2.getResponse().getContentAsString(),
                GetUserDto.class
        ).getId();

        CreateCardDto cardDto2 = createCardDto();
        cardDto2.setNumber("9999888877776666");
        mockMvc.perform(post("/api/cards/user/{userId}", userId2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardDto2)))
                .andExpect(status().isCreated());


        mockMvc.perform(get("/api/cards/search")
                        .param("userName", "John")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].number").value("1111222233334444"));
    }

    @Test
    void searchCards_ByFilterUserSurname() throws Exception {

        CreateUserDto userDto = new CreateUserDto();
        userDto.setName("John");
        userDto.setSurname("Smith");
        userDto.setEmail("john_" + System.nanoTime() + "@mail.com");

        MvcResult userResult = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated())
                .andReturn();

        Long userId = objectMapper.readValue(
                userResult.getResponse().getContentAsString(),
                GetUserDto.class
        ).getId();

        CreateCardDto cardDto = createCardDto();
        cardDto.setNumber("1111222233334444");
        mockMvc.perform(post("/api/cards/user/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardDto)))
                .andExpect(status().isCreated());

        CreateUserDto userDto2 = new CreateUserDto();
        userDto2.setName("Mike");
        userDto2.setSurname("Lolo");
        userDto2.setEmail("mike_" + System.nanoTime() + "@mail.com");

        MvcResult userResult2 = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto2)))
                .andExpect(status().isCreated())
                .andReturn();

        Long userId2 = objectMapper.readValue(
                userResult2.getResponse().getContentAsString(),
                GetUserDto.class
        ).getId();

        CreateCardDto cardDto2 = createCardDto();
        cardDto2.setNumber("9999888877776666");
        mockMvc.perform(post("/api/cards/user/{userId}", userId2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardDto2)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/cards/search")
                        .param("userSurname", "Smith")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void searchCards_ByFilterActive() throws Exception {
        Long userId = createUser();

        CreateCardDto activeCardDto = createCardDto();
        activeCardDto.setNumber("1111222233334444");
        mockMvc.perform(post("/api/cards/user/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(activeCardDto)))
                .andExpect(status().isCreated());

        CreateCardDto inactiveCardDto = createCardDto();
        inactiveCardDto.setNumber("5555444433332222");

        MvcResult inactiveCardResult = mockMvc.perform(post("/api/cards/user/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inactiveCardDto)))
                .andExpect(status().isCreated())
                .andReturn();

        Long inactiveCardId = objectMapper.readValue(
                inactiveCardResult.getResponse().getContentAsString(),
                GetCardDto.class
        ).getId();

        mockMvc.perform(patch("/api/cards/{id}/active", inactiveCardId)
                        .param("active", "false"))
                .andExpect(status().isNoContent());


        mockMvc.perform(get("/api/cards/search")
                        .param("active", "true")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));

        mockMvc.perform(get("/api/cards/search")
                        .param("active", "false")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void searchCards_withPagination() throws Exception {
        Long userId = createUser();

        for (int i = 0; i < 5; i++) {
            CreateCardDto dto = createCardDto();
            dto.setNumber("11112222333344" + i);

            mockMvc.perform(post("/api/cards/user/{userId}", userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)));
        }

        mockMvc.perform(get("/api/cards/search")
                        .param("page", "1")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.totalPages").value(3));
    }

}
