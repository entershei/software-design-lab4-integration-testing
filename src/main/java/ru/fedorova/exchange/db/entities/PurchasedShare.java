package ru.fedorova.exchange.db.entities;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "purchased_shares")
@IdClass(PurchasedShare.PurchasedShareId.class)
public class PurchasedShare {
    @Id
    private Integer ownerId;
    @Id
    private Integer companyId;
    private Integer quantity = 0;

    public PurchasedShare() {
    }

    public PurchasedShare(Integer ownerId, Integer companyId, Integer quantity) {
        this.ownerId = ownerId;
        this.companyId = companyId;
        this.quantity = quantity;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void addQuantity(Integer toAdd) {
        quantity += toAdd;
    }

    public void subtractQuantity(Integer toSubtract) {
        quantity -= toSubtract;
    }

    public static class PurchasedShareId implements Serializable {
        protected Integer ownerId;
        protected Integer companyId;

        public PurchasedShareId() {
        }

        public PurchasedShareId(Integer ownerId, Integer companyId) {
            this.ownerId = ownerId;
            this.companyId = companyId;
        }
    }
}