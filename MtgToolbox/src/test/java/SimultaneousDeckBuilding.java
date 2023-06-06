import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.paukov.combinatorics3.Generator;

import de.rohmio.mtg.scryfall.api.ScryfallApi;
import de.rohmio.mtg.scryfall.api.model.CardObject;
import de.rohmio.mtg.scryfall.api.model.ListObject;
import de.rohmio.mtg.scryfall.api.model.enums.Direction;
import de.rohmio.mtg.scryfall.api.model.enums.PriceType;
import de.rohmio.mtg.scryfall.api.model.enums.Sorting;
import de.rohmio.mtg.scryfall.api.model.enums.Unique;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import suppennudel.DeckList;
import suppennudel.gui.SimulDecksView;

public class SimultaneousDeckBuilding {
	
	private Map<String, BigDecimal> prices = new HashMap<>();

	@Test
	public void main() throws IOException {
		Map<String, Integer> collection = SimulDecksView.getCombinedCollection(new File("src/test/resources/ManaBox_Collection.csv"));
		SimulDecksView.simulatePossession(collection);

		File[] files = new File("src/test/resources/deck-lists").listFiles();
		List<DeckList> deckLists = new ArrayList<>();
		for (File file : files) {
			DeckList deckList = new DeckList(file);
			deckLists.add(deckList);
		}
		
		calcCombinations(collection, deckLists);
		
	}
	
	private void calcCombinations(Map<String, Integer> collection, List<DeckList> deckLists) {
		List<List<DeckList>> nonBuildableSets = new ArrayList<>();
		List<List<DeckList>> buildableSets = new ArrayList<>();
		BooleanProperty previousHadBuildables = new SimpleBooleanProperty(true);

		for (int setSize = 1; setSize < deckLists.size() && previousHadBuildables.get(); ++setSize) {
			previousHadBuildables.set(false);
			LocalDateTime start = LocalDateTime.now();
			Generator.combination(deckLists).simple(setSize).stream().forEach(combo -> {
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
			});
			System.out.println("Execution Time: " + ChronoUnit.MILLIS.between(start, LocalDateTime.now()));
		}
	}



}
