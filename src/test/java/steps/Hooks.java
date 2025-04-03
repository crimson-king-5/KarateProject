package steps;

import io.cucumber.java.AfterStep;
import io.cucumber.java.Scenario;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

public class Hooks {

    @AfterStep
    public void takeScreenshotIfStepFails(Scenario scenario) {
        WebDriver driver = LoginSteps.getDriver();
        if (scenario.isFailed() && driver != null) {
            try {
                final byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                scenario.attach(screenshot, "image/png", "screenshot");
                System.out.println("Capture d'écran ajoutée au scénario : " + scenario.getName());
            } catch (Exception e) {
                System.err.println("Impossible de prendre une capture : " + e.getMessage());
            }
        }
    }
}