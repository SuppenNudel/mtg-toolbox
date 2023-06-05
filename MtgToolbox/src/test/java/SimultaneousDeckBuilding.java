import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;

import org.junit.jupiter.api.Test;
import org.paukov.combinatorics3.Generator;

import io.github.suppennudel.CsvHandler;
import io.github.suppennudel.CsvProfile;
import io.github.suppennudel.MtgCsvBean;
import suppennudel.DeckList;

public class SimultaneousDeckBuilding {

	@Test
	public void main() throws IOException {
		Map<String, Integer> collection = getCombinedCollection(new File("src/test/resources/ManaBox_Collection.csv"));

		File[] files = new File("src/test/resources/deck-lists").listFiles();
		List<DeckList> deckLists = new ArrayList<>();
		for (File file : files) {
			DeckList deckList = new DeckList(file);
			deckLists.add(deckList);
		}

		Map<Integer, Integer> buildables = new HashMap<>();
		for (int setSize = 1; setSize < 9 && (!buildables.containsKey(setSize-1) || buildables.get(setSize - 1) > 0); ++setSize) {
			LocalDateTime start = LocalDateTime.now();
			final int setSizeFinal = setSize;
			buildables.put(setSizeFinal, 0);
			Generator.combination(deckLists).simple(setSize).stream().forEach(combo -> {
				Map<String, Integer> combinedList = combo.stream().map(deck -> deck.getCombined())
						.reduce(new BinaryOperator<Map<String, Integer>>() {
							@Override
							public Map<String, Integer> apply(Map<String, Integer> map1, Map<String, Integer> map2) {
								HashMap<String, Integer> newMap = new HashMap<>(map1);
								map2.forEach((key, value) -> newMap.merge(key, value, (v1, v2) -> v1 + v2));
								return newMap;
							}
						}).get();

				for (Entry<String, Integer> card : combinedList.entrySet()) {
					combinedList.compute(card.getKey(), (collectionCardName, listAmount) -> {
						int collectionAmount = collection.getOrDefault(card.getKey(), 0);
						return listAmount - collectionAmount;
					});
				}
				int totalCards = combinedList.values().stream().reduce(0, (a, b) -> (a < 0 ? 0 : a) + (b < 0 ? 0 : b));
				System.out.print(combo.size() + " For " + combo + " you ");
				if (totalCards == 0) {
					System.out.println("have everything you need");
					buildables.compute(setSizeFinal, new BiFunction<Integer, Integer, Integer>() {
						@Override
						public Integer apply(Integer key, Integer value) {
							return value + 1;
						}
					});
				} else {
					System.out.println("would need:");
//					BigDecimal totalPrice = new BigDecimal("0.0");
					for (Entry<String, Integer> card : combinedList.entrySet()) {
						Integer amount = card.getValue();
						String cardname = card.getKey();
						if (amount > 0) {
//							ListObject<CardObject> listObject = ScryfallApi.cards.search(cardname + " game:paper")
//									.order(Sorting.EUR).dir(Direction.ASC).get();
//							CardObject cardObject = listObject.getData().get(0);
//							Double price = cardObject.getPricesEnum().get(PriceType.EUR);
							System.out.println(amount + " " + cardname);
//							System.out.println(amount + " " + cardname + " - " + price + " €");
//							if(price != null) {
//								BigDecimal priceBD = new BigDecimal(price.toString());
//								BigDecimal amountBD = new BigDecimal(amount);
//								BigDecimal multiply = priceBD.multiply(amountBD);
//								totalPrice = totalPrice.add(multiply);
//							}
						}
					}
//					System.out.println(totalPrice + " €");
				}
				System.out.println("---------------------------------------------");
			});
			System.out.println(buildables);
			System.out.println("Execution Time: " + ChronoUnit.MILLIS.between(start, LocalDateTime.now()));
			System.out.println();
		}

	}

	private static Map<String, Integer> getCombinedCollection(File collectionFile) {
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
