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

    public final static String OK = "Ok\n";
    public final static String SHAREHOLDER_ALREADY_EXISTS
            = "Shareholder with the same ID already exists.\n";
    public final static String SHAREHOLDER_NOT_EXISTS = "ShareholderId does not exist.\n";
    public final static String COMPANY_NOT_EXISTS = "CompanyId does not exist\n";
    public final static String COMPANY_NOT_ENOUGH_SHARES
            = "Transaction declined.\nCompany does not have enough shares.\n";
    public final static String SHAREHOLDER_NOT_ENOUGH_MONEY
            = "Transaction declined.\nUnfortunately, at the moment, shares cost more, than you have.\n";
    public final static String SHAREHOLDER_DO_NOT_HAVE_SHARES
            = "Transaction declined.\nUnfortunately, you don't have shares of this company.\n";
    public final static String SHAREHOLDER_DO_NOT_HAVE_ENOUGH_SHARES
            = "Transaction declined.\nUnfortunately, you don't have enough shares of this company.\n";

    public static Integer getStockRate(Integer companyId) {
        final int MIN_COST = 100;
        final int MAX_COST = 50000;

        return (int) ((Math.random() * (MAX_COST - MIN_COST)) + MIN_COST);
    }

    @PostMapping("/shareholder/add_new")
    public String addNewShareholder(@RequestParam(value = "id") int id, @RequestParam(value = "name") String name) {
        if (shareholders.existsById(id)) {
            return SHAREHOLDER_ALREADY_EXISTS;
        }
        shareholders.save(new Shareholder(id, name));
        return OK;
    }

    @PostMapping("/shareholder/add_money")
    public String addMoney(@RequestParam(value = "id") int id, @RequestParam(value = "money") int money) {
        if (shareholders.findById(id).isEmpty()) {
            return SHAREHOLDER_NOT_EXISTS;
        }
        Shareholder shareholder = shareholders.findById(id).get();
        shareholder.addMoney(money);
        shareholders.save(shareholder);
        return OK;
    }

    @PostMapping("/shareholder/buy_shares")
    public String buyShares(@RequestParam(value = "shareholderId") int shareholderId,
                            @RequestParam(value = "companyId") int companyId,
                            @RequestParam(value = "quantity") int quantity) {
        if (shareholders.findById(shareholderId).isEmpty()) {
            return SHAREHOLDER_NOT_EXISTS;
        }
        if (companies.findById(companyId).isEmpty()) {
            return COMPANY_NOT_EXISTS;
        }
        Company company = companies.findById(companyId).get();
        if (company.getStockBalance() < quantity) {
            return COMPANY_NOT_ENOUGH_SHARES;
        }
        Integer sharesPrice = getStockRate(companyId) * quantity;
        Shareholder shareholder = shareholders.findById(shareholderId).get();
        if (shareholder.getMoney() < sharesPrice) {
            return SHAREHOLDER_NOT_ENOUGH_MONEY;
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

        return OK;
    }

    @PostMapping("/shareholder/sell_shares")
    public String sellShares(@RequestParam(value = "shareholderId") int shareholderId,
                             @RequestParam(value = "companyId") int companyId,
                             @RequestParam(value = "quantity") int quantity) {
        if (shareholders.findById(shareholderId).isEmpty()) {
            return SHAREHOLDER_NOT_EXISTS;
        }
        if (companies.findById(companyId).isEmpty()) {
            return COMPANY_NOT_EXISTS;
        }
        if (purchasedShares.findByOwnerIdAndCompanyId(shareholderId, companyId).isEmpty()) {
            return SHAREHOLDER_DO_NOT_HAVE_SHARES;
        }
        PurchasedShare purchasedShare = purchasedShares.findByOwnerIdAndCompanyId(shareholderId, companyId).get();
        if (purchasedShare.getQuantity() < quantity) {
            return SHAREHOLDER_DO_NOT_HAVE_ENOUGH_SHARES;
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

        return OK;
    }

    @GetMapping("/shareholder/shares_report/{id}")
    public SharesReport getSharesReport(@PathVariable int id) {
        if (shareholders.findById(id).isEmpty()) {
            return null;
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
            return null;
        }
        Integer sum = shareholders.findById(id).get().getMoney();
        SharesReport sharesReport = getSharesReport(id);
        for (ShareInfo share : sharesReport.shares) {
            sum += share.cost;
        }
        return sum;
    }

    public static class SharesReport {
        private String ownerName;
        private final List<ShareInfo> shares = new ArrayList<>();

        public SharesReport() {}

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
        private String companyName;
        private Integer quantity;
        private Integer cost;

        public ShareInfo() {}

        public ShareInfo(String companyName, Integer quantity, Integer cost) {
            this.companyName = companyName;
            this.quantity = quantity;
            this.cost = cost;
        }

        public String getCompanyName() {
            return companyName;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public Integer getCost() {
            return cost;
        }
    }
}
