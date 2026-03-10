CREATE TABLE accounts (
      account_id INT PRIMARY KEY,
      account_holder VARCHAR(100) NOT NULL,
      balance DECIMAL(10,2) NOT NULL,
      CONSTRAINT chk_balance CHECK (balance > 100)
);

INSERT INTO accounts VALUES (1, 'Rahul', 1000);
INSERT INTO accounts VALUES (2, 'Amit', 500);
