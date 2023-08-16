package suppennudel.gui;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.VBox;
import suppennudel.DeckList;

public class DeckComboView extends VBox {

	private List<DeckList> deckCombo;
	private ObjectProperty<BigDecimal> totalMissingPrice = new SimpleObjectProperty<>(new BigDecimal("0"));
	private ObservableList<MissingCard> missingCards = FXCollections.observableArrayList();

	public DeckComboView(List<DeckList> deckCombo) {
		this.deckCombo = deckCombo;

		VBox vDeckNames = new VBox();
		deckCombo.forEach(d -> {
			vDeckNames.getChildren().add(new Label(d.toString()));
		});

		setSpacing(5.0);

		setPrefWidth(USE_COMPUTED_SIZE);

		setStyle("-fx-border-color: black");
		setPadding(new Insets(10));

		managedProperty().bindBidirectional(visibleProperty());

		VBox vBoxMissingCards = new VBox();

		Label totalPriceLabel = new Label();
		totalPriceLabel.textProperty().bind(totalMissingPrice.asString());

		totalMissingPrice.bind(Bindings.createObjectBinding(
				() -> missingCards.stream().map(missingCard -> missingCard.getPrice().multiply(missingCard.getAmount()))
				.reduce(new BigDecimal("0"), BigDecimal::add),
				missingCards));
		missingCards.addListener((ListChangeListener<MissingCard>) c -> {
			while (c.next()) {
				c.getAddedSubList().forEach(mc -> vBoxMissingCards.getChildren()
						.add(new Label(mc.getAmount() + " " + mc.getCardname() + " - " + mc.getPrice())));
			}
		});

		getChildren().addAll(vDeckNames, vBoxMissingCards, totalPriceLabel);

		MenuItem copyMissingCardsMenu = new MenuItem("Copy missing cards");
		copyMissingCardsMenu.setOnAction(event -> {
			Optional<String> data = missingCards.stream().map(mc -> mc.amount + " " + mc.cardname)
					.reduce((t, u) -> t + System.lineSeparator() + u);
			final Clipboard clipboard = Clipboard.getSystemClipboard();
			final ClipboardContent content = new ClipboardContent();
			content.putString(data.get());
			clipboard.setContent(content);
		});
		ContextMenu contextMenu = new ContextMenu();
		contextMenu.getItems().add(copyMissingCardsMenu);
		setOnContextMenuRequested(event -> {
			if (!missingCards.isEmpty()) {
				contextMenu.show(this, event.getScreenX(), event.getScreenY());
			}
		});
	}

	public ObservableList<MissingCard> getMissingCards() {
		return missingCards;
	}

	@Override
	public String toString() {
		return "view of " + deckCombo;
	}

	public ObjectProperty<BigDecimal> getTotalMissingPrice() {
		return totalMissingPrice;
	}

	public List<DeckList> getDeckCombo() {
		return deckCombo;
	}

	public void addMissingCard(String cardname, BigDecimal amount, BigDecimal price) {
		missingCards.add(new MissingCard(cardname, amount, price));
	}

	private class MissingCard {

		private String cardname;
		private BigDecimal amount;
		private BigDecimal price;

		public MissingCard(String cardname, BigDecimal amount, BigDecimal price) {
			this.cardname = cardname;
			this.amount = amount;
			this.price = price;
		}

		public String getCardname() {
			return cardname;
		}

		public BigDecimal getAmount() {
			return amount;
		}

		public BigDecimal getPrice() {
			return price;
		}
	}

}
