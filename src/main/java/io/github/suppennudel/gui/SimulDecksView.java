package io.github.suppennudel.gui;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.paukov.combinatorics3.Generator;

import de.rohmio.mtg.mtgtop8.api.MtgTop8Api;
import de.rohmio.mtg.mtgtop8.api.model.MtgTop8DeckList;
import de.rohmio.mtg.mtgtop8.api.model.SearchResult;
import de.rohmio.mtg.mtgtop8.api.model.SearchResultDeck;
import de.rohmio.mtg.scryfall.api.ScryfallApi;
import de.rohmio.mtg.scryfall.api.model.CardObject;
import de.rohmio.mtg.scryfall.api.model.ListObject;
import de.rohmio.mtg.scryfall.api.model.enums.Direction;
import de.rohmio.mtg.scryfall.api.model.enums.PriceType;
import de.rohmio.mtg.scryfall.api.model.enums.Sorting;
import de.rohmio.mtg.scryfall.api.model.enums.Unique;
import io.github.suppennudel.BinderType;
import io.github.suppennudel.CsvHandler;
import io.github.suppennudel.CsvProfile;
import io.github.suppennudel.MtgCsvBean;
import io.github.suppennudel.decklists.GenericDeckInfoFx;
import io.github.suppennudel.decklists.aetherhub.Aetherhub;
import io.github.suppennudel.decklists.aetherhub.AetherhubDeckInfo;
import io.github.suppennudel.decklists.playingmtg.FormatRelevancePlayingPioneer;
import io.github.suppennudel.decklists.playingmtg.PlayingMtgDeckInfo;
import io.github.suppennudel.mtg.generic.MtgDeckInfo;
import io.github.suppennudel.mtg.generic.MtgFormat;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

public class SimulDecksView implements Initializable {

	@FXML
	private TableView<GenericDeckInfoFx> deckListTable;
	@FXML
	private TableColumn<GenericDeckInfoFx, Boolean> checkColumn;
	@FXML
	private TableColumn<GenericDeckInfoFx, String> deckNameColumn;
	@FXML
	private TableColumn<GenericDeckInfoFx, String> tierColumn;
	@FXML
	private TableColumn<GenericDeckInfoFx, LocalDate> dateColumn;
	@FXML
	private TableColumn<GenericDeckInfoFx, String> sourceColumn;

	@FXML
	private TextArea simulatePossessionTextArea;

	@FXML
	private HBox columnHolder;

	private ObservableList<GenericDeckInfoFx> deckLists = FXCollections
			.observableArrayList(dl -> new Observable[] { dl.selectedProperty() });

	private Map<String, BigDecimal> prices = new HashMap<>();
	private ObservableList<DeckComboView> combos = FXCollections.observableArrayList();
	private Map<String, Integer> myCollection;
	private Map<Integer, ComboColumn> columns = new HashMap<>();

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		combos.addListener(createDeckComboAddedListener());
		deckListTable.setItems(deckLists);

		deckListTable.setColumnResizePolicy(p -> true);

		checkColumn.setCellFactory(col -> {
			CheckBoxTableCell<GenericDeckInfoFx, Boolean> checkBoxTableCell = new CheckBoxTableCell<>();
			checkBoxTableCell.setSelectedStateCallback(index -> deckListTable.getItems().get(index).selectedProperty());
			return checkBoxTableCell;
		});

		checkColumn.setCellValueFactory(param -> param.getValue().selectedProperty());

		deckNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
		tierColumn.setCellValueFactory(new PropertyValueFactory<>("tier"));
		dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
		sourceColumn.setCellValueFactory(new PropertyValueFactory<>("source"));

		SearchResult searchResult = MtgTop8Api.search().format(MtgFormat.PIONEER).cards("Monastery Swiftspear").get();
		SearchResultDeck searchResultDeck = searchResult.getDecks().get(0);
		List<MtgTop8DeckList> list = MtgTop8Api.compare().deckIds(searchResultDeck.getDeckId()).get();
		MtgTop8DeckList deckList = list.get(0);
		System.out.println(deckList);

		deckLists.add(new GenericDeckInfoFx(deckList));
	}

	@FXML
	private void start() {
		combos.clear();
		columns.clear();
		columnHolder.getChildren().clear();

		// myCollection = getCombinedCollection(new File("G:/Meine
		// Ablage/ManaBox_Collection.csv"));
		myCollection = getCombinedCollection(new File("ManaBox_Collection.csv"));

		String text = simulatePossessionTextArea.getText();
		String[] lines = text.split("\n");
		Map<String, Integer> toAdd = new HashMap<>();

		for (String line : lines) {
			if (line.isEmpty()) {
				continue;
			}
			String[] split = line.split(" ", 2);
			toAdd.put(split[1], Integer.parseInt(split[0]));
		}
		simulatePossession(myCollection, toAdd);
		Thread thread = new Thread(() -> calcCombinations(myCollection, deckLists));
		thread.setDaemon(true);
		thread.start();
	}

	private <T> void sort(Pane pane, Comparator<T> comparator) {
		ObservableList<Node> workingCollection = FXCollections.observableArrayList(pane.getChildren());

		Collections.sort(workingCollection, (o1, o2) -> {
			if (o1 instanceof DeckComboView && o2 instanceof DeckComboView) {
				DeckComboView dc1 = (DeckComboView) o1;
				DeckComboView dc2 = (DeckComboView) o2;
				return dc1.getTotalMissingPrice().get().compareTo(dc2.getTotalMissingPrice().get());
			}
			return 0;
		});

		pane.getChildren().setAll(workingCollection);
	}

	private List<MtgDeckInfo> loadDeckLists() {
		File[] files = new File("decks").listFiles((FileFilter) File::isFile);
		List<MtgDeckInfo> deckLists = new ArrayList<>();
		for (File file : files) {
			try {
				MtgDeckInfo deckList = new AetherhubDeckInfo(file);
				deckLists.add(deckList);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return deckLists;
	}

	@FXML
	private void loadDeckListsFromAetherHub() {
		Aetherhub aetherhub = new Aetherhub();
		List<AetherhubDeckInfo> deckLists = aetherhub.getDeckLists("PlayingMTG", Aetherhub.Format.PIONEER);
		deckLists.forEach(deck -> {
			File file;
			try {
				file = aetherhub.downloadDeckListToFile(deck);
				deck.parse(file);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		});

		this.deckLists.setAll(deckLists.stream().map(GenericDeckInfoFx::new).collect(Collectors.toList()));
	}

	private List<PlayingMtgDeckInfo> loadDeckListsFromPlayingMtg() {
		ExecutorService executor = Executors.newFixedThreadPool(5, r -> {
			Thread thread = Executors.defaultThreadFactory().newThread(r);
			thread.setName("loadDeckList - " + thread.getName());
			thread.setDaemon(true);
			return thread;
		});
		try {
			List<PlayingMtgDeckInfo> allDecks = FormatRelevancePlayingPioneer.getOverview();
			List<Callable<PlayingMtgDeckInfo>> tasks = new ArrayList<>();
			for (PlayingMtgDeckInfo decklist : allDecks) {
				tasks.add(() -> {
					try {
						boolean success = FormatRelevancePlayingPioneer.fillDeckList(decklist);
						if (!success) {
							allDecks.remove(decklist);
						}
						return decklist;
					} catch (MalformedURLException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					return null;
				});
			}
			try {
				executor.invokeAll(tasks);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			executor.shutdown();
			return allDecks;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private ListChangeListener<DeckComboView> createDeckComboAddedListener() {
		return c -> {
			while (c.next()) {
				if (c.wasAdded()) {
					List<? extends DeckComboView> addedSubList = new ArrayList<>(c.getAddedSubList());
					addedSubList.forEach(deckComboView -> {
						int size = deckComboView.getDeckCombo().size();
						Platform.runLater(() -> {
							deckComboView.visibleProperty().bind(Bindings.createBooleanBinding(
									// only look at all check boxes that are selected
									// only show deck combo view if all selected decks are contained in the
									// deckComboView
									() -> deckLists.stream().filter(dl -> dl.selectedProperty().get())
									.allMatch(dl -> deckComboView.getDeckCombo().contains(dl)),
									deckLists));
							ComboColumn column = columns.compute(size, (k, v) -> {
								if (v == null) {
									v = new ComboColumn(size);
									columnHolder.getChildren().add(v);
								}
								return v;
							});
							column.add(deckComboView);
						});
					});
				}
			}
		};
	}

	public static void simulatePossession(Map<String, Integer> collection, Map<String, Integer> toAdd) {
		toAdd.forEach((key, value) -> collection.merge(key, value, Integer::sum));
	}

	private ExecutorService createExecutor() {
		return Executors.newFixedThreadPool(10, r -> {
			Thread thread = Executors.defaultThreadFactory().newThread(r);
			thread.setName("calcCombinations - " + thread.getName());
			thread.setDaemon(true);
			return thread;
		});
	}

	private void calcCombinations(Map<String, Integer> collection, List<GenericDeckInfoFx> deckLists) {
		ExecutorService executor = createExecutor();

		Set<GenericDeckInfoFx> previousSuccessfulDeckLists = null;
		for (int setSize = 1; previousSuccessfulDeckLists == null
				|| setSize < deckLists.size() && previousSuccessfulDeckLists.size() > 0; ++setSize) {
			System.out.println("Set Size: " + setSize + " with " + previousSuccessfulDeckLists);
			LocalDateTime start = LocalDateTime.now();
			Set<GenericDeckInfoFx> successfulDeckLists = new HashSet<>();

			List<Callable<DeckComboView>> tasks = new ArrayList<>();
			Generator.combination(previousSuccessfulDeckLists == null ? deckLists : previousSuccessfulDeckLists)
			.simple(setSize).stream().forEach(combo -> {
				tasks.add(() -> {
					DeckComboView calculatedCombo = calculateCombo(combo);
					if (calculatedCombo == null) {
						return null;
					}
					synchronized (combos) {
						combos.add(calculatedCombo);
					}
					if (calculatedCombo.getMissingCards().size() == 0) {
						synchronized (successfulDeckLists) {
							successfulDeckLists.addAll(calculatedCombo.getDeckCombo());
						}
					}
					return calculatedCombo;
				});
			});
			try {
				executor.invokeAll(tasks);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			previousSuccessfulDeckLists = new HashSet<>(successfulDeckLists);
			System.out.println("Execution Time: " + ChronoUnit.MILLIS.between(start, LocalDateTime.now()));
		}
		executor.shutdown();
	}

	private DeckComboView calculateCombo(List<GenericDeckInfoFx> combo) {
		Thread.currentThread().setName(combo.size() + " - " + combo.toString());
		// check if combo should be analyzed
		// not analyzing if subset is already not buildable
		synchronized (combos) {
			for (DeckComboView c : combos) {
				if (c.getMissingCards().size() > 0) {
					// not buildable
					if (combo.containsAll(c.getDeckCombo())) {
						return null;
					}
				}
			}
		}

		Map<String, Integer> combinedList = new HashMap<>();
		combo.forEach(dl -> dl.getCombined()
				.forEach((card, amount) -> combinedList.merge(card, amount, (v1, v2) -> v1 + v2)));

		Map<String, Integer> missingCards = new HashMap<>();
		for (Entry<String, Integer> card : combinedList.entrySet()) {
			String cardname = card.getKey();
			Integer collectionCards = myCollection.getOrDefault(cardname, 0);
			Integer listCards = card.getValue();
			missingCards.put(cardname, listCards - collectionCards);
		}

		DeckComboView deckComboView = new DeckComboView(combo);
		BigDecimal totalPrice = new BigDecimal("0.0");
		for (Entry<String, Integer> card : missingCards.entrySet()) {
			Integer amount = card.getValue();
			String cardname = card.getKey();
			if (amount > 0) {
				BigDecimal priceBD = prices.get(cardname);
				if (priceBD == null) {
					ListObject<CardObject> listObject = ScryfallApi.cards.search("!\"" + cardname + "\" game:paper")
							.unique(Unique.PRINTS).order(Sorting.EUR).dir(Direction.ASC).get();
					Optional<Double> findFirst = listObject.getData().stream()
							.map(c -> c.getPricesEnum().get(PriceType.EUR)).filter(p -> p != null).findFirst();
					if (findFirst.isPresent()) {
						Double price = findFirst.get();
						priceBD = new BigDecimal(price.toString());
					} else {
						priceBD = new BigDecimal("0");
					}
					prices.put(cardname, priceBD);
				}
				BigDecimal amountBD = new BigDecimal(amount);
				BigDecimal multiply = priceBD.multiply(amountBD);
				totalPrice = totalPrice.add(multiply);
				deckComboView.addMissingCard(cardname, amountBD, priceBD);
			}
		}
		return deckComboView;
	}

	public static Map<String, Integer> getCombinedCollection(File collectionFile) {
		List<MtgCsvBean> readCsv = CsvHandler.readCsv(collectionFile, CsvProfile.MANABOX);
		Map<String, Integer> collection = new HashMap<>();
		readCsv.forEach(bean -> {
			BinderType binderType = bean.getBinderType();
			if (binderType == BinderType.list) {
				return;
			}
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
