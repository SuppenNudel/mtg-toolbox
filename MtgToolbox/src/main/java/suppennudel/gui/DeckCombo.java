package suppennudel.gui;

import java.util.List;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import suppennudel.DeckList;

public class DeckCombo extends VBox {

	private List<DeckList> deckCombo;
	
	public DeckCombo(List<DeckList> deckCombo) {
		this.deckCombo = deckCombo;
		deckCombo.forEach(d -> {
			getChildren().add(new Label(d.toString()));
		});
		
		setPrefWidth(USE_COMPUTED_SIZE);
		
		setStyle("-fx-border-color: black");
	}
	
	public List<DeckList> getDeckCombo() {
		return deckCombo;
	}

}
