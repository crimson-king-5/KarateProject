package steps;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.*;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import pages.LoginPage;

import static org.testng.Assert.*;
import static org.testng.Assert.assertTrue;

public class LoginSteps {
    private static WebDriver webDriver;
    private LoginPage loginPage;

    @Before
    public void setUp() {
        WebDriverManager.chromedriver().setup();
        webDriver = new ChromeDriver();
        loginPage = new LoginPage(webDriver);
    }

    public static WebDriver getDriver() {
        return webDriver;
    }

    @After
    public void tearDown() {
        if (webDriver != null) {
            webDriver.quit();
        }
    }

    @Given("L'utilisateur est sur la page de connexion de SauceDemo")
    public void ouvrirSauceDemo() {
        loginPage.openLoginPage();
    }

    @When("Il saisit le nom d'utilisateur {string}")
    public void entrerUsername(String username) {
        loginPage.enterUsername(username);
    }

    @And("Il saisit le mot de passe {string}")
    public void entrerPassword(String password) {
        loginPage.enterPassword(password);
    }

    @And("Il clique sur le bouton de connexion")
    public void cliquerConnexion() {
        loginPage.clickLogin();
    }

    @Then("Il doit être redirigé vers la page d'accueil")
    public void verifiePageAccueil() {
        assertTrue(loginPage.isOnHomePage());
    }

    @Then("Il doit voir le message d'erreur {string}")
    public void verifieErreurConnexion(String messageAttendu) {
            String message = loginPage.getErrorMessage();
            assertTrue(message.contains(messageAttendu));

    }

    @Then("Il reste sur la page de connexion")
    public void verifieToujoursSurConnexion() {
        assertTrue(loginPage.isOnLoginPage());
    }

    @Then("L'utilisateur doit être redirigé vers la page de connexion")
    public void verifierRetourLogin() {
        assertTrue(loginPage.isLoginPageDisplayed());
    }

}
