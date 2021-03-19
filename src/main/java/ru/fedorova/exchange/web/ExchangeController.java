package ru.fedorova.exchange.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.fedorova.exchange.db.CompanyDatabase;
import ru.fedorova.exchange.db.entities.Company;

import java.util.ArrayList;
import java.util.List;

import static ru.fedorova.exchange.web.ShareholderController.getStockRate;

@RestController
public class ExchangeController {

    @Autowired
    private CompanyDatabase companies;

    @PostMapping("/exchange/add_new_company/")
    public String addNewCompany(@RequestParam(value = "id") int id, @RequestParam(value = "name") String name,
                                @RequestParam(value = "stock_balance") int stockBalance) {
        if (companies.existsById(id)) {
            return "Company with the save ID already exists.\n";
        }
        companies.save(new Company(id, name, stockBalance));
        return "Ok\n";
    }

    @GetMapping("/exchange/report_company/{id}")
    public StockInfo getCompanyReport(@PathVariable int id) {
        if (companies.findById(id).isEmpty()) {
            return new StockInfo(null, null, null);
        }
        Company company = companies.findById(id).get();
        return new StockInfo(company.getName(), getStockRate(id), company.getStockBalance());
    }

    @GetMapping("/exchange/report_stocks")
    public List<StockInfo> getStocksReport() {
        Iterable<Company> allCompanies = companies.findAll();
        List<StockInfo> stocks = new ArrayList<>();
        for (Company company : allCompanies) {
            stocks.add(new StockInfo(company.getName(), getStockRate(company.getId()), company.getStockBalance()));
        }
        return stocks;
    }


    public static class StockInfo {
        private final String companyName;
        private final Integer priceForOneShare;
        private final Integer stockBalance;

        public StockInfo(String companyName, Integer priceForOneShare, Integer stockBalance) {
            this.companyName = companyName;
            this.priceForOneShare = priceForOneShare;
            this.stockBalance = stockBalance;
        }

        public Integer getStockBalance() {
            return stockBalance;
        }

        public Integer getPriceForOneShare() {
            return priceForOneShare;
        }

        public String getCompanyName() {
            return companyName;
        }
    }
}
