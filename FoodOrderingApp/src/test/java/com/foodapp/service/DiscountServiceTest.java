package com.foodapp.service;

import com.foodapp.model.Discount;
import com.foodapp.repository.DiscountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiscountServiceTest {

    @Mock
    private DiscountRepository discountRepository;

    @InjectMocks
    private DiscountService discountService;

    @Test
    void addDiscount_Success() {
        Discount discount = new Discount(1, "10% Off", 10.0, 100.0, LocalDateTime.now(), LocalDateTime.now().plusDays(1), true, false, LocalDateTime.now());
        when(discountRepository.addDiscount(discount)).thenReturn(discount);

        Discount result = discountService.addDiscount(discount);
        
        assertEquals(discount, result);
        verify(discountRepository).addDiscount(discount);
    }

    @Test
    void toggleActive_DiscountExists_Success() {
        Discount discount = new Discount(1, "10% Off", 10.0, 100.0, LocalDateTime.now(), LocalDateTime.now().plusDays(1), true, false, LocalDateTime.now());
        when(discountRepository.findById(1)).thenReturn(discount);
        when(discountRepository.toggleActive(1)).thenReturn(discount);

        Discount result = discountService.toggleActive(1);
        
        assertEquals(discount, result);
        verify(discountRepository).toggleActive(1);
    }

    @Test
    void toggleActive_DiscountNotFound_ThrowsException() {
        when(discountRepository.findById(1)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> discountService.toggleActive(1));
    }

    @Test
    void applyDiscount_NoActiveDiscounts_ReturnsZero() {
        when(discountRepository.getActiveDiscounts()).thenReturn(Collections.emptyList());

        double result = discountService.applyDiscount(100.0);

        assertEquals(0.0, result);
    }

    @Test
    void applyDiscount_WithActiveDiscounts_ReturnsMostProfitableForAdminRate() {
        Discount d1 = new Discount(1, "10% Off", 10.0, 100.0, LocalDateTime.now(), LocalDateTime.now().plusDays(1), true, false, LocalDateTime.now());
        Discount d2 = new Discount(2, "20% Off", 20.0, 200.0, LocalDateTime.now(), LocalDateTime.now().plusDays(1), true, false, LocalDateTime.now());

        when(discountRepository.getActiveDiscounts()).thenReturn(Arrays.asList(d1, d2));

        // For 250 order amount:
        // d1 applied on 250 -> discount amount is 250 * 0.10 = 25
        // d2 applied on 250 -> discount amount is 250 * 0.20 = 50
        // Most profitable for Admin means min discount amount 
        // -> d1 is chosen (10% rate)
        double result = discountService.applyDiscount(250.0);

        assertEquals(10.0, result);
    }

    @Test
    void deleteDiscount_DiscountExists_Success() {
        Discount discount = new Discount(1, "10% Off", 10.0, 100.0, LocalDateTime.now(), LocalDateTime.now().plusDays(1), true, false, LocalDateTime.now());
        when(discountRepository.findById(1)).thenReturn(discount);

        discountService.deleteDiscount(1);

        verify(discountRepository).deleteDiscount(1);
    }

    @Test
    void deleteDiscount_DiscountNotFound_ThrowsException() {
        when(discountRepository.findById(1)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> discountService.deleteDiscount(1));
    }
}
