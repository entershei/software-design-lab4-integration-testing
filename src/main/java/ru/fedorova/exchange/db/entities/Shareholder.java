package ru.fedorova.exchange.db.entities;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "shareholders")
public class Shareholder {
    @Id
    private Integer id;
    private String name;
    private Integer money = 0;

    public Shareholder() {
    }

    public Shareholder(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public void addMoney(Integer money) {
        this.money += money;
    }

    public void subtractMoney(Integer money) {
        this.money -= money;
    }

    public String getName() {
        return name;
    }

    public Integer getMoney() {
        return money;
    }
}
