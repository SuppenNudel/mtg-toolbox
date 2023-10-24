package io.github.suppennudel;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import io.github.suppennudel.gui.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Launcher extends Application {

	private static Application app;
	private static Stage stage;

	private static String version;
	private static String name;

	public static void main(String[] args) {
		loadProjectInfos();
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		app = this;
		stage = primaryStage;

		FXMLLoader fxmlLoader = createFxmlLoader(MainController.class);
		Scene scene = new Scene(fxmlLoader.load());

		scene.getRoot().setStyle("-fx-base:black");

		primaryStage.sizeToScene();
		primaryStage.setMaxHeight(Double.MAX_VALUE);
		primaryStage.setTitle(String.format("%s - %s", name, version));
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	public static FXMLLoader createFxmlLoader(Class<? extends Initializable> controller) {
		String fxmlName = controller.getSimpleName().replace("Controller", "View");
		URL location = controller.getResource(fxmlName+".fxml");
		FXMLLoader fxmlLoader = new FXMLLoader(location);
		return fxmlLoader;
	}

	private static void loadProjectInfos() {
		final Properties properties = new Properties();
		try {
			ClassLoader classLoader = Launcher.class.getClassLoader();
			InputStream resourceAsStream = classLoader.getResourceAsStream("project.properties");
			properties.load(resourceAsStream);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		version = properties.getProperty("app.version");
		name = properties.getProperty("app.name");
	}

	public static void openBrowser(String uri) {
		app.getHostServices().showDocument(uri);
	}

	public static Application getApp() {
		return app;
	}
	public static Stage getStage() {
		return stage;
	}
	public static String getName() {
		return name;
	}
	public static String getVersion() {
		return version;
	}

}
