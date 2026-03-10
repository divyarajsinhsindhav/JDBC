package com.foodapp.controller;

import com.foodapp.exception.EmptyCartException;
import com.foodapp.exception.EmptyOrderException;
import com.foodapp.exception.ItemNotFoundException;
import com.foodapp.model.*;
import com.foodapp.service.*;
import com.foodapp.utils.IdGenerator;
import com.foodapp.utils.InputValidation;
import com.foodapp.utils.SessionManager;

import java.util.List;
import java.util.Scanner;

public class CustomerController {

    private static final int ADD_ITEM = 1;
    private static final int SHOW_CART = 2;
    private static final int REMOVE_ITEM = 3;
    private static final int UPDATE_CART = 4;
    private static final int PLACE_ORDER = 5;
    private static final int VIEW_ORDERS = 6;
    private static final int LOGOUT = 7;
    private static final int BACK = 8;

    private CustomerService customerService;
    private MenuService menuService;
    private OrderService orderService;
    private CartService cartService;
    private SessionManager sessionManager;
    private DeliveryPartnerService deliveryPartnerService;
    private DiscountService discountService;

    private Scanner scanner = new Scanner(System.in);

    public CustomerController(CustomerService customerService, MenuService menuService, OrderService orderService,
            CartService cartService, SessionManager sessionManager, DeliveryPartnerService deliveryPartnerService,
            DiscountService discountService) {

        this.customerService = customerService;
        this.menuService = menuService;
        this.orderService = orderService;
        this.cartService = cartService;
        this.sessionManager = sessionManager;
        this.deliveryPartnerService = deliveryPartnerService;
        this.discountService = discountService;
    }

    private void logout() {

        sessionManager.logout();

        System.out.println("Logged out successfully.");
    }

    public void displayOption() {
        try {
            while (sessionManager.isLoggedIn()) {

                printOptionMenu();

                int choice = InputValidation.readIntInRange(scanner, "Enter your choice: ", ADD_ITEM, LOGOUT);

                handleChoice(choice);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void handleChoice(int choice) {
        try {
            switch (choice) {

                case ADD_ITEM -> addItemToCart();

                case SHOW_CART -> displayCart();

                case REMOVE_ITEM -> removeItemFromCart();

                case UPDATE_CART -> updateCart();

                case PLACE_ORDER -> placeOrder();

                case VIEW_ORDERS -> getAllOrders();

                case LOGOUT -> logout();

                default -> System.out.println("Invalid choice.");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void printOptionMenu() {

        System.out.println("\n===== CUSTOMER MENU =====");
        System.out.println("1. Add Item To Cart");
        System.out.println("2. Show Cart Items");
        System.out.println("3. Remove Item From Cart");
        System.out.println("4. Update Cart");
        System.out.println("5. Place Order");
        System.out.println("6. View Orders");
        System.out.println("7. Logout");
    }

    private void addItemToCart() {
        User customer = sessionManager.getCurrentCustomer();
        do {
            menuService.displayMenu();

            int foodId = InputValidation.readPositiveInt(scanner, "Enter Food Item ID: ");

            int quantity = InputValidation.readPositiveInt(scanner, "Enter Quantity: ");

            FoodItem foodItem = menuService.findFoodItem(foodId);

            if (foodItem == null) {
                System.out.println("Food item not found.");
                return;
            }

            OrderItem existingItem = cartService.getFoodItemExisted(customer.getId(), foodItem);

            if (existingItem != null) {
                cartService.updateOrderItemQuantityIfAlreadyExist(
                        customer.getId(),
                        existingItem,
                        quantity);
                continue;
            }

            int id = IdGenerator.getNextOrderItemID();

            double price = foodItem.getPrice() * quantity;

            OrderItem orderItem = new OrderItem(id, foodItem, quantity, price);

            cartService.addOrderItemToCart(customer.getId(), orderItem);

            System.out.println("Item added to cart.");
        } while (InputValidation.doUserWantToContinue(scanner,
                "\nDo you want to add another item to cart?"));

    }

    private void removeItemFromCart() {

        User customer = sessionManager.getCurrentCustomer();

        try {
            displayCart();
        } catch (Exception e) {
            throw new EmptyCartException("No item found in cart.");
        }

        int orderItemId = InputValidation.readPositiveInt(scanner, "Enter Order Item ID: ");

        OrderItem orderItem = cartService.getOrderItemFromCart(customer.getId(), orderItemId);

        if (orderItem == null) {
            throw new ItemNotFoundException("Order Item not found.");
        }

        cartService.removeOrderItemFromCart(customer.getId(), orderItem);

        System.out.println("Item removed from cart.");
    }

    private void updateCart() {

        User customer = sessionManager.getCurrentCustomer();
        displayCart();
        int orderItemId = InputValidation.readPositiveInt(scanner, "Enter Food Item ID: ");

        int quantity = InputValidation.readPositiveInt(scanner, "Enter New Quantity: ");

        OrderItem orderItem = cartService.getOrderItemFromCart(customer.getId(), orderItemId);

        if (orderItem == null) {
            System.out.println("Food item not found.");
            return;
        }

        orderItem.setQuantity(quantity);

        cartService.updateOrderItemQuantity(customer.getId(), orderItem);

        System.out.println("Cart updated successfully.");
    }

    private void getAllOrders() {

        User customer = sessionManager.getCurrentCustomer();

        List<Order> customerOrder = orderService.getOrdersByCustomer(customer);

        if (customerOrder == null || customerOrder.isEmpty()) {
            throw new EmptyOrderException("No orders found.");
        }

        System.out.println("\n==================== YOUR ORDERS ====================");
        System.out.printf("%-8s  %-12s  %-8s  %-18s  %-20s%n",
                "Order ID", "Amount", "Payment", "Status", "Delivery Partner");
        System.out.println("----------------------------------------------------------------------");

        customerOrder.forEach(order -> {
            String partner = (order.getDeliveryPartner() != null)
                    ? order.getDeliveryPartner().getName()
                    : "Not Assigned";
            System.out.printf("%-8d  %-12.2f  %-8s  %-18s  %-20s%n",
                    order.getId(),
                    order.getFinalAmount(),
                    order.getPaymentMode(),
                    order.getOrderStatus(),
                    partner);
        });

        System.out.println("======================================================");
    }

    private void displayCart() {

        User customer = sessionManager.getCurrentCustomer();
        List<OrderItem> cart = cartService.getCart(customer.getId());

        if (cart == null || cart.isEmpty()) {
            throw new EmptyCartException("Cart is empty.");
        }

        System.out.println("\n==================== YOUR CART ====================");

        System.out.printf("%-10s %-20s %-10s %-12s %-12s%n",
                "Item ID", "Item Name", "Qty", "Unit Price", "Subtotal");

        System.out.println("--------------------------------------------------------------");

        double totalAmount = 0.0;

        double subtotal = 0.0;
        for (OrderItem item : cart) {

            double unitPrice = item.getFoodItem().getPrice();
            int quantity = item.getQuantity();
            subtotal = unitPrice * quantity;

            totalAmount += subtotal;

            System.out.printf("%-10d %-20s %-10d %-12.2f %-12.2f%n",
                    item.getId(),
                    item.getFoodItem().getName(),
                    quantity,
                    unitPrice,
                    subtotal);
        }

        System.out.println("--------------------------------------------------------------");
        System.out.printf("%-45s %.2f%n", "Total Amount:", totalAmount);

        double discountRate = discountService.applyFlatDiscount(totalAmount);
        double discountAmount = (totalAmount * discountRate / 100);
        if (discountRate > 0) {
            double finalAmount = totalAmount - discountAmount;

            System.out.printf("%-45s -%.2f%n",
                    "Flat Discount:",
                    discountAmount);

            System.out.printf("%-45s %.2f%n",
                    "Final Amount to Pay:",
                    finalAmount);
        } else {
            System.out.printf("%-45s %.2f%n",
                    "Final Amount to Pay:",
                    totalAmount);
        }

        System.out.println("====================================================\n");
    }

    private void placeOrder() throws InterruptedException {

        User customer = sessionManager.getCurrentCustomer();

        List<OrderItem> cart = cartService.getCart(customer.getId());

        if (cart == null || cart.isEmpty()) {
            System.out.println("Cart is empty.");
            return;
        }

        if (!deliveryPartnerService.checkDeliveryPartnerAvailable()) {
            System.out.println("Sorry, no delivery partners are currently active. Please try again later.");
            return;
        }

        displayCart();

        boolean wantToPlaceOrder = InputValidation.doUserWantToContinue(scanner, "Want to place order?");
        if (!wantToPlaceOrder) {
            return;
        }

        PaymentMode mode = handlePayment();

        Order order;
        try {
            order = orderService.placeOrder(customer, mode, cart);
        } catch (EmptyCartException e) {
            System.out.println(e.getMessage());
            return;
        }

        System.out.println("\nOrder placed successfully! Order ID: " + order.getId());

        InvoicePrinter.printInvoice(order);

        cartService.clearCustomerCart(customer.getId());
    }

    private PaymentMode handlePayment() {

        System.out.println("\nSelect Payment Mode");
        System.out.println("1. UPI");
        System.out.println("2. CASH");

        int choice = InputValidation.readIntInRange(scanner, "Enter payment option: ", 1, 2);

        return switch (choice) {
            case 1 -> PaymentMode.UPI;
            case 2 -> PaymentMode.CASH;
            default -> throw new IllegalArgumentException("Invalid payment option");
        };
    }
}