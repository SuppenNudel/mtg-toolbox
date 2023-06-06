package suppennudel.gui;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import suppennudel.Launcher;
import suppennudel.config.UserConfig;
import suppennudel.config.UserConfigKey;
import suppennudel.config.UserConfigListKey;

public class MainController implements Initializable {
	
	@FXML
	private TabPane tabPane;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub

		// usage
		String string = UserConfig.getConfig().get(UserConfigKey.EXAMPLE_CONFIG);
		List<String> stringList = UserConfig.getConfig().getList(UserConfigListKey.EXAMPLE_LIST_CONFIG);
		
		try {
			Node load = Launcher.createFxmlLoader(SimulDecksView.class).load();
			Tab e = new Tab("Simul Decks", load);
			tabPane.getTabs().add(e);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
