package org.ea;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HearthDeck {
    public static void main(String[] args) {

        Map<String, Long> ownedCards = new HashMap<String, Long>();

        try {
            JSONObject jsonObject = (JSONObject) JSONValue.parse(new FileReader("db.json"));
            JSONArray arr = (JSONArray) jsonObject.get("cards");
            for(Object obj : arr) {
                JSONObject card = (JSONObject) obj;
                if(card.get("owned") != null && (Long)card.get("owned") > 0) {
                    ownedCards.put((String)card.get("name"), (Long)card.get("owned"));
                }
            }

            jsonObject = (JSONObject) JSONValue.parse(new FileReader("decks.json"));
            arr = (JSONArray) jsonObject.get("decks");

            JSONArray newDecks = new JSONArray();

            int decksCount = 0;
            for(Object obj : arr) {
                JSONObject deck = (JSONObject) obj;
                JSONArray arr2 = (JSONArray) deck.get("cards");
                int numCards = 0;
                List<String> seen = new ArrayList<String>();
                for(Object obj2 : arr2) {
                    JSONObject card = (JSONObject) obj2;
                    String name = (String)card.get("name");
                    Long amount = (Long)card.get("amount");

                    if(seen.contains(name)) continue;
                    seen.add(name);

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
                    decksCount++;

                    newDecks.add(deck);

                    if(decksCount == -1) {
                        System.out.println(deck.get("name") + "(" + deck.get("class") + ") = "+numCards);
                        for(Object obj2 : arr2) {
                            JSONObject card = (JSONObject) obj2;
                            System.out.println(card.get("name") + " = "+card.get("amount"));
                        }
                        System.exit(0);
                    }
                }
//                System.out.println(numCards + ": " + deck.get("name"));
            }

            JSONObject outputJSON = new JSONObject();
            outputJSON.put("decks", newDecks);
            try (FileWriter file = new FileWriter("founddecks.json")) {
                file.write(outputJSON.toJSONString());
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
