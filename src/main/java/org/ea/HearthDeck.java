package org.ea;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.*;

public class HearthDeck {

    public static String getHash(JSONArray cards) {
        List<String> cardStrings = new ArrayList<>();
        for(Object o : cards) {
            JSONObject card = (JSONObject)o;
            cardStrings.add(card.get("owned")+""+card.get("name").toString().toLowerCase());
        }
        Collections.sort(cardStrings);

        try {
            MessageDigest md = MessageDigest.getInstance("SHA1");
            for (String s : cardStrings) md.update(s.getBytes());
            return Base64.getEncoder().encodeToString(md.digest());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void main(String[] args) {

        Map<String, Long> ownedCards = new HashMap<String, Long>();

        try {
            JSONObject dataBaseJSON = (JSONObject) JSONValue.parse(new FileReader("db.json"));
            JSONArray arr = (JSONArray) dataBaseJSON.get("cards");
            for(Object obj : arr) {
                JSONObject card = (JSONObject) obj;
                if(card.get("owned") != null && (Long)card.get("owned") > 0) {
                    ownedCards.put((String)card.get("name"), (Long)card.get("owned"));
                }
            }

            List<String> seenKeys = new ArrayList<>();
            JSONArray oldDecks = (JSONArray) dataBaseJSON.get("decks");
            if(oldDecks == null) oldDecks = new JSONArray();
            for (Object o : oldDecks) {
                JSONObject oldDeck = (JSONObject) o;
                seenKeys.add((String) oldDeck.get("cardSHA"));
            }

            JSONObject jsonObject = (JSONObject) JSONValue.parse(new FileReader("decks.json"));
            arr = (JSONArray) jsonObject.get("decks");

            int decksCount = 0;
            for(Object obj : arr) {
                JSONArray cleanListOfCards = new JSONArray();
                List<String> seen = new ArrayList<>();

                JSONObject deck = (JSONObject) obj;
                JSONArray arr2 = (JSONArray) deck.get("cards");
                int numCards = 0;
                for(Object obj2 : arr2) {
                    JSONObject card = (JSONObject) obj2;
                    String name = (String)card.get("name");
                    Long amount = (Long)card.get("amount");

                    if(seen.contains(name)) continue;
                    seen.add(name);

                    cleanListOfCards.add(card);

                    Long owned = ownedCards.get(name);
                    if(owned != null) {
                        if(owned > amount) {
                            numCards += amount;
                        } else {
                            numCards += owned;
                        }
                    }
                }

                if(numCards > 29) {
                    String key = getHash(cleanListOfCards);
                    if(seenKeys.contains(key)) continue;
                    decksCount++;
                    deck.put("cards", cleanListOfCards);
                    deck.put("cardSHA", key);
                    seenKeys.add(key);

                    System.out.println(deck.get("cardSHA"));

                    oldDecks.add(deck);

                    if(decksCount == -1) {
                        System.out.println(deck.get("name") + "(" + deck.get("class") + ") = "+numCards);
                        for(Object obj2 : cleanListOfCards) {
                            JSONObject card = (JSONObject) obj2;
                            System.out.println(card.get("name") + " = "+card.get("amount"));
                        }
                        System.exit(0);
                    }
                }
            }

            dataBaseJSON.put("decks", oldDecks);
            try (FileWriter file = new FileWriter("db.json")) {
                file.write(dataBaseJSON.toJSONString());
                file.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println(decksCount);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
