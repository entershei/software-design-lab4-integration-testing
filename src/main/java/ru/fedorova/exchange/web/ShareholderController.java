package ru.fedorova.exchange.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.fedorova.exchange.db.CompanyDatabase;
import ru.fedorova.exchange.db.PurchasedShareDatabase;
import ru.fedorova.exchange.db.ShareholderDatabase;
import ru.fedorova.exchange.db.entities.Company;
import ru.fedorova.exchange.db.entities.PurchasedShare;
import ru.fedorova.exchange.db.entities.Shareholder;

import java.util.ArrayList;
import java.util.List;

@RestController
public class ShareholderController {

    @Autowired
    private ShareholderDatabase shareholders;
    @Autowired
    private PurchasedShareDatabase purchasedShares;
    @Autowired
    private CompanyDatabase companies;

    public static Integer getStockRate(Integer companyId) {
        final int MIN_COST = 100;
        final int MAX_COST = 50000;

        return (int) ((Math.random() * (MAX_COST - MIN_COST)) + MIN_COST);
    }

    @PostMapping("/shareholder/add_new/")
    public String addNewShareholder(@RequestParam(value = "id") int id, @RequestParam(value = "name") String name) {
        if (shareholders.existsById(id)) {
            return "Shareholder with the same ID already exists.\n";
        }
        shareholders.save(new Shareholder(id, name));
        return "Ok\n";
    }

    @PostMapping("/shareholder/add_money/")
    public String addMoney(@RequestParam(value = "id") int id, @RequestParam(value = "money") int money) {
        if (shareholders.findById(id).isEmpty()) {
            return "Id does not exist.\n";
        }
        Shareholder shareholder = shareholders.findById(id).get();
        shareholder.addMoney(money);
        shareholders.save(shareholder);
        return "Ok\n";
    }

    @PostMapping("/shareholder/buy_shares/")
    public String buyShares(@RequestParam(value = "shareholderId") int shareholderId,
                            @RequestParam(value = "companyId") int companyId,
                            @RequestParam(value = "quantity") int quantity) {
        if (shareholders.findById(shareholderId).isEmpty()) {
            return "ShareholderId does not exist.\n";
        }
        if (companies.findById(companyId).isEmpty()) {
            return "CompanyId does not exist\n";
        }
        Company company = companies.findById(companyId).get();
        if (company.getStockBalance() < quantity) {
            return "Transaction declined.\nCompany does not have enough shares. Actual number of shares is "
                    + company.getStockBalance().toString() + ".\n";
        }
        Integer sharesPrice = getStockRate(companyId) * quantity;
        Shareholder shareholder = shareholders.findById(shareholderId).get();
        if (shareholder.getMoney() < sharesPrice) {
            return "Transaction declined.\nUnfortunately, at the moment, shares cost "
                    + sharesPrice.toString() + ", but you have only " + shareholder.getMoney().toString() + ".\n";
        }

        shareholder.subtractMoney(sharesPrice);
        shareholders.save(shareholder);

        company.subtractShares(quantity);
        companies.save(company);

        if (purchasedShares.findByOwnerIdAndCompanyId(shareholderId, companyId).isEmpty()) {
            purchasedShares.save(new PurchasedShare(shareholderId, companyId, quantity));
        } else {
            PurchasedShare purchasedShare = purchasedShares.findByOwnerIdAndCompanyId(shareholderId, companyId).get();
            purchasedShare.addQuantity(quantity);
            purchasedShares.save(purchasedShare);
        }

        return "Ok\n";
    }

    @PostMapping("/shareholder/sell_shares/")
    public String sellShares(@RequestParam(value = "shareholderId") int shareholderId,
                             @RequestParam(value = "companyId") int companyId,
                             @RequestParam(value = "quantity") int quantity) {
        if (shareholders.findById(shareholderId).isEmpty()) {
            return "ShareholderId does not exist.\n";
        }
        if (companies.findById(companyId).isEmpty()) {
            return "CompanyId does not exist\n";
        }
        if (purchasedShares.findByOwnerIdAndCompanyId(shareholderId, companyId).isEmpty()) {
            return "\"Transaction declined.\nUnfortunately, you don't have shares of this company.\n";
        }
        PurchasedShare purchasedShare = purchasedShares.findByOwnerIdAndCompanyId(shareholderId, companyId).get();
        if (purchasedShare.getQuantity() < quantity) {
            return "Transaction declined.\nUnfortunately, you don't have enough shares. Actual number of shares is "
                    + purchasedShare.getQuantity().toString() + ".\n";
        }
        purchasedShare.subtractQuantity(quantity);
        purchasedShares.save(purchasedShare);

        Shareholder shareholder = shareholders.findById(shareholderId).get();
        int sharesPrice = getStockRate(companyId) * quantity;
        shareholder.addMoney(sharesPrice);
        shareholders.save(shareholder);

        Company company = companies.findById(companyId).get();
        company.addShares(quantity);
        companies.save(company);

        return "Ok\n";
    }

    @GetMapping("/shareholder/shares_report/{id}")
    public SharesReport getSharesReport(@PathVariable int id) {
        if (shareholders.findById(id).isEmpty()) {
            return new SharesReport(null);
        }
        SharesReport report = new SharesReport(shareholders.findById(id).get().getName());

        List<PurchasedShare> ownerPurchasedShares = (List<PurchasedShare>) purchasedShares.findAllByOwnerId(id);

        for (PurchasedShare share : ownerPurchasedShares) {
            report.addShare(new ShareInfo(companies.findById(share.getCompanyId()).get().getName(),
                    share.getQuantity(), share.getQuantity() * getStockRate(share.getCompanyId())));
        }
        return report;
    }

    // Including free money in the balance.
    @GetMapping("/shareholder/resources_report/{id}")
    public Integer getResources(@PathVariable int id) {
        if (shareholders.findById(id).isEmpty()) {
            return 0;
        }
        Integer sum = shareholders.findById(id).get().getMoney();
        SharesReport sharesReport = getSharesReport(id);
        for (ShareInfo share : sharesReport.shares) {
            sum += share.cost;
        }
        return sum;
    }

    public static class SharesReport {
        private final String ownerName;
        private final List<ShareInfo> shares = new ArrayList<>();

        public SharesReport(String ownerName) {
            this.ownerName = ownerName;
        }

        public void addShare(ShareInfo share) {
            this.shares.add(share);
        }

        public String getOwnerName() {
            return ownerName;
        }

        public List<ShareInfo> getShares() {
            return shares;
        }
    }

    public static class ShareInfo {
        private final String company_name;
        private final Integer quantity;
        private final Integer cost;

        public ShareInfo(String company_name, Integer quantity, Integer cost) {
            this.company_name = company_name;
            this.quantity = quantity;
            this.cost = cost;
        }

        public String getCompany_name() {
            return company_name;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public Integer getCost() {
            return cost;
        }
    }
}
