package com.commercepal.apiservice.products.ot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

@Slf4j
public class OTProductDescriptionParser {

  OTProductDescriptionParser() {
  }

  public static List<String> parseProductDescription(final String htmlDescription) {
    try {
      final Document doc = Jsoup.parse(htmlDescription);

      // Step 1: Try extracting basic content (paragraphs, simple divs, etc.)
      final List<String> basicContent = extractBasicContent(doc);
      if (!basicContent.isEmpty()) {
        return basicContent;
      }

      // Step 2: Fallback to structured content extraction if basic content is empty
      return extractStructuredContent(doc);

    } catch (Exception e) {
      log.error("Failed to parse product description", e);
      return new ArrayList<>();
    }
  }

  private static List<String> extractBasicContent(Document doc) {
    List<String> paragraphs = new ArrayList<>();
    Elements paraElements = doc.select("p, div:not(:has(*))");

    // Use a Set to avoid duplicates
    Set<String> uniqueTexts = new HashSet<>();

    for (Element para : paraElements) {
      String text = para.text().trim();

      // Remove bullet-like leading characters and extra symbols
      text = text.replaceAll("^[•\\-\\*\\u2022\\s]+",
          ""); // removes bullets, dashes, stars at start

      // Extra cleanup: normalize spaces
      text = text.replaceAll("\\s+", " ").trim();

      // Skip too-short or meaningless strings
      if (text.length() > 1 && uniqueTexts.add(text)) {
        paragraphs.add(text);
      }
    }

    return paragraphs;
  }

  private static List<String> extractStructuredContent(Document doc) {
    List<String> features = new ArrayList<>();

    // Look for common patterns in product descriptions
    Elements allElements = doc.select("*");

    // Use a Set to avoid duplicates
    Set<String> uniqueTexts = new HashSet<>();

    for (Element element : allElements) {
      String text = element.text().trim();

      // Remove bullet-like leading characters and extra symbols
      text = text.replaceAll("^[•\\-\\*\\u2022\\s]+",
          ""); // removes bullets, dashes, stars at start

      // Extra cleanup: normalize spaces
      text = text.replaceAll("\\s+", " ").trim();

      // Feature descriptions (medium length text)
      // if (text.length() > 20 && text.length() < 300 && uniqueTexts.add(text)) {
      // features.add(text);
      // }

      if (text.length() > 1 && uniqueTexts.add(text)) {
        features.add(text);
      }
    }

    return features;
  }
}
