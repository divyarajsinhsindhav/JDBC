# 🧪 Unit Testing with JUnit 5 & Mockito

> A comprehensive guide to writing unit tests for the **FoodOrderingApp** — covering theory, annotations, and real code examples.

---

## 📚 Table of Contents

1. [What is Unit Testing?](#1-what-is-unit-testing)
2. [Why Unit Testing?](#2-why-unit-testing)
3. [What is JUnit?](#3-what-is-junit)
4. [What is Mockito?](#4-what-is-mockito)
5. [Project Test Setup (Maven Dependencies)](#5-project-test-setup-maven-dependencies)
6. [JUnit 5 Annotations](#6-junit-5-annotations)
7. [Mockito Annotations](#7-mockito-annotations)
8. [Mockito Core Methods](#8-mockito-core-methods)
9. [JUnit 5 Assertion Methods](#9-junit-5-assertion-methods)
10. [Test Naming Convention](#10-test-naming-convention)
11. [Real Examples from This Project](#11-real-examples-from-this-project)
12. [AAA Pattern — The Golden Rule of Unit Tests](#12-aaa-pattern--the-golden-rule-of-unit-tests)
13. [Quick Reference Cheat Sheet](#13-quick-reference-cheat-sheet)

---

## 1. What is Unit Testing?

A **unit test** is a test that validates a **single, isolated piece of code** — typically one method or one class — without depending on external systems like databases, file systems, or network calls.

| Concept | Meaning |
|---|---|
| **Unit** | The smallest testable part of code (a method or class) |
| **Isolation** | Dependencies are replaced with fake objects (mocks) |
| **Automation** | Tests run automatically with every build |
| **Repeatability** | Tests produce the same result every time |

### What is NOT a Unit Test?

| Type | Description |
|---|---|
| **Integration Test** | Tests multiple components working together (e.g., Service + Real DB) |
| **End-to-End Test** | Tests the entire application flow from UI to DB |
| **Manual Test** | A human clicks through the application manually |

---

## 2. Why Unit Testing?

```
Without tests         With tests
──────────────        ──────────────────────────────────────
Change code    →      Change code
   ↓                      ↓
Hope it works  →      Run tests → Pass ✅ OR Fail ❌ (instantly)
   ↓                      ↓
Deploy & pray  →      Fix issues early, deploy with confidence
```

**Key benefits:**
- 🐛 **Catch bugs early** — before they reach production
- 🔒 **Prevent regressions** — existing features don't break when you add new ones
- 📖 **Living documentation** — tests describe expected behaviour clearly
- ♻️ **Enables refactoring** — change implementation freely if tests pass
- ⚡ **Fast feedback** — run in milliseconds, no DB or server needed

---

## 3. What is JUnit?

**JUnit** is the most popular Java testing framework. This project uses **JUnit 5** (also called *JUnit Jupiter*).

| Version | Artifact ID | Notes |
|---|---|---|
| JUnit 4 | `junit` | Older, single jar |
| JUnit 5 | `junit-jupiter-api` + `junit-jupiter-engine` | Modern, modular, Java 8+ |

JUnit provides:
- **Annotations** to mark test methods (`@Test`, `@BeforeEach`, etc.)
- **Assertion methods** to verify expected results (`assertEquals`, `assertThrows`, etc.)
- A **test runner** that discovers and executes tests automatically

---

## 4. What is Mockito?

**Mockito** is a Java mocking framework that allows you to create **fake (mock) objects** that simulate real dependencies.

### Why Mock?

Consider `OrderService`. It depends on:
- `OrderRepository` → talks to PostgreSQL database
- `DeliveryPartnerService` → has complex business logic
- `DiscountService` → reads from database

In a unit test, you don't want to connect to a real database. Instead, you **mock** these dependencies so `OrderService` can be tested in complete isolation.

```
Real Application:          Unit Test:
─────────────────          ──────────────────────
OrderService               OrderService
    │                           │
    ├─ OrderRepository    →     ├─ Mock OrderRepository  (fake, controllable)
    ├─ DeliveryPartner    →     ├─ Mock DeliveryPartner  (fake, controllable)
    └─ DiscountService    →     └─ Mock DiscountService  (fake, controllable)
         ↓                                ↓
    Real Database              No database needed ✅
```

---

## 5. Project Test Setup (Maven Dependencies)

Located in `pom.xml`:

```xml
<!-- JUnit 5 API — contains annotations and assertion methods -->
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter-api</artifactId>
    <version>5.10.1</version>
    <scope>test</scope>
</dependency>

<!-- JUnit 5 Engine — discovers and runs the tests -->
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter-engine</artifactId>
    <version>5.10.1</version>
    <scope>test</scope>
</dependency>

<!-- Mockito Core — mocking framework -->
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-core</artifactId>
    <version>5.8.0</version>
    <scope>test</scope>
</dependency>

<!-- Mockito JUnit Jupiter — integrates Mockito with JUnit 5 -->
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-junit-jupiter</artifactId>
    <version>5.8.0</version>
    <scope>test</scope>
</dependency>
```

> **`scope: test`** means these jars are only used during testing and are NOT included in the production build.

---

## 6. JUnit 5 Annotations

### `@Test`

Marks a method as a test case. JUnit will automatically discover and run it.

```java
@Test
void addFoodItem_ValidFoodItem_Success() {
    menuService.addFoodItem(1, "Cake", 15.0);
    verify(menuRepository).addFoodItem(any(FoodItem.class));
}
```

- Method must be **`void`** and have **no parameters**
- Method should be **package-private** (no `public` needed in JUnit 5)
- Each `@Test` method is independent — JUnit creates a fresh class instance per test

---

### `@BeforeEach`

Runs **before every single test** method in the class. Used to set up shared test data or reset state.

```java
@BeforeEach
void setUp() {
    customer = new Customer(1, "Test User", "test@test.com", "password", "1234567890", "123 Test St");
    food = new FoodItem(10, "Burger", 50.0);
    item = new OrderItem(100, food, 2, 50.0);
}
```

Used in `OrderServiceTest` to avoid repeating object creation in every test.

| Lifecycle Annotation | When it runs |
|---|---|
| `@BeforeAll` | Once before **all** tests in the class (method must be `static`) |
| `@BeforeEach` | Before **each** test method |
| `@AfterEach` | After **each** test method |
| `@AfterAll` | Once after **all** tests in the class (method must be `static`) |

---

### `@ExtendWith(MockitoExtension.class)`

Tells JUnit 5 to use Mockito's extension. This:
- Automatically processes `@Mock` and `@InjectMocks` annotations
- Validates that all mocks are properly used (no unnecessary stubbing)
- Cleans up mocks between tests

```java
@ExtendWith(MockitoExtension.class)   // ← Enable Mockito in this test class
class MenuServiceTest {
    ...
}
```

> Without this, `@Mock` and `@InjectMocks` annotations are ignored.

---

### `@Disabled`

Skips a test temporarily. Useful when a test is known to be broken and you want to fix it later.

```java
@Disabled("Fix after DB schema refactor")
@Test
void someTest() { ... }
```

---

### `@DisplayName`

Gives a human-readable name to a test (shown in IDE and reports).

```java
@Test
@DisplayName("Should throw ItemNotFoundException when food item does not exist")
void deleteItem_ItemNotFound_ThrowsException() { ... }
```

---

## 7. Mockito Annotations

### `@Mock`

Creates a **mock object** — a fake implementation of an interface or class. All methods return default values (`null`, `0`, `false`, empty list) unless you explicitly configure them with `when(...)`.

```java
@Mock
private MenuRepository menuRepository;  // Fake — no real DB calls

@Mock
private CartService cartService;        // Fake — no real logic runs
```

**Usage in `MenuServiceTest`:**
```java
// Stub: when findFoodItemById is called with (100, true), return this fake food item
when(menuRepository.findFoodItemById(100, true)).thenReturn(foodItem);
```

---

### `@InjectMocks`

Creates the **real object under test** and automatically injects all `@Mock` fields into it via constructor, setter, or field injection.

```java
@InjectMocks
private MenuService menuService;
// Mockito will inject: menuRepository + cartService into menuService
```

```
@Mock MenuRepository     ──┐
@Mock CartService        ──┼──▶  @InjectMocks MenuService  (the class being tested)
```

> **Important:** There is only ever **one** `@InjectMocks` per test class — that's the class you're testing.

---

### `@Spy`

Creates a **partial mock** — a real object where you can override specific methods while keeping real behaviour for others. Not used directly in this project but useful to know.

```java
@Spy
private List<String> spyList = new ArrayList<>();
// Real list, but you can verify calls on it
```

---

### `@Captor`

Captures the exact argument passed to a mock method. Useful when you want to inspect the object that was passed.

```java
@Captor
private ArgumentCaptor<FoodItem> foodItemCaptor;

// Then in test:
verify(menuRepository).addFoodItem(foodItemCaptor.capture());
FoodItem captured = foodItemCaptor.getValue();
assertEquals("Cake", captured.getName());
```

---

## 8. Mockito Core Methods

### `when(...).thenReturn(...)`

Stubs a mock method — when the method is called with specific arguments, return a specific value.

```java
// When findById(1) is called, return the discount object
when(discountRepository.findById(1)).thenReturn(discount);

// When getFreeDeliveryPartner() is called, return a partner
when(deliveryPartnerService.getFreeDeliveryPartner()).thenReturn(partner);

// When getFreeDeliveryPartner() is called, return null (no free partner)
when(deliveryPartnerService.getFreeDeliveryPartner()).thenReturn(null);
```

---

### `when(...).thenThrow(...)`

Makes a mock method throw an exception.

```java
when(discountRepository.findById(1)).thenThrow(new RuntimeException("Not found"));
```

---

### `verify(...)`

Verifies that a mock method was **called** (interaction verification). Fails if the method was NOT called.

```java
// Verify deleteFoodItem(100) was called exactly once
verify(menuRepository).deleteFoodItem(100);

// Verify removeFoodItemFromAllCarts was called with 100
verify(cartService).removeFoodItemFromAllCarts(100);
```

---

### `verify(..., never())`

Verifies that a mock method was **never called**.

```java
// If item not found, deleteFoodItem should NEVER be called
verify(menuRepository, never()).deleteFoodItem(100);
verify(cartService, never()).removeFoodItemFromAllCarts(100);
```

---

### `verify(..., times(n))`

Verifies a method was called exactly `n` times.

```java
verify(orderRepository, times(2)).addOrder(any());
verify(menuRepository, times(0)).deleteFoodItem(any()); // same as never()
```

---

### `any()` / `any(ClassName.class)`

An **argument matcher** — matches any argument of that type. Useful when the exact value doesn't matter.

```java
// Match any MenuCategory object
verify(menuRepository).addCategory(any(MenuCategory.class));

// Match any FoodItem object
verify(menuRepository).addFoodItem(any(FoodItem.class));

// Match any argument regardless of type
verify(deliveryPartnerService, never()).changeDeliveryPartnerStatus(any(), any());
```

---

### `doNothing().when(...)`

For `void` methods, stubs them to do nothing (this is the default for mocks, but explicit when needed).

```java
doNothing().when(menuRepository).deleteFoodItem(100);
```

---

## 9. JUnit 5 Assertion Methods

All from `org.junit.jupiter.api.Assertions.*`

### `assertEquals(expected, actual)`

Checks that two values are equal.

```java
assertEquals(discount, result);             // objects must be equal
assertEquals(OrderStatus.QUEUED, placedOrder.getOrderStatus());
assertEquals(0.0, result);                  // double comparison
assertEquals(10.0, result);
```

---

### `assertNotNull(object)`

Checks that the object is **not null**.

```java
assertNotNull(placedOrder);
```

---

### `assertNull(object)`

Checks that the object **is null**.

```java
assertNull(placedOrder.getDeliveryPartner());
```

---

### `assertThrows(ExceptionClass, executable)`

Checks that a specific exception is thrown when the code runs. Returns the exception for further inspection.

```java
// Throws ItemNotFoundException when item doesn't exist
assertThrows(ItemNotFoundException.class, () -> menuService.deleteItem(100));

// Throws EmptyCartException for empty cart
assertThrows(EmptyCartException.class, () ->
    orderService.placeOrder(customer, PaymentMode.CASH, Collections.emptyList()));

// Throws IllegalArgumentException for null customer
assertThrows(IllegalArgumentException.class, () ->
    orderService.placeOrder(null, PaymentMode.CASH, Collections.singletonList(item)));

// Throws IllegalStateException for wrong order status
assertThrows(IllegalStateException.class, () ->
    orderService.deliverOrder(order, partner));
```

---

### `assertDoesNotThrow(executable)`

Checks that **no exception** is thrown.

```java
assertDoesNotThrow(() -> menuService.deleteItem(100));
```

---

### `assertTrue(condition)` / `assertFalse(condition)`

```java
assertTrue("New Category Name".equals(category.getCategory()));
assertFalse(list.isEmpty());
```

---

## 10. Test Naming Convention

This project follows the **`methodName_Condition_ExpectedResult`** naming convention:

```
deleteItem_ItemExists_Success
   │             │          │
   │             │          └── What should happen (Success / ThrowsException)
   │             └──────────── The condition / scenario
   └────────────────────────── The method being tested
```

| Test Name | Method | Condition | Expected Result |
|---|---|---|---|
| `addCategory_ValidCategory_Success` | `addCategory` | Valid input | Succeeds |
| `deleteItem_ItemNotFound_ThrowsException` | `deleteItem` | Item missing | Exception thrown |
| `placeOrder_EmptyCart_ThrowsException` | `placeOrder` | Empty cart | Exception |
| `placeOrder_NoFreePartner_QueuedOrder` | `placeOrder` | No partner | Order queued |
| `deliverOrder_ValidOrder_Success` | `deliverOrder` | Valid state | Order delivered |

---

## 11. Real Examples from This Project

### Example 1 — `MenuServiceTest`: Verify interaction + exception path

```java
@ExtendWith(MockitoExtension.class)
class MenuServiceTest {

    @Mock
    private MenuRepository menuRepository;  // Fake repository — no DB

    @Mock
    private CartService cartService;        // Fake service

    @InjectMocks
    private MenuService menuService;        // The class we are testing (REAL)

    @Test
    void deleteItem_ItemExists_Success() {
        // ARRANGE
        FoodItem foodItem = new FoodItem(100, "Burger", 50.0);
        when(menuRepository.findFoodItemById(100, true)).thenReturn(foodItem);

        // ACT + ASSERT (no exception)
        assertDoesNotThrow(() -> menuService.deleteItem(100));

        // VERIFY interactions
        verify(menuRepository).deleteFoodItem(100);
        verify(cartService).removeFoodItemFromAllCarts(100);
    }

    @Test
    void deleteItem_ItemNotFound_ThrowsException() {
        // ARRANGE — simulate item not found
        when(menuRepository.findFoodItemById(100, true)).thenReturn(null);

        // ACT + ASSERT — expect exception
        assertThrows(ItemNotFoundException.class, () -> menuService.deleteItem(100));

        // VERIFY — these should NEVER be called if item was not found
        verify(menuRepository, never()).deleteFoodItem(100);
        verify(cartService, never()).removeFoodItemFromAllCarts(100);
    }
}
```

---

### Example 2 — `OrderServiceTest`: `@BeforeEach` + complex stubbing

```java
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private DeliveryPartnerService deliveryPartnerService;
    @Mock private OrderRepository orderRepository;
    @Mock private CartRepository cartRepository;
    @Mock private DiscountService discountService;

    @InjectMocks
    private OrderService orderService;

    // Shared test data — set up before EACH test
    private Customer customer;
    private OrderItem item;

    @BeforeEach
    void setUp() {
        customer = new Customer(1, "Test User", "test@test.com", "password", "1234567890", "123 Test St");
        FoodItem food = new FoodItem(10, "Burger", 50.0);
        item = new OrderItem(100, food, 2, 50.0);
    }

    @Test
    void placeOrder_PartnerAssigned_Success() {
        // ARRANGE
        List<OrderItem> cart = Collections.singletonList(item);
        when(discountService.applyDiscount(100.0)).thenReturn(10.0);

        DeliveryPartner partner = new DeliveryPartner(2, "Partner 1", "partner@test.com", "pass", "123");
        when(deliveryPartnerService.getFreeDeliveryPartner()).thenReturn(partner);

        // ACT
        Order placedOrder = orderService.placeOrder(customer, PaymentMode.CASH, cart);

        // ASSERT
        assertNotNull(placedOrder);
        assertEquals(OrderStatus.OUT_FOR_DELIVERY, placedOrder.getOrderStatus());
        assertEquals(partner, placedOrder.getDeliveryPartner());

        // VERIFY
        verify(deliveryPartnerService).changeDeliveryPartnerStatus(partner, DeliveryPartnerStatus.BUSY);
        verify(orderRepository).addOrder(placedOrder);
    }
}
```

---

### Example 3 — `DiscountServiceTest`: Return value assertions

```java
@Test
void applyDiscount_NoActiveDiscounts_ReturnsZero() {
    // ARRANGE — empty list returned
    when(discountRepository.getActiveDiscounts()).thenReturn(Collections.emptyList());

    // ACT
    double result = discountService.applyDiscount(100.0);

    // ASSERT — no discounts means 0 discount amount
    assertEquals(0.0, result);
}

@Test
void addDiscount_Success() {
    Discount discount = new Discount(...);
    when(discountRepository.addDiscount(discount)).thenReturn(discount);

    Discount result = discountService.addDiscount(discount);

    assertEquals(discount, result);
    verify(discountRepository).addDiscount(discount);
}
```

---

## 12. AAA Pattern — The Golden Rule of Unit Tests

Every well-written test follows the **AAA (Arrange, Act, Assert)** pattern:

```
┌─────────────────────────────────────────────────────────┐
│  ARRANGE  │  Set up test data, configure mocks (when)   │
├─────────────────────────────────────────────────────────┤
│  ACT      │  Call the method under test                 │
├─────────────────────────────────────────────────────────┤
│  ASSERT   │  Verify result with assertEquals / verify   │
└─────────────────────────────────────────────────────────┘
```

**Example:**
```java
@Test
void deleteDiscount_DiscountExists_Success() {
    // ARRANGE
    Discount discount = new Discount(1, "10% Off", ...);
    when(discountRepository.findById(1)).thenReturn(discount);

    // ACT
    discountService.deleteDiscount(1);

    // ASSERT
    verify(discountRepository).deleteDiscount(1);
}
```

---

## 13. Quick Reference Cheat Sheet

### JUnit 5 Annotations

| Annotation | Purpose |
|---|---|
| `@Test` | Marks a method as a test |
| `@BeforeEach` | Runs before every test method |
| `@AfterEach` | Runs after every test method |
| `@BeforeAll` | Runs once before all tests (static) |
| `@AfterAll` | Runs once after all tests (static) |
| `@ExtendWith(...)` | Register an extension (e.g., Mockito) |
| `@Disabled` | Skip a test temporarily |
| `@DisplayName` | Custom test name in reports |

### Mockito Annotations

| Annotation | Purpose |
|---|---|
| `@Mock` | Creates a fake (mock) object |
| `@InjectMocks` | Creates the real object and injects mocks |
| `@Spy` | Creates a real object with mock capabilities |
| `@Captor` | Captures arguments passed to mock methods |

### Mockito Methods

| Method | Purpose |
|---|---|
| `when(mock.method()).thenReturn(value)` | Stub — make mock return a value |
| `when(mock.method()).thenThrow(ex)` | Stub — make mock throw an exception |
| `verify(mock).method(args)` | Verify method was called |
| `verify(mock, never()).method(args)` | Verify method was NEVER called |
| `verify(mock, times(n)).method(args)` | Verify method was called n times |
| `any()` / `any(Class.class)` | Argument matcher — match any value |
| `doNothing().when(mock).voidMethod()` | Stub a void method |

### JUnit 5 Assertions

| Assertion | Purpose |
|---|---|
| `assertEquals(expected, actual)` | Values are equal |
| `assertNotNull(obj)` | Object is not null |
| `assertNull(obj)` | Object is null |
| `assertTrue(condition)` | Condition is true |
| `assertFalse(condition)` | Condition is false |
| `assertThrows(Exception.class, () -> ...)` | Exception is thrown |
| `assertDoesNotThrow(() -> ...)` | No exception is thrown |

---

> 📁 **Test files in this project:**
> - `src/test/java/com/foodapp/service/MenuServiceTest.java`
> - `src/test/java/com/foodapp/service/OrderServiceTest.java`
> - `src/test/java/com/foodapp/service/DiscountServiceTest.java`

> ▶️ **Run all tests:**
> ```bash
> mvn test
> ```
