package suppennudel;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.fxml.Initializable;
import suppennudel.config.UserConfig;
import suppennudel.config.UserConfigKey;
import suppennudel.config.UserConfigListKey;

public class MainController implements Initializable {

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub

		// usage
		String string = UserConfig.getConfig().get(UserConfigKey.EXAMPLE_CONFIG);
		List<String> stringList = UserConfig.getConfig().getList(UserConfigListKey.EXAMPLE_LIST_CONFIG);
	}

}
