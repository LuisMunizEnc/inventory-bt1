package com.inventory.products.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.products.exception.CategoryAlreadyExistsException;
import com.inventory.products.exception.CategoryNotFoundException;
import com.inventory.products.model.Category;
import com.inventory.products.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryController.class)
public class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryServiceImpl categoryService;

    @InjectMocks
    private CategoryController categoryController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void givenCategory_whenCreateCategory_thenReturnsCreatedCategory() throws Exception {
        // given
        Category categoryToCreate = Category.builder().categoryName("Electronics").build();
        Category createdCategory = Category.builder().categoryName("Electronics").build();
        when(categoryService.createCategory(categoryToCreate)).thenReturn(createdCategory);

        // when
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryToCreate)))
                .andExpect(status().isCreated())
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED.value());
        Category responseCategory = objectMapper.readValue(response.getContentAsString(), Category.class);
        assertNotNull(responseCategory);
        assertEquals("Electronics", responseCategory.getCategoryName());
        verify(categoryService).createCategory(categoryToCreate);
    }

    @Test
    public void givenNoCategories_whenGetAllCategories_thenReturnEmptyList() throws Exception {
        // given
        List<Category> emptyList = new ArrayList<>();
        when(categoryService.getAllCategories()).thenReturn(emptyList);

        // when
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.get("/categories"))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        List<Category> responseList = objectMapper.readValue(response.getContentAsString(), List.class);
        assertNotNull(responseList);
        assertEquals(0, responseList.size());
        verify(categoryService).getAllCategories();
    }

    @Test
    public void givenSomeCategories_whenGetAllCategories_thenReturnListOfCategories() throws Exception {
        // given
        List<Category> categoryList = new ArrayList<>();
        categoryList.add(Category.builder().categoryName("Electronics").build());
        categoryList.add(Category.builder().categoryName("Food").build());
        when(categoryService.getAllCategories()).thenReturn(categoryList);

        // when
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.get("/categories"))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        List<Category> responseList = objectMapper.readValue(response.getContentAsString(), List.class);
        assertNotNull(responseList);
        assertEquals(2, responseList.size());
        List<Category> actualCategories = objectMapper.readValue(response.getContentAsString(),
                new com.fasterxml.jackson.core.type.TypeReference<List<Category>>(){});
        assertEquals("Electronics", actualCategories.get(0).getCategoryName());
        assertEquals("Food", actualCategories.get(1).getCategoryName());
        verify(categoryService).getAllCategories();
    }

    @Test
    public void givenExistingCategoryName_whenGetCategoryByName_thenReturnCategory() throws Exception {
        // given
        String categoryName = "Electronics";
        Category category = Category.builder().categoryName(categoryName).build();
        when(categoryService.getCategoryByName(categoryName)).thenReturn(category);

        // when
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.get("/categories/" + categoryName))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        Category responseCategory = objectMapper.readValue(response.getContentAsString(), Category.class);
        assertNotNull(responseCategory);
        assertEquals(categoryName, responseCategory.getCategoryName());
        verify(categoryService).getCategoryByName(categoryName);
    }

    @Test
    public void givenNonExistingCategoryName_whenGetCategoryByName_thenReturnNotFound() throws Exception {
        // given
        String categoryName = "NonExistent";
        when(categoryService.getCategoryByName(categoryName)).thenThrow(new CategoryNotFoundException("Category not found"));

        // when
        mockMvc.perform(MockMvcRequestBuilders.get("/categories/" + categoryName))
                .andExpect(status().isNotFound());

        // then
        verify(categoryService).getCategoryByName(categoryName);
    }

    @Test
    public void givenCategoryAlreadyExistsException_whenCreateCategory_thenHandleException() throws Exception {
        // given
        Category categoryToCreate = Category.builder().categoryName("ExistingCategory").build();
        when(categoryService.createCategory(categoryToCreate)).thenThrow(new CategoryAlreadyExistsException("Category already exists"));

        // when
        // then
        mockMvc.perform(MockMvcRequestBuilders.post("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryToCreate)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void givenIllegalArgumentException_whenCreateCategory_thenHandleException() throws Exception {
        // given
        Category invalidCategory = Category.builder().categoryName("").build();
        when(categoryService.createCategory(invalidCategory)).thenThrow(new IllegalArgumentException("Invalid argument"));

        // when
        // then
        mockMvc.perform(MockMvcRequestBuilders.post("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCategory)))
                .andExpect(status().isBadRequest());
    }
}