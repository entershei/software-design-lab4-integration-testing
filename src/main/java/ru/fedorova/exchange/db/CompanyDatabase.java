package ru.fedorova.exchange.db;

import org.springframework.data.repository.CrudRepository;
import ru.fedorova.exchange.db.entities.Company;

public interface CompanyDatabase extends CrudRepository<Company, Integer> {
}
