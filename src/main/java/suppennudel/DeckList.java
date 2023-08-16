package suppennudel;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class DeckList {

	private enum ListSection {
		MAIN, SIDEBOARD, COMPANION;
	}

	public enum Tier {
		S, A, B, C, D, OFF_META;
	}

	private Map<String, Integer> main = new HashMap<>();
	private Map<String, Integer> side = new HashMap<>();

	private Map<String, Integer> combined;

	private BooleanProperty selected = new SimpleBooleanProperty(false);

	private File file;
	private String url;
	private String name;
	private Tier tier;

	private String thumbSource;

	public BooleanProperty selectedProperty() {
		return selected;
	}

	public DeckList(String name) {
		this.name = name;
	}

	public DeckList(String name, String url, Tier tier) {
		this(name);
		this.url = url;
		this.tier = tier;
	}

	public Tier getTier() {
		return tier;
	}

	public String getName() {
		return name;
	}

	public DeckList(File file) throws IOException {
		this(file.getName());
		this.file = file;
		loadFromFile(file);
	}

	public void loadFromFile(File file) throws IOException {
		String readFileToString = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
		// readFileToString = readFileToString.replace("\uFEFF", ""); // remove BOM
		String[] lines = readFileToString.split(System.lineSeparator());
		ListSection listSection = null;
		for (String line : lines) {
			if (line.isEmpty()) {
				listSection = ListSection.SIDEBOARD;
			} else if (line.equals("Deck")) {
				listSection = ListSection.MAIN;
			} else if (line.equals("Sideboard")) {
				listSection = ListSection.SIDEBOARD;
			} else if (line.equals("Companion")) {
				listSection = ListSection.COMPANION;
			} else {
				try {
					String[] split = line.split(" ", 2);
					int amount = Integer.parseInt(split[0]);
					String cardname = split[1];

					Map<String, Integer> mapToPutIn = null;
					switch (listSection) {
					case MAIN:
						mapToPutIn = main;
						break;
					case SIDEBOARD:
						mapToPutIn = side;
						break;
					default:
						break;
					}
					if (mapToPutIn != null) {
						cardname = cardname.replaceAll(" //.*", "");
						mapToPutIn.put(cardname, amount);
					}
				} catch (Exception e) {
					e.printStackTrace();
					System.err.println(e.getMessage() + " in " + file);
					throw e;
				}
			}
		}
	}

	@Override
	public String toString() {
		return tier + " - " + name;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DeckList) {
			DeckList other = (DeckList) obj;
			boolean result = other.toString().equals(toString());
			return result;
		}
		return false;
	}

	public Map<String, Integer> getMain() {
		return main;
	}

	public Map<String, Integer> getSide() {
		return side;
	}

	public File getFile() {
		return file;
	}

	public String getUrl() {
		return url;
	}

	public Map<String, Integer> getCombined() {
		if (combined == null) {
			combined = new HashMap<>(main);
			side.forEach((key, value) -> combined.merge(key, value, (v1, v2) -> v1 + v2));
		}
		return combined;
	}

	public void setThumbSource(String thumbSource) {
		this.thumbSource = thumbSource;
	}
	public String getThumbSource() {
		return thumbSource;
	}

}
