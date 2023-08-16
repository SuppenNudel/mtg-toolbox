package suppennudel.playingmtg;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import suppennudel.DeckList;
import suppennudel.DeckList.Tier;

public class FormatRelevancePlayingPioneer {

	private static final String PP_TIER_LIST_URL = "https://playingpioneer.com/pioneer-tier-list/";
	private static final String PP_ARCHIVE_URL = "https://playingmtg.com/pioneer/deck-archive/";
	private static final Pattern pattern = Pattern
			.compile("(?<quantity>\\d+) (?<cardname>[^\\/\\n]+[^\\s\\/])( \\/\\/ (.*))?");

	public static List<DeckList> getDeckLists() throws MalformedURLException, IOException {
		List<DeckList> overview = getOverview();
		List<DeckList> deckLists = new ArrayList<>();
		for (DeckList deck : overview) {
			fillDeckList(deck);
		}
		return deckLists;
	}

	public static List<DeckList> getOverview() throws MalformedURLException, IOException {
		List<DeckList> overview = new ArrayList<>();

		overview.addAll(overviewFromUrl(PP_TIER_LIST_URL));
		overview.addAll(overviewFromUrl(PP_ARCHIVE_URL));

		return overview;
	}

	private static List<DeckList> overviewFromUrl(String ppUrl) throws MalformedURLException, IOException {
		List<DeckList> overview = new ArrayList<>();
		Document docArchive = Jsoup.parse(new URL(ppUrl), 10000);
		Elements archiveArticles = docArchive.select("article.entry-card");
		for (Element article : archiveArticles) {
			Elements link = article.select(".entry-title > a");
			String url = link.attr("href");
			String text = link.text();
			Elements image = article.select("img");
			String thumbSource = image.attr("src");

			Tier tier;
			if(ppUrl.equals(PP_TIER_LIST_URL)) {
				Element row = article.parent().parent().parent().parent();
				Elements heading = row.select("h2.wp-block-heading");
				String tierHeader = heading.text();
				String replace = tierHeader.replace(" TIER", "");
				tier = Tier.valueOf(replace);
			} else if(ppUrl.equals(PP_ARCHIVE_URL)) {
				tier = Tier.OFF_META;
			} else {
				throw new RuntimeException("URL not handled: "+ppUrl);
			}
			DeckList deckList = new DeckList(text, url, tier);
			deckList.setThumbSource(thumbSource);
			overview.add(deckList);
		}
		return overview;
	}

	private static final LocalDate today = LocalDate.now();

	public static boolean fillDeckList(DeckList deckList) throws MalformedURLException, IOException {
		File deckListCache = deckListCache(deckList);
		if(deckListCache.exists()) {
			deckList.loadFromFile(deckListCache);
			return true;
		}

		System.out.println("filling deckList " + deckList);
		Document doc = Jsoup.parse(new URL(deckList.getUrl()), 10000);
		Elements iframes = doc.select("iframe");
		String iframeSource = iframes.attr("src");
		Elements deckBlock = doc.select(".deck-block");
		if(iframeSource.startsWith("https://aetherhub.com/")) {
			// is aetherhub deck
			Document aetherhubDoc = Jsoup.parse(new URL(iframeSource), 10000);
			Elements table = aetherhubDoc.select("table > tbody > tr > td > div > a > b");
			for (Element cardElement : table) {
				Matcher matcher = pattern.matcher(cardElement.text());
				if (matcher.matches()) {
					int amount = Integer.parseInt(matcher.group("quantity"));
					String cardName = matcher.group("cardname");
					deckList.getMain().merge(cardName, amount, (t, u) -> t + u);
				} else {
					System.err.println("NO MATCH: " + cardElement.text());
				}
			}
		} else if(!deckBlock.isEmpty()) {
			// native
			Elements cards = deckBlock.select("div.card");
			for(Element cardElement : cards) {
				int amount = Integer.parseInt(cardElement.attr("data-quantity"));
				String cardName = cardElement.attr("data-name");
				deckList.getMain().merge(cardName, amount, (t, u) -> t + u);
			}
		}
		System.out.println("Done filling deckList " + deckList);
		writeDeckListToFile(deckList);
		return true;
	}

	private static void writeDeckListToFile(DeckList deckList) throws IOException {
		StringBuilder sb = new StringBuilder();
		sb.append("Deck" + System.lineSeparator());
		deckList.getMain().forEach((t, u) -> sb.append(u + " " + t + System.lineSeparator()));
		sb.append(System.lineSeparator());
		sb.append("Sideboard" + System.lineSeparator());
		deckList.getSide().forEach((t, u) -> sb.append(u + " " + t + System.lineSeparator()));

		FileUtils.writeStringToFile(deckListCache(deckList), sb.toString(),
				StandardCharsets.UTF_8, false);
	}

	private static File deckListCache(DeckList deckList) {
		return new File("decklists/" + today + "/" + deckList.getName());
	}

}
