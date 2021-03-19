DROP TABLE IF EXISTS shareholders;
DROP TABLE IF EXISTS purchased_shares;
DROP TABLE IF EXISTS companies;

CREATE TABLE shareholders
(
    id    INT PRIMARY KEY,
    name  VARCHAR(200) NOT NULL,
    money INT          NOT NULL,
    CHECK more_than_zero_money(money >= 0)
);

CREATE TABLE companies
(
    id    INT PRIMARY KEY,
    name  VARCHAR(200) NOT NULL,
    stock INT          NOT NULL,
    CHECK more_than_zero_stock(stock >= 0)
);

CREATE TABLE purchased_shares
(
    owner_id   INT NOT NULL,
    company_id INT NOT NULL,
    quantity   INT NOT NULL,
    UNIQUE KEY purchased_shares_key (owner_id, company_id),
    CHECK more_than_zero_quantity(quantity >= 0),
    FOREIGN KEY (owner_id) references shareholders (id),
    FOREIGN KEY (company_id) references companies (id)
);
