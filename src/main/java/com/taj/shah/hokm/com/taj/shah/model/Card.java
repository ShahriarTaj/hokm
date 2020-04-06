package com.taj.shah.hokm.com.taj.shah.model;

import java.util.*;

public class Card {


    public static Card findWinninCard(Collection<Card> values, String hokm) throws Exception {
        //first card is the one that determines the suits
        if (values.size() != 4) {
            throw new Exception("There must be exactly 4 cards in order to determine the winning card");
        }
        Card winningCard = null;
        String winningSuit = "";
        String suit = "";
        int i = 0;
        for (Card card : values) {
            if (i++ == 0) {
                winningCard = card;
                winningSuit = card.abbrev.substring(1);
                continue;
            } else {
                suit = card.abbrev.substring(1);
            }
            boolean isCurrentCardRelevant = suit.equalsIgnoreCase(winningSuit) || suit.equalsIgnoreCase(hokm);
            if (!isCurrentCardRelevant){
                continue;
            }
            boolean isWinningCardSmallerRanked = winningCard.sortString().compareTo(card.sortString()) < 0;
            boolean isWinningCardHokm = winningSuit.equals(hokm);
            boolean isCurrentCardHokm = suit.equalsIgnoreCase(hokm);
            if (!isWinningCardHokm && isCurrentCardHokm){
                winningCard = card;
                winningSuit = hokm;
                continue;
            }
            //they are both the same suit
            if (isWinningCardSmallerRanked){
                winningCard = card;
            }
        }


        return winningCard;

    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Card card = (Card) o;
        return Objects.equals(abbrev, card.abbrev);
    }

    @Override
    public int hashCode() {
        return Objects.hash(abbrev);
    }

    static Map<String, String> sortOrder = new TreeMap<>();

    static {
        String[] suit = {"C", "D", "H", "S"};
        String[] rank = {"2", "3", "4", "5", "6", "7", "8", "9", "T", "J",
                "Q", "K", "A"};
        int i = 0;
        for (String r : rank) {
            //System.out.println("Order of " + r + " is " + String.format("%02d",i));
            sortOrder.put(r, String.format("%02d", i++));
        }

    }

    public static String lookupCardIcon(String s) throws Exception {
        String cardSign = "";
        switch (s) {
            case "C":
                cardSign = "&clubsuit;";
                break;
            case "D":
                cardSign = "&diamondsuit;";
                break;
            case "H":
                cardSign = "&heartsuit;";
                break;
            case "S":
                cardSign = "&spadesuit;";
                break;
            default:
                throw new Exception("can not find sign for " + s);
        }


        return cardSign;
    }

    public static String lookupCardSuitAbbreviation(String s) throws Exception {
        String cardSuit = "";
        switch (s) {
            case "C":
                cardSuit = "clubs";
                break;
            case "D":
                cardSuit = "diams";
                break;
            case "H":
                cardSuit = "hearts";
                break;
            case "S":
                cardSuit = "spades";
                break;
            default:
                throw new Exception("can not find suit" + s);
        }


        return cardSuit;
    }

    public String abbrev;

    public String sortString() {
        String x = abbrev.substring(1) + sortOrder.get(abbrev.substring(0, 1));
        return x;
    }

    public String toString() {
        //The abbrev is set as say 5D
        return abbrev;
    }

    public static List<Card> deal() throws Exception {
        String[] x = dealraw();
        List<Card> cards = new LinkedList<>();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 13; j++) {
                Card card = new Card();
                card.abbrev = x[i * 13 + j];
                cards.add(card);
            }
        }
        return cards;
    }

    public static String[] dealraw() {
        int N = 4;  // Number of Hands to deal
        int GAME = 13;        // game is five card stud so deal 5 cards

        String[] suit = {"C", "D", "H", "S"};
        String[] rank = {"2", "3", "4", "5", "6", "7", "8", "9", "T", "J",
                "Q", "K", "A"};

        // avoid hardwired constants
        int SUITS = suit.length;
        int RANKS = rank.length;
        int DECK_SIZE = SUITS * RANKS;

        // initialize deck
        String[] deck = new String[DECK_SIZE];
        for (int i = 0; i < RANKS; i++) {
            for (int j = 0; j < SUITS; j++) {
                deck[SUITS * i + j] = rank[i] + suit[j];
            }
        }

        // shuffle deck, deal a hand
        // repeat N = Number of Hands times
        for (int hands = 0; hands < N; hands++) {

            // shuffle
            for (int i = 0; i < DECK_SIZE; i++) {
                int r = i + (int) (Math.random() * (DECK_SIZE - i));
                String t = deck[r];
                deck[r] = deck[i];
                deck[i] = t;
            }
        }
        return deck;
    }

    public static void main(String[] a) throws Exception {
        Map<String, Card> c = new LinkedHashMap<>();
        Card c1 = new Card();
        c1.abbrev = "2H";

        Card c2 = new Card();
        c2.abbrev = "2D";

        Card c3 = new Card();
        c3.abbrev = "TH";

        Card c4 = new Card();
        c4.abbrev = "KD";

        c.put("1", c1);
        c.put("2", c2);
        c.put("3", c3);
        c.put("4", c4);


        //System.out.println(Card.findWinninCard(c.values(), "S"));


    }

}