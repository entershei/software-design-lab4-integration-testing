package ru.fedorova.exchange.db.entities;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="companies")
public class Company {
    @Id
    private Integer id;
    private String name;
    private Integer stockBalance = 0;

    public Company() {}

    public Company(Integer id, String name, Integer stockBalance) {
        this.id = id;
        this.name = name;
        this.stockBalance = stockBalance;
    }

    public String getName() {
        return name;
    }

    public Integer getStockBalance() {
        return stockBalance;
    }

    public void addShares(int quantity) {
        stockBalance += quantity;
    }

    public void subtractShares(int quantity) {
        stockBalance -= quantity;
    }

    public Integer getId() {
        return id;
    }
}
