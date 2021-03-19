package ru.fedorova.exchange.db;

import org.springframework.data.repository.CrudRepository;
import ru.fedorova.exchange.db.entities.PurchasedShare;

import java.util.Optional;

public interface PurchasedShareDatabase extends CrudRepository<PurchasedShare, PurchasedShare.PurchasedShareId> {
    Iterable<PurchasedShare> findAllByOwnerId(Integer ownerId);

    Optional<PurchasedShare> findByOwnerIdAndCompanyId(Integer ownerId, Integer companyId);
}
