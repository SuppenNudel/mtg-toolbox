package suppennudel;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;

public class DeckList {
	
	private enum ListSection {
		MAIN,
		SIDEBOARD,
		COMPANION;
	}

	private File file;
	private Map<String, Integer> main = new HashMap<>();
	private Map<String, Integer> side = new HashMap<>();

	private Map<String, Integer> combined;

	public DeckList(File file) throws IOException {
		this.file = file;
		String readFileToString = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
//		readFileToString = readFileToString.replace("\uFEFF", ""); // remove BOM
		String[] lines = readFileToString.split(System.lineSeparator());
		ListSection listSection = null;
		for (String line : lines) {
			if (line.isEmpty()) {
				// don't change, just skip
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
					if(mapToPutIn != null) {
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
		return file.getName();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DeckList) {
			DeckList other = (DeckList) obj;
			return other.toString().equals(toString());
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

	public Map<String, Integer> getCombined() {
		if (combined == null) {
			combined = new HashMap<>(main);
			side.forEach((key, value) -> combined.merge(key, value, (v1, v2) -> v1 + v2));
		}
		return combined;
	}

}
