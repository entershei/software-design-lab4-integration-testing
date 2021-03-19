package ru.fedorova.exchange;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.fedorova.exchange.web.ExchangeController;
import ru.fedorova.exchange.web.ExchangeController.StockInfo;
import ru.fedorova.exchange.web.ShareholderController;
import ru.fedorova.exchange.web.ShareholderController.*;

import java.util.Collections;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static ru.fedorova.exchange.web.ExchangeController.COMPANY_ALREADY_EXISTS;
import static ru.fedorova.exchange.web.ShareholderController.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class IntegrationTest {

//    @Container
//    public GenericContainer redis = new GenericContainer(DockerImageName.parse("redis:5.0.3-alpine"))
//            .withExposedPorts(8080);

    @Autowired
    private ExchangeController exchangeController;
    @Autowired
    private ShareholderController shareholderController;
    @Autowired
    private TestRestTemplate restTemplate;
    @LocalServerPort
    private int port;

    private String addNewCompanyUrl = "add_new_company?id=%d&name=%s&stock_balance=%d";
    private String reportCompanyUrl = "report_company/%d";
    private String reportStocksUrl = "report_stocks";
    private String addNewShareholderUrl = "add_new?id=%d&name=%s";
    private String addMoneyUrl = "add_money?id=%d&money=%d";
    private String buySharesUrl = "buy_shares?shareholderId=%d&companyId=%d&quantity=%d";
    private String sellSharesUrl = "sell_shares?shareholderId=%d&companyId=%d&quantity=%d";
    private String sharesReportUrl = "shares_report/%d";
    private String resourcesReportUrl = "resources_report/%d";


    @BeforeEach
    public void setUp() {
        String urlExchangePrefix = "http://localhost:" + port + "/exchange/";
        addNewCompanyUrl = urlExchangePrefix + addNewCompanyUrl;
        reportCompanyUrl = urlExchangePrefix + reportCompanyUrl;
        reportStocksUrl = urlExchangePrefix + reportStocksUrl;

        String urlShareholderPrefix = "http://localhost:" + port + "/shareholder/";
        addNewShareholderUrl = urlShareholderPrefix + addNewShareholderUrl;
        addMoneyUrl = urlShareholderPrefix + addMoneyUrl;
        buySharesUrl = urlShareholderPrefix + buySharesUrl;
        sellSharesUrl = urlShareholderPrefix + sellSharesUrl;
        sharesReportUrl = urlShareholderPrefix + sharesReportUrl;
        resourcesReportUrl = urlShareholderPrefix + resourcesReportUrl;
    }

    @Test
    public void contextLoads() {
        assertThat(exchangeController).isNotNull();
        assertThat(shareholderController).isNotNull();
        assertThat(restTemplate).isNotNull();
    }

    @Test
    public void doubleRegistration() {
        assertThat(restTemplate.postForObject(
                String.format(addNewCompanyUrl, 1, "Google", 1000), null, String.class)).isEqualTo(OK);
        assertThat(restTemplate.postForObject(
                String.format(addNewCompanyUrl, 1, "Yandex", 100), null, String.class))
                .isEqualTo(COMPANY_ALREADY_EXISTS);

        assertThat(restTemplate.postForObject(
                String.format(addNewShareholderUrl, 1, "Ira"), null, String.class)).isEqualTo(OK);
        assertThat(restTemplate.postForObject(
                String.format(addNewShareholderUrl, 1, "Masha"), null, String.class))
                .isEqualTo(SHAREHOLDER_ALREADY_EXISTS);
    }

    @Test
    public void notExist() {
        assertThat(restTemplate.postForObject(String.format(addMoneyUrl, 1, 100), null, String.class))
                .isEqualTo(SHAREHOLDER_NOT_EXISTS);
        assertThat(restTemplate.postForObject(String.format(buySharesUrl, 1, 1, 100), null, String.class))
                .isEqualTo(SHAREHOLDER_NOT_EXISTS);
        assertThat(restTemplate.postForObject(
                String.format(addNewShareholderUrl, 1, "Ira"), null, String.class)).isEqualTo(OK);
        assertThat(restTemplate.postForObject(String.format(sellSharesUrl, 1, 1, 100), null, String.class))
                .isEqualTo(COMPANY_NOT_EXISTS);
    }

    @Test
    public void emptyReports() {
        assertThat(restTemplate.getForObject(String.format(reportCompanyUrl, 1), ExchangeController.StockInfo.class)).isNull();
        assertThat(restTemplate.getForObject(reportStocksUrl,
                Collections.<StockInfo>emptyList().getClass()))
                .isEqualTo(Collections.<StockInfo>emptyList());
        assertThat(restTemplate.getForObject(String.format(resourcesReportUrl, 1), SharesReport.class)).isNull();
        assertThat(restTemplate.getForObject(String.format(resourcesReportUrl, 1), Integer.class)).isNull();
    }
}
