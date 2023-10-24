package io.github.suppennudel.decklists.playingmtg;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import io.github.suppennudel.decklists.GenericDeckInfo;

public class PlayingMtgDeckInfo extends GenericDeckInfo {

	public enum Tier {
		S, A, B, C, D, OFF_META;
	}
	private Tier tier;

	private String thumbSource;

	public PlayingMtgDeckInfo(String name) {
		super(name);
	}

	public void setTier(Tier tier) {
		this.tier = tier;
	}

	public Tier getTier() {
		return tier;
	}

	public PlayingMtgDeckInfo(File file) throws IOException {
		this(file.getName());
		parse(file);
	}

	public void setThumbSource(String thumbSource) {
		this.thumbSource = thumbSource;
	}
	public String getThumbSource() {
		return thumbSource;
	}

	@Override
	public void parse(File file) throws IOException {
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
						mapToPutIn = getMain();
						break;
					case SIDEBOARD:
						mapToPutIn = getSide();
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

}
