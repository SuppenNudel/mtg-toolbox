package io.github.suppennudel.gui;

import java.util.Collections;

import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

public class ComboColumn extends VBox {

	private ObservableList<DeckComboView> deckComboViews = FXCollections.observableArrayList(dcv -> new Observable[] { dcv.visibleProperty() });
	private VBox comboViewHolder;

	public ComboColumn(int comboSize) {
		comboViewHolder = new VBox(10);
		ScrollPane scrollPane = new ScrollPane(comboViewHolder);
		scrollPane.setContent(comboViewHolder);
		scrollPane.setFitToWidth(true);

		setAlignment(Pos.TOP_CENTER);
		getChildren().addAll(new Label(comboSize+""), scrollPane);

		//		deckComboViews.addListener((ListChangeListener<DeckComboView>) c -> {
		//			while(c.next()) {
		//				if(c.wasAdded()) {
		//					BooleanBinding booleanBinding = new BooleanBinding() {
		//						@Override
		//						protected boolean computeValue() {
		//							return c.getList().stream().anyMatch(DeckComboView::isVisible);
		//						}
		//					};
		//					visibleProperty().bind(booleanBinding);
		//				}
		//			}
		//		});
	}

	private void sort() {
		ObservableList<Node> workingCollection = FXCollections.observableArrayList(comboViewHolder.getChildren());

		Collections.sort(workingCollection, (o1, o2) -> {
			if (o1 instanceof DeckComboView && o2 instanceof DeckComboView) {
				DeckComboView dc1 = (DeckComboView) o1;
				DeckComboView dc2 = (DeckComboView) o2;
				return dc1.getTotalMissingPrice().get().compareTo(dc2.getTotalMissingPrice().get());
			}
			return 0;
		});

		comboViewHolder.getChildren().setAll(workingCollection);
	}

	public void add(DeckComboView deckComboView) {
		deckComboViews.add(deckComboView);
		comboViewHolder.getChildren().add(deckComboView);
		sort();
	}

}
