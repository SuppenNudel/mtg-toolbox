package io.github.suppennudel.gui;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import io.github.suppennudel.Launcher;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

public class MainController implements Initializable {

	@FXML
	private TabPane tabPane;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		try {
			Node load = Launcher.createFxmlLoader(SimulDecksView.class).load();
			Tab e = new Tab("Simul Decks", load);
			tabPane.getTabs().add(e);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
