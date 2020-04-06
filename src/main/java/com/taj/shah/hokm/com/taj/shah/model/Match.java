package com.taj.shah.hokm.com.taj.shah.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class Match {
    public List<MatchSet> sets = new LinkedList<>();
    public List<Player> players = new LinkedList<>();
    public Integer teamAscore = new Integer(0);
    public Integer teamBscore = new Integer(0);
    public Integer teamAoverallScore = 0;
    public Integer teamBoverallScore = 0;

    public Integer getTeamAscore() {
        return teamAscore;
    }

    public void setTeamAscore(Integer teamAscore) {
        this.teamAscore = teamAscore;
    }

    public Integer getTeamBscore() {
        return teamBscore;
    }

    public void setTeamBscore(Integer teamBscore) {
        this.teamBscore = teamBscore;
    }

    public List<MatchSet> getSets() {
        return sets;
    }

    public void setSets(List<MatchSet> sets) {
        this.sets = sets;
    }


    public Integer getPlayerCount() {
        return players.size();
    }

    public Player getPlayer(String playerName) {
        Player p = null;
        try {
            p = players.stream().filter(x -> x.name.equalsIgnoreCase(playerName)).findFirst().get();
        } catch (Exception e) { /* so what */}
        return p;
    }

    public Player getPlayer(Integer playerNumber) {
        Player p = null;
        try {
            p = players.stream().filter(x -> x.playerNumber.equals(playerNumber)).findFirst().get();
        } catch (Exception e) { /* so what */}
        return p;
    }

    public void dealNewHand() throws Exception {
        for (Player player : players) {
            player.hand.cards = Card.deal();
        }

    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public Player getPlayerHoldingCard(Card card) {
        Player playerWithTheCard = null;
        for (Player player : players) {
            Optional<Card> x = player.hand.cards.stream().filter(c -> c.abbrev.equalsIgnoreCase(card.abbrev)).findFirst();
            if (x.isPresent()){
                playerWithTheCard = player;

            }
        }
        return playerWithTheCard;
    }
}
