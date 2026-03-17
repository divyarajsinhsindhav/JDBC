package com.foodapp.service;

import com.foodapp.exception.ItemNotFoundException;
import com.foodapp.model.FoodItem;
import com.foodapp.model.MenuCategory;
import com.foodapp.repository.MenuRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MenuServiceTest {

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private CartService cartService;

    @InjectMocks
    private MenuService menuService;

    @Test
    void addCategory_ValidCategory_Success() {
        menuService.addCategory(1, "Desserts");
        verify(menuRepository).addCategory(any(MenuCategory.class));
    }

    @Test
    void addFoodItem_ValidFoodItem_Success() {
        menuService.addFoodItem(1, "Cake", 15.0);
        verify(menuRepository).addFoodItem(any(FoodItem.class));
    }

    @Test
    void deleteItem_ItemExists_Success() {
        FoodItem foodItem = new FoodItem(100, "Burger", 50.0);
        when(menuRepository.findFoodItemById(100, true)).thenReturn(foodItem);

        assertDoesNotThrow(() -> menuService.deleteItem(100));

        verify(menuRepository).deleteFoodItem(100);
        verify(cartService).removeFoodItemFromAllCarts(100);
    }

    @Test
    void deleteItem_ItemNotFound_ThrowsException() {
        when(menuRepository.findFoodItemById(100, true)).thenReturn(null);

        assertThrows(ItemNotFoundException.class, () -> menuService.deleteItem(100));
        verify(menuRepository, never()).deleteFoodItem(100);
        verify(cartService, never()).removeFoodItemFromAllCarts(100);
    }

    @Test
    void updateCategory_CategoryExists_Success() {
        MenuCategory category = new MenuCategory(1, "Old Name");
        when(menuRepository.findCategoryById(1)).thenReturn(category);

        menuService.updateCategory(1, "New Category Name");

        assert("New Category Name".equals(category.getCategory()));
    }

    @Test
    void updateFoodItem_Success() {
        FoodItem foodItem = new FoodItem(100, "Burger", 50.0);
        menuService.updateFoodItem(foodItem);
        verify(menuRepository).updateFoodItem(foodItem);
    }
}
