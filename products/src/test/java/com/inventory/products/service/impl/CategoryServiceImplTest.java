package com.inventory.products.service.impl;

import com.inventory.products.exception.EntityAlreadyExistsException;
import com.inventory.products.exception.EntityInvalidArguments;
import com.inventory.products.exception.EntityNotFoundException;
import com.inventory.products.model.Category;
import com.inventory.products.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    public void givenCategory_whenCategoryIsCreated_thenCreateCategory(){
        // given
        Category categoryToCreate = Category.builder().categoryName("TestCategory").build();
        when(categoryRepository.existsByCategoryName(categoryToCreate.getCategoryName())).thenReturn(false);
        when(categoryRepository.save(categoryToCreate)).thenReturn(categoryToCreate);

        // when
        Category createdCategory = categoryService.createCategory(categoryToCreate);

        // then
        assertNotNull(createdCategory);
        assertEquals("TestCategory", createdCategory.getCategoryName());
        verify(categoryRepository).save(categoryToCreate);
    }

    @Test
    public void givenNullReference_whenCreateCategory_thenThrowIllegalArgumentException() {
        // given
        Category category = null;

        // when
        // then
        assertThatThrownBy(() -> categoryService.createCategory(category))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void givenCategoryWithNullName_whenCreateCategory_thenThrowCategoryInvalidArgumentsException(){
        Category category = Category.builder()
                .categoryName(null)
                .build();

        //when
        //then
        assertThatThrownBy(() -> categoryService.createCategory(category))
                .isInstanceOf(EntityInvalidArguments.class);
    }

    @Test
    public void givenCategoryWithEmptyName_whenCreateCategory_thenThrowCategoryInvalidArgumentsException(){
        // given
        Category category = Category.builder()
                .categoryName("")
                .build();

        //when
        //then
        assertThatThrownBy(() -> categoryService.createCategory(category))
                .isInstanceOf(EntityInvalidArguments.class);
    }

    @Test
    public void givenCategoryThatExists_whenCreateCategory_thenThrowCategoryAlreadyExistsException(){
        // given
        Category category = Category.builder()
                .categoryName("Food")
                .build();

        when(categoryRepository.existsByCategoryName("Food")).thenReturn(true);

        // when
        // then
        assertThatThrownBy(() -> categoryService.createCategory(category))
                .isInstanceOf(EntityAlreadyExistsException.class);
    }

    @Test
    public void givenNoCategories_whenGetAllCategories_thenReturnEmptyArray(){
        // given
        when(categoryService.getAllCategories()).thenReturn(new ArrayList<>());

        // when
        List<Category> categoryList = categoryService.getAllCategories();

        //then
        assertNotNull(categoryList);
        assertTrue(categoryList.isEmpty());
        verify(categoryRepository).findAll();
    }

    @Test
    void givenSomeCategories_whenGetAllCategories_thenReturnCategories() {
        // given
        List<Category> expectedCategories = new ArrayList<>();
        expectedCategories.add(Category.builder().categoryName("Electronics").build());
        expectedCategories.add(Category.builder().categoryName("Food").build());
        when(categoryRepository.findAll()).thenReturn(expectedCategories);

        // when
        List<Category> categories = categoryService.getAllCategories();

        // then
        assertNotNull(categories);
        assertEquals(2, categories.size());
        assertEquals("Electronics", categories.get(0).getCategoryName());
        assertEquals("Food", categories.get(1).getCategoryName());
        verify(categoryRepository, times(1)).findAll();
    }

    @Test
    void givenCategories_whenFindByName_thenReturnCategory() {
        // given
        String categoryName = "Electronics";
        Category expectedCategory = Category.builder().categoryName(categoryName).build();
        when(categoryRepository.findByCategoryName(categoryName)).thenReturn(Optional.ofNullable(expectedCategory));

        // when
        Category actualCategory = categoryService.getCategoryByName(categoryName);

        // then
        assertNotNull(actualCategory);
        assertEquals(categoryName, actualCategory.getCategoryName());
        verify(categoryRepository, times(1)).findByCategoryName(categoryName);
    }

    @Test
    void testGetCategoryByName_CategoryDoesNotExist() {
        // given
        String categoryName = "NonExistent";
        when(categoryRepository.findByCategoryName(categoryName)).thenReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> categoryService.getCategoryByName(categoryName))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
