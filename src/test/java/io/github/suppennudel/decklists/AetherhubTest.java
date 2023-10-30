package io.github.suppennudel.decklists;

import java.io.IOException;
import java.net.MalformedURLException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

import io.github.suppennudel.decklists.aetherhub.Aetherhub;
import io.github.suppennudel.decklists.aetherhub.AetherhubDeckInfo;

public class AetherhubTest {

	@Test
	public void test() {
		System.out.println(LocalDate.now().toString());
	}

	@Test
	public void getDeckLists() {
		Aetherhub aetherhub = new Aetherhub();
		aetherhub.getDeckLists("PlayingMTG");
	}

	@Test
	public void getDeckListsPioneer() {
		Aetherhub aetherhub = new Aetherhub();
		List<AetherhubDeckInfo> deckLists = aetherhub.getDeckLists("PlayingMTG", Aetherhub.Format.PIONEER);
		deckLists.forEach(deck -> {
			aetherhub.downloadDeckListToFile(deck);
		});
	}

	@Test
	public void getDeck() throws MalformedURLException, IOException {
		Aetherhub aetherhub = new Aetherhub();
		aetherhub.getDeck("azorius-60-card-control");
	}

	@Test
	public void requestTest() {
		Aetherhub deckListSource = new Aetherhub();
		deckListSource.makeRequest("https://aetherhub.com/User/PlayingMTG/Decks", driver -> {

		});
	}

	@Test
	public void locatorTest() {
		String url = "https://locator.wizards.com/store/9137";
		WebDriver driver = new FirefoxDriver();
		driver.get(url);
		driver.manage().timeouts().implicitlyWait(Duration.ofMillis(2000));
		WebElement storeName = driver.findElement(By.tagName("h3"));
		System.out.println(storeName.getText());
		driver.quit();

	}

}
