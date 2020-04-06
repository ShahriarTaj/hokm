package com.taj.shah.hokm.com.taj.shah.model;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class Hand {
    public List<Card> cards = new LinkedList<>();

    public List<Card> getCards() {
        return cards;
    }

    public void setCards(List<Card> cards) {
        this.cards = cards;
    }

    public List<Card> firstHand() {
        return
                cards.stream()
                        .limit(5)
                        .sorted(Comparator.comparing(Card::sortString))
                        .collect(Collectors.toList());
    }

    public List<Card> allHand( String trumpCard){
        List<Card> nonTrumps =
                cards.stream()
                        .filter(x -> !x.abbrev.substring(1).equalsIgnoreCase(trumpCard))
                        .sorted(Comparator.comparing(Card::sortString))
                        .collect(Collectors.toList());
        List<Card> trumps =
                cards.stream()
                        .filter(x -> x.abbrev.substring(1).equalsIgnoreCase(trumpCard))
                        .sorted(Comparator.comparing(Card::sortString))
                        .collect(Collectors.toList());

        List<Card> allCards = new LinkedList<>();
        allCards.addAll(nonTrumps);
        allCards.addAll(trumps);
        return allCards;
    }

    @Override
    public String toString() {
        return "Hand{" +
                "cards=" + cards +
                '}';
    }
}
