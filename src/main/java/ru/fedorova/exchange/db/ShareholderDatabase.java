package ru.fedorova.exchange.db;

import org.springframework.data.repository.CrudRepository;
import ru.fedorova.exchange.db.entities.Shareholder;

public interface ShareholderDatabase extends CrudRepository<Shareholder, Integer> {
}
