package com.innowise.userservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.userservice.domain.dao.UserRepository;
import com.innowise.userservice.web.dto.user.CreateUserDto;
import com.innowise.userservice.web.dto.user.GetUserDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.annotation.DirtiesContext;
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
public class UserControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    @AfterEach
    void cleanup() {
        userRepository.deleteAll();
        Objects.requireNonNull(cacheManager.getCache("users")).clear();
    }

    private CreateUserDto createTestUserDto() {
        CreateUserDto dto = new CreateUserDto();
        dto.setName("Ivan");
        dto.setSurname("Ivanov");
        dto.setEmail("ivan@mail.com");
        return dto;
    }

    @Test
    void test_createUser_Success() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createTestUserDto())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Ivan"));

        assertEquals(1, userRepository.count());
    }

    @Test
    void test_getUserById_Throws404_IfNotFound() throws Exception {
        mockMvc.perform(get("/api/users/{id}", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    void test_Cache_FullLifecycle_Get_Update_Delete() throws Exception {

        MvcResult result = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createTestUserDto())))
                .andExpect(status().isCreated())
                .andReturn();

        GetUserDto createdUser = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                GetUserDto.class
        );

        Long userId = createdUser.getId();

        assertNull(getCacheValue(userId));

        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ivan"));

        assertNotNull(getCacheValue(userId));
        assertEquals("Ivan", ((GetUserDto) getCacheValue(userId)).getName());

        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ivan"));

        CreateUserDto updateDto = createTestUserDto();
        updateDto.setName("Petr");

        mockMvc.perform(put("/api/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Petr"));

        assertNotNull(getCacheValue(userId));
        assertEquals("Petr", ((GetUserDto) getCacheValue(userId)).getName());

        mockMvc.perform(delete("/api/users/{id}", userId))
                .andExpect(status().isNoContent());

        assertNull(getCacheValue(userId));
        assertTrue(userRepository.findById(userId).isEmpty());
    }

    private Object getCacheValue(Long id) {
        Cache.ValueWrapper vw =
                Objects.requireNonNull(cacheManager.getCache("users")).get(id);
        return vw != null ? vw.get() : null;
    }


    @Test
    void test_patchUserActiveStatus_Success() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createTestUserDto())))
                .andExpect(status().isCreated())
                .andReturn();

        GetUserDto user = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                GetUserDto.class
        );

        Long id = user.getId();

        mockMvc.perform(patch("/api/users/{id}/active", id)
                        .param("active", "false"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void test_getAllUsers_Pagination() throws Exception {
        for (int i = 0; i < 5; i++) {
            CreateUserDto dto = createTestUserDto();
            dto.setEmail("user" + i + "@mail.com");
            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isCreated());
        }

        mockMvc.perform(get("/api/users")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(5));
    }


    @Test
    void test_getUserByEmail_Success() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createTestUserDto())))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/users/by-email")
                        .param("email", "ivan@mail.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ivan@mail.com"));
    }

    @Test
    void test_getUserByEmail_NotFound() throws Exception {
        mockMvc.perform(get("/api/users/by-email")
                        .param("email", "absent@mail.com"))
                .andExpect(status().isNotFound());
    }

    @Test
    void test_getUserBySurname_Pagination() throws Exception {
        CreateUserDto dto = createTestUserDto();
        dto.setSurname("Ivanov");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/users/by-surname")
                        .param("surname", "Iva")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void test_searchUsers_ByFilter() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createTestUserDto())))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/users/search")
                        .param("name", "Ivan")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void test_searchUsers_ByFilterSurname() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createTestUserDto())))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/users/search")
                        .param("surname", "Ivanov")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void test_searchUsers_ByFilterActiveStatus() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createTestUserDto())))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/users/search")
                        .param("active", "true")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void test_patchUserActiveStatus_EvictsCache() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createTestUserDto())))
                .andExpect(status().isCreated())
                .andReturn();

        GetUserDto user = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                GetUserDto.class
        );
        Long id = user.getId();


        mockMvc.perform(get("/api/users/{id}", id))
                .andExpect(status().isOk());

        assertNotNull(getCacheValue(id));

        // пач должен инвалиднуть
        mockMvc.perform(patch("/api/users/{id}/active", id)
                        .param("active", "false"))
                .andExpect(status().isNoContent());

        assertNull(getCacheValue(id));
    }

    @Test
    void test_patchUserActiveStatus_UpdatesDbAndEvictsCache() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createTestUserDto())))
                .andExpect(status().isCreated())
                .andReturn();

        GetUserDto user = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                GetUserDto.class
        );
        Long id = user.getId();

        mockMvc.perform(get("/api/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));

        assertNotNull(getCacheValue(id), "User should be cached before PATCH");

        mockMvc.perform(patch("/api/users/{id}/active", id)
                        .param("active", "false"))
                .andExpect(status().isNoContent());

        assertNull(getCacheValue(id), "Cache should be evicted after PATCH");

        MvcResult updatedResult = mockMvc.perform(get("/api/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false))
                .andReturn();

        GetUserDto updatedUser = objectMapper.readValue(
                updatedResult.getResponse().getContentAsString(),
                GetUserDto.class
        );

        assertTrue(userRepository.findById(id).isPresent(), "User should exist in DB");
        assertFalse(userRepository.findById(id).get().getActive(), "Active status in DB should be false");

        assertNotNull(getCacheValue(id), "Cache should be repopulated after GET");
        assertFalse(((GetUserDto) getCacheValue(id)).getActive(), "Cached value should reflect updated active status");
    }

}
