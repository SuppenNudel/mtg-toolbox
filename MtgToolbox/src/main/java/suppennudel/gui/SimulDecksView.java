package suppennudel.gui;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.paukov.combinatorics3.Generator;

import de.rohmio.mtg.scryfall.api.ScryfallApi;
import de.rohmio.mtg.scryfall.api.model.CardObject;
import de.rohmio.mtg.scryfall.api.model.ListObject;
import de.rohmio.mtg.scryfall.api.model.enums.Direction;
import de.rohmio.mtg.scryfall.api.model.enums.PriceType;
import de.rohmio.mtg.scryfall.api.model.enums.Sorting;
import de.rohmio.mtg.scryfall.api.model.enums.Unique;
import io.github.suppennudel.CsvHandler;
import io.github.suppennudel.CsvProfile;
import io.github.suppennudel.MtgCsvBean;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import suppennudel.DeckList;

public class SimulDecksView implements Initializable {
	
	@FXML
	private VBox vbox;
	@FXML
	private FlowPane flow;

	private Map<String, BigDecimal> prices = new HashMap<>();
	private ObservableList<List<DeckList>> buildableSets = FXCollections.observableArrayList();
	private List<List<DeckList>> nonBuildableSets = new ArrayList<>();
	private Map<String, Integer> collection;
	private BooleanProperty previousHadBuildables = new SimpleBooleanProperty(true);
	
	private Map<DeckList, CheckBox> checkBoxes = new HashMap<>();
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		buildableSets.addListener(new ListChangeListener<List<DeckList>>() {
			@Override
			public void onChanged(Change<? extends List<DeckList>> c) {
				while(c.next()) {
					if(c.wasAdded()) {
						List<? extends List<DeckList>> addedSubList = new ArrayList<>(c.getAddedSubList());
						addedSubList.forEach(list -> {
							DeckCombo deckCombo = new DeckCombo(list);
							boolean contains = flow.getChildren().contains(deckCombo);
							if(contains) {
								System.out.println("already in flow");
							} else {
								Platform.runLater(() -> {
									flow.getChildren().add(deckCombo);
								});
							}
						});
					}
				}
			}
		});
		
		collection = getCombinedCollection(new File("src/test/resources/ManaBox_Collection.csv"));
		simulatePossession(collection);
		
		File[] files = new File("src/test/resources/deck-lists").listFiles();
		List<DeckList> deckLists = new ArrayList<>();
		for (File file : files) {
			try {
				DeckList deckList = new DeckList(file);
				deckLists.add(deckList);
				CheckBox checkBox = new CheckBox(deckList.toString());
				checkBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
					@Override
					public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue,
							Boolean newValue) {
						flow.getChildren().forEach(child -> {
							if (child instanceof DeckCombo) {
								DeckCombo deckCombo = (DeckCombo) child;
								if(deckCombo.getDeckCombo().contains(deckList)) {
//									deckCombo.setVisible(newValue);
								}
							}
						});
					}
				});
				checkBoxes.put(deckList, checkBox);
				vbox.getChildren().add(checkBox);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		Thread thread = new Thread(() -> calcCombinations(collection, deckLists));
		thread.setDaemon(true);
		thread.start();
	}

	public static void simulatePossession(Map<String, Integer> collection) {
//		collection.compute("Blue Sun's Twilight", (key, old) -> old == null ? 3 : old + 3);
//		collection.compute("Memory Deluge", (key, old) -> old == null ? 1 : old + 1);
//		collection.compute("Reckoner Bankbuster", (key, old) -> old == null ? 1 : old + 1);
//		collection.compute("Obliterating Bolt", (key, old) -> old == null ? 1 : old + 1);
//		collection.compute("Thrun, Breaker of Silence", (key, old) -> old == null ? 2 : old + 2);
//		collection.compute("Rending Volley", (key, old) -> old == null ? 1 : old + 1);
//		collection.compute("Stubborn Denial", (key, old) -> old == null ? 1 : old + 1);
//		collection.compute("Otawara, Soaring City", (key, old) -> old == null ? 1 : old + 1);
	}

	
	private void calcCombinations(Map<String, Integer> collection, List<DeckList> deckLists) {
		for (int setSize = 1; setSize < deckLists.size() && previousHadBuildables.get(); ++setSize) {
			LocalDateTime start = LocalDateTime.now();
			previousHadBuildables.set(false);
			Generator.combination(deckLists).simple(setSize).stream().forEach(combo -> {
				calculateCombo(combo);
			});
			System.out.println("Execution Time: " + ChronoUnit.MILLIS.between(start, LocalDateTime.now()));
		}
	}
	
	private void calculateCombo(List<DeckList> combo) {
		boolean anyMatch = nonBuildableSets.stream().anyMatch(list -> {
			boolean containsAll = combo.containsAll(list);
			return containsAll;
		});
		if (anyMatch) {
			return;
		}

		List<Map<String, Integer>> collect = combo.stream().map(deck -> deck.getCombined())
				.collect(Collectors.toList());
		Map<String, Integer> combinedList = Stream.of(collect.toArray())
				.flatMap(m -> ((Map<String, Integer>) m).entrySet().stream())
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v1 + v2));

		Map<String, Integer> result = new HashMap<>();
		for (Entry<String, Integer> card : combinedList.entrySet()) {
			String cardname = card.getKey();
			Integer collectionCards = collection.getOrDefault(cardname, 0);
			Integer listCards = card.getValue();
			result.put(cardname, listCards - collectionCards);
		}
		
		int totalCards = result.values().stream().reduce(0, (a, b) -> (a < 0 ? 0 : a) + (b < 0 ? 0 : b));
		System.out.print(combo.size() + " decks - For " + combo + " you ");
		if (totalCards == 0) {
			System.out.println("have everything you need");
			buildableSets.add(combo);
			previousHadBuildables.set(true);
		} else {
			nonBuildableSets.add(combo);
			System.out.println("would need:");
			BigDecimal totalPrice = new BigDecimal("0.0");
			for (Entry<String, Integer> card : result.entrySet()) {
				Integer amount = card.getValue();
				String cardname = card.getKey();
				if (amount > 0) {
					BigDecimal bigDecimal = prices.get(cardname);
					System.out.print(amount + " " + cardname + " - ");
					if (bigDecimal == null) {
						ListObject<CardObject> listObject = ScryfallApi.cards.search("!\""+cardname + "\" game:paper").unique(Unique.PRINTS)
								.order(Sorting.EUR).dir(Direction.ASC).get();
						Optional<Double> findFirst = listObject.getData().stream()
								.map(c -> c.getPricesEnum().get(PriceType.EUR)).filter(p -> p != null)
								.findFirst();
						if (findFirst.isPresent()) {
							Double price = findFirst.get();
							bigDecimal = new BigDecimal(price.toString());
						} else {
							bigDecimal = new BigDecimal("0");
						}
						prices.put(cardname, bigDecimal);
					}
					BigDecimal amountBD = new BigDecimal(amount);
					BigDecimal multiply = bigDecimal.multiply(amountBD);
					totalPrice = totalPrice.add(multiply);
					System.out.println(multiply + "€");
				}
			}
			System.out.println(totalPrice + "€");
		}
		System.out.println("---------------------------------------------");
	}

	public static Map<String, Integer> getCombinedCollection(File collectionFile) {
		List<MtgCsvBean> readCsv = CsvHandler.readCsv(collectionFile, CsvProfile.MANABOX);
		Map<String, Integer> collection = new HashMap<>();
		readCsv.forEach(bean -> {
			collection.compute(bean.getCardName().replaceAll(" //.*", ""), (key, previousValue) -> {
				Integer quantity = bean.getQuantity();
				if (previousValue == null) {
					previousValue = 0;
				}
				return previousValue + quantity;
			});
		});
		return collection;
	}

}
