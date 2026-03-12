CREATE TYPE user_role AS ENUM (
    'ADMIN',
    'CUSTOMER',
    'DELIVERY_PARTNER'
);

CREATE TYPE delivery_partner_status AS ENUM (
    'INACTIVE',
    'ACTIVE',
    'BUSY'
);

CREATE TYPE payment_mode AS ENUM (
    'UPI',
    'CASH'
);

CREATE TYPE order_status AS ENUM (
    'PENDING',
    'QUEUED',
    'OUT_FOR_DELIVERY',
    'DELIVERED',
    'CANCELLED'
);

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role user_role NOT NULL,
    is_deleted BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL,

    CONSTRAINT chk_name CHECK (name ~ '^[A-Za-z ]{2,50}$'),

    CONSTRAINT chk_email CHECK (email ~ '^[A-Za-z0-9+_.-]+@[A-Za-z0-9-]+\\.[A-Za-z]{2,}$'),

    CONSTRAINT chk_password
        CHECK (
            LENGTH(password) >= 8
            AND password ~ '[A-Z]'
            AND password ~ '[a-z]'
            AND password ~ '[0-9]'
        )
);

CREATE TABLE customer (
    id INT PRIMARY KEY,
    phone VARCHAR(15) NOT NULL,
    address TEXT NOT NULL,
    CONSTRAINT chk_customer_phone CHECK (phone ~ '^[6-9][0-9]{9}$'),
    CONSTRAINT fk_customer_user FOREIGN KEY (id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE admin (
    id INT PRIMARY KEY,
    CONSTRAINT fk_admin_user FOREIGN KEY (id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE delivery_partner (
    id INT PRIMARY KEY,
    phone VARCHAR(15) NOT NULL,
    status delivery_partner_status DEFAULT 'INACTIVE',
    CONSTRAINT fk_delivery_user FOREIGN KEY (id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    parent_category_id INT,
    is_active BOOLEAN DEFAULT TRUE,
    is_deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_parent_category FOREIGN KEY (parent_category_id) REFERENCES categories(id) ON DELETE CASCADE
);

CREATE TABLE food_items (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    category_id INT,
    is_active BOOLEAN DEFAULT TRUE,
    is_deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_fooditem_category FOREIGN KEY (category_id) REFERENCES categories(id)
);

CREATE TABLE cart (
    id BIGSERIAL PRIMARY KEY,
    customer_id INT UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cart_customer FOREIGN KEY (customer_id) REFERENCES customer(id) ON DELETE CASCADE
);

CREATE TABLE cart_items (
    id BIGSERIAL PRIMARY KEY,
    cart_id INT NOT NULL,
    food_item_id INT NOT NULL,
    quantity INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_quantity CHECK (quantity > 0),
    CONSTRAINT uq_cart_food UNIQUE(cart_id, food_item_id),
    CONSTRAINT fk_cartitem_cart FOREIGN KEY (cart_id) REFERENCES cart(id) ON DELETE CASCADE,
    CONSTRAINT fk_cartitem_food FOREIGN KEY (food_item_id) REFERENCES food_items(id)
);

CREATE TABLE payment (
    id BIGSERIAL PRIMARY KEY,
    mode payment_mode NOT NULL,
    customer_id INT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payment_customer FOREIGN KEY (customer_id) REFERENCES customer(id)
);

CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    customer_id INT NOT NULL,
    delivery_partner_id INT,
    total_amount DECIMAL(10,2) NOT NULL,
    discount_rate DECIMAL(5,2) DEFAULT 0,
    final_amount DECIMAL(10,2) NOT NULL,
    address TEXT NOT NULL,
    status order_status DEFAULT 'PENDING',
    payment_id INT,
    is_deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_order_customer FOREIGN KEY (customer_id) REFERENCES customer(id),
    CONSTRAINT fk_order_delivery_partner FOREIGN KEY (delivery_partner_id) REFERENCES delivery_partner(id),
    CONSTRAINT fk_order_payment FOREIGN KEY (payment_id) REFERENCES payment(id)
);

CREATE TABLE order_items (
     id BIGSERIAL PRIMARY KEY,
     order_id INT NOT NULL,
     food_item_id INT,
     food_item_name VARCHAR(150) NOT NULL,
     price DECIMAL(10,2) NOT NULL,
     quantity INT NOT NULL,
     subtotal DECIMAL(10,2) NOT NULL,
     is_deleted BOOLEAN DEFAULT FALSE,
     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
     CONSTRAINT fk_orderitem_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

CREATE TABLE order_status_history (
      id BIGSERIAL PRIMARY KEY,
      order_id INT NOT NULL,
      status order_status NOT NULL,
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_order_status_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

INSERT INTO categories (name) VALUES ('MENU');