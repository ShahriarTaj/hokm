package com.taj.shah.hokm.com.taj.shah.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.models.auth.In;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MatchSet {
    public boolean cardsHaveBeenDealt = false;
    public String hakem;
    public String hokm;
    public Player winningPlayer = null;
    public Player whoseTurn = null;
    public Map<String, Card> thrownCards = new LinkedHashMap<>();
    public boolean isSetOver = false;

    @JsonIgnore
    public Match parent;

    public String getHakem() {
        return hakem;
    }

    public void setHakem(String hakem) {
        this.hakem = hakem;
    }

    public String getHokm() {
        return hokm;
    }

    public void setHokm(String hokm) {
        this.hokm = hokm;
    }


    public void dealNewHand() throws Exception {
        List<Card> cards = Card.deal();
        int i = 0;
        for (Player player : parent.players) {
            player.hand.cards = new LinkedList<>();
            player.hand.cards.addAll(cards.subList(i * 13, (i + 1) * 13));
            i++;
            //System.out.println(player);
        }

    }

    public Player getPlayerWhoHasTheThrownCard(Card card) {
        // we have a map of player name and card.
        //go threw all the keys (playername) to see if the card is the card he threw
        Player theGuyWithTheCard = null;
        for (String playerNameWhoThrewTheCard : thrownCards.keySet()){
            Card x = thrownCards.get(playerNameWhoThrewTheCard);
            if (x!= null){
                if ( x.abbrev.equalsIgnoreCase(card.abbrev)){
                    theGuyWithTheCard = parent.getPlayer(playerNameWhoThrewTheCard);
                    break;
                }
            }
        }
        return theGuyWithTheCard;
    }

    @Override
    public String toString() {
        return "MatchSet{" +
                "cardsHaveBeenDealt=" + cardsHaveBeenDealt +
                ", hakem='" + hakem + '\'' +
                ", hokm='" + hokm + '\'' +
                ", winningPlayer=" + winningPlayer +
                ", whoseTurn=" + whoseTurn +
                ", thrownCards=" + thrownCards +
                ", isSetOver=" + isSetOver +
                ", parent=" + parent +
                '}';
    }
}
