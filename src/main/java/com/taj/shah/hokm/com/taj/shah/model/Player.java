package com.taj.shah.hokm.com.taj.shah.model;

public class Player {

    public  String name;
    public  boolean isHakem = false;
    public Hand hand = new Hand();
    public String teamName;
    public Integer playerNumber;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isHakem() {
        return isHakem;
    }

    public void setHakem(boolean hakem) {
        isHakem = hakem;
    }

    public Hand getHand() {
        return hand;
    }

    public void setHand(Hand hand) {
        this.hand = hand;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public void removeCard(Card card) {
        //System.out.println("Size before remove" + hand.cards.size());
        hand.cards.remove(card);
        //System.out.println("Size after remove" + hand.cards.size());
    }

    @Override
    public String toString() {
        return "Player{" +
                "name='" + name + '\'' +
                ", isHakem=" + isHakem +
                ", hand=" + hand +
                ", teamName='" + teamName + '\'' +
                ", playerNumber=" + playerNumber +
                '}';
    }
}
