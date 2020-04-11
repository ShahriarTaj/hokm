package com.taj.shah.hokm;

import com.taj.shah.hokm.com.taj.shah.model.*;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import sun.misc.BASE64Encoder;

import javax.annotation.PostConstruct;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class Hokm2Controller {

    Match match = new Match();
    MatchSet currentSet;
    Random random;

    @Autowired
    BlockingQueue<String> sseQueue;

    final Integer numberOfHandsToWinTheSet = 7;

    @PostConstruct
    public void init() throws Exception {

        Velocity.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        Velocity.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        Velocity.init();
        initializeMatch();
        random = new Random();
        random.setSeed(System.currentTimeMillis());
    }

    private void initializeMatch() throws Exception {
        match.sets = new LinkedList<>();
        if (currentSet == null) {
            currentSet = new MatchSet();
            match.sets.add(currentSet);
            currentSet.parent = match;
        }
        currentSet.hokm = null;
        currentSet.hakem = null;
        currentSet.cardsHaveBeenDealt = false;
        currentSet.winningPlayer = null;
        currentSet.thrownCards = Collections.synchronizedMap(new LinkedHashMap<>());
        currentSet.isSetOver = false;
        match.teamBscore = 0;
        match.teamAscore = 0;
//        match.teamBoverallScore = 0;
//        match.teamAoverallScore = 0;
        currentSet.dealNewHand();
    }


    @GetMapping("/newSet")
    public String startASet() throws Exception {
        Player winp = currentSet.whoseTurn == null ? match.getPlayer(currentSet.hakem) : currentSet.whoseTurn;
        currentSet.winningPlayer = null;
        synchronized (this) {
            if (currentSet.isSetOver) {
                initializeMatch();
            } else {
                currentSet.thrownCards = Collections.synchronizedMap(new LinkedHashMap<>());
                currentSet.isSetOver = false;
            }
        }
        //System.out.println(currentSet);
        sseQueue.add("THREW");
        return winp.playerNumber.toString();
    }

    @GetMapping("/throwCard/{card}/")
    public ResponseEntity<String> throwInCard(@PathVariable("card") String cardAbbrev,
                                              HttpServletRequest request) throws Exception {
        String playerName = getCookieValue(request, "player");
        Card card = new Card();
        card.abbrev = cardAbbrev;
        Player player = null;
        synchronized (this) {
            if (currentSet.thrownCards.containsKey(playerName)) {
                throw new Exception(playerName + " عمو، چند تا کارت میخوای بندازی؟ ");
            }
            currentSet.thrownCards.put(playerName, card);
            player = match.getPlayer(playerName);
            player.removeCard(card);
            Integer x = player.playerNumber;
            Integer y = getNextPlayerAfter(x);
            currentSet.whoseTurn = match.getPlayer(y);
            sseQueue.add("THREW");
        }
        return new ResponseEntity<String>(getCardsHtml(player.playerNumber, request), HttpStatus.OK);
    }

    private Integer getNextPlayerAfter(Integer x) {
        Integer y = 0;
        switch (x) {
            case 1:
                y = 3;
                break;
            case 3:
                y = 4;
                break;
            case 4:
                y = 2;
                break;
            case 2:
                y = 1;
                break;
        }
        return y;
    }


    @GetMapping("/newgame")
    public Match freshGame() throws Exception {
        if (match.getPlayerCount() != 4) {
            throw new Exception("There must be exactly four players to start the game");
        }
        initializeMatch();
        //getNewSet();
        sseQueue.add("THREW");
        return match;
    }

    @PostMapping("/login")
    public RedirectView login(HttpServletRequest request, HttpServletResponse response) throws Exception {


        String thisPlayer = request.getParameter("uname");
        Cookie cookie = new Cookie("player", new String(Base64.getEncoder().encode(thisPlayer.getBytes())));
        cookie.setMaxAge(60 * 60 * 10);
        cookie.setPath("/");
        response.addCookie(cookie);

        addPlayer(thisPlayer);
        addPlayer("Adam Joon");
        addPlayer("حمید");
        addPlayer("Hossain");

        synchronized (this) {
            if (match.getPlayerCount() == 4 && !currentSet.cardsHaveBeenDealt) {
                int i = 0;
                for (Player p : match.players) {
                    p.playerNumber = i + 1;
                    switch (i) {
                        case 0:
                        case 3:
                            p.teamName = "Team A";
                            break;
                        default:
                            p.teamName = "Team B";
                    }
                    i++;
                }
                freshGame();
                chooseHakem();
                currentSet.cardsHaveBeenDealt = true;
            }
        }
//        for (Player p : match.getPlayers()) {
//            System.out.println(p);
//        }

        return new

                RedirectView("/HokmTable.html");

    }


    @GetMapping("/setWhoseTurn/{playerName}/")
    public void setWhoseTurn(@PathVariable("playerName") String playerName) {
        currentSet.whoseTurn = match.getPlayer(playerName);
        sseQueue.add("THREW");
    }


//    @GetMapping("/dealCards")
//    public List<Player> getNewSet() throws Exception {
//        MatchSet set = new MatchSet();
//        set.parent = match;
//        match.sets.add(set);
//        currentSet.hokm = null;
//        currentSet.cardsHaveBeenDealt = false;
//        set.dealNewHand();
//        sseQueue.add("THREW");
//        return match.players;
//    }

    @GetMapping("/players")
    public List<Player> getListOfPlayers() {
        return match.getPlayers();
    }


    @GetMapping("/setHokm/{hokm}/")
    public String setHokm(@PathVariable("hokm") String hokm) {
        String changedHokm;
        if (hokm.length() > 1) {
            changedHokm = hokm.substring(1);
        } else {
            changedHokm = hokm;
        }
        currentSet.hokm = changedHokm;
        sseQueue.add("HOKM");
        return currentSet.hokm;
    }

    //https://www.linkedin.com/in/shahriartaj/
    @GetMapping("/scores")
    public String getscores(HttpServletRequest request) throws Exception {
        String cookiedPlayer = getCookieValue(request, "player");
        StringBuilder sb = new StringBuilder();
        sb.append("<table style=\"width:100%\" border-spacing= '5px'>");
        sb.append("<th>");
        if (currentSet.hokm != null) {
            sb.append("Hokm: ");
            switch (currentSet.hokm) {
                case "H":
                    sb.append("&heartsuit;");
                    break;
                case "S":
                    sb.append("&spadesuit;");
                    break;
                case "D":
                    sb.append("&diamondsuit;");
                    break;
                case "C":
                    sb.append("&clubsuit;");
                    break;
            }
            sb.append("</th>");
        }
        synchronized (this) {
            Player hakem = match.getPlayer(currentSet.hakem);
            if (currentSet.thrownCards.keySet().size() == 4 && !currentSet.isSetOver) {
                Card winningCard = Card.findWinninCard(currentSet.thrownCards.values(), currentSet.hokm);
                boolean incrementScores = (currentSet.winningPlayer == null);
                currentSet.winningPlayer = currentSet.getPlayerWhoHasTheThrownCard(winningCard);
                if (incrementScores) {
                    if (currentSet.winningPlayer.getTeamName().equals("Team A")) {
                        match.teamAscore++;
                        if (match.teamAscore >= numberOfHandsToWinTheSet) {
                            match.teamAoverallScore++;
                            currentSet.isSetOver = true;
                        }
                    } else {
                        match.teamBscore++;
                        if (match.teamBscore >= numberOfHandsToWinTheSet) {
                            match.teamBoverallScore++;
                            currentSet.isSetOver = true;
                        }
                    }
                }
                //sb.append(paintAMiddleCard(template, currentSet.winningPlayer.name));

            }
            if (currentSet.winningPlayer != null) {
                sb.append("<th>");
                sb.append("The winner is: " + currentSet.winningPlayer.name);
                sb.append("</th>");
                currentSet.whoseTurn = currentSet.winningPlayer;

            }
            if (currentSet.isSetOver) {
                sb.append("<th>");
                if (match.teamBscore >= numberOfHandsToWinTheSet) {
                    sb.append("Team B wins");
                } else {
                    sb.append("Team A wins");
                }
                sb.append("</th>");

            }
            if ( hakem != null && currentSet.winningPlayer != null) {
                if (!hakem.getTeamName().equalsIgnoreCase(currentSet.winningPlayer.teamName)) {
                    Integer whoNext = getNextPlayerAfter(hakem.playerNumber);
                    Player next = match.getPlayer(whoNext);
                    currentSet.whoseTurn = next;
                    setHakem( next.name );
                } else {
                    //System.out.println("Winner is " + currentSet.winningPlayer);
                    //System.out.println("Hakem remains " + currentSet.hakem);
                }
            }
            if (currentSet.whoseTurn != null && currentSet.whoseTurn.name.equalsIgnoreCase(cookiedPlayer) && currentSet.thrownCards.keySet().size() == 4) {
                sb.append("<th>");
                sb.append("<input type='button' value='Next' onClick='newSet()'>");
                sb.append("</th>");
            }
            sb.append("<th>");
            sb.append("Team A (this hand)" + match.teamAscore);
            sb.append("</th>");
            sb.append("<th>");
            sb.append("Team B (this hand) " + match.teamBscore);
            sb.append("</th>");
            sb.append("<th>");
            sb.append("Team A (overall)" + match.teamAoverallScore);
            sb.append("</th>");
            sb.append("<th>");
            sb.append("Team B (overall) " + match.teamBoverallScore);
            sb.append("</th>");
            sb.append("</table>");
        }
        return sb.toString();
    }


    @GetMapping("/middleTable")
    public String getMiddleTable(HttpServletRequest request) throws Exception {
        String thisPlayer = getCookieValue(request, "player");
        StringBuilder sb = new StringBuilder();
        synchronized (this) {
            Template template = Velocity.getTemplate("/static/vm/middleTable.vm");
            sb.append("            <div class=\"playingCards fourColours faceImages\">\n");
            sb.append("<ul class='table'>");
            for (String playerName : currentSet.thrownCards.keySet()) {
                sb.append(paintAMiddleCard(template, playerName));

            }
            sb.append("</ul>");
            sb.append("</div>");
        }

        return sb.toString();
    }

    private String paintAMiddleCard(Template template, String playerName) throws Exception {
        Card card = currentSet.thrownCards.get(playerName);
        String rank = card.abbrev.startsWith("T") ? "10" : card.abbrev.substring(0, 1);
        VelocityContext context = new VelocityContext();
        context.put("playerName", playerName);
        context.put("cardId", card.abbrev);
        context.put("cardIcon", Card.lookupCardIcon(card.abbrev.substring(1)));
        context.put("rank", rank);
        context.put("rankLowerCase", rank.toLowerCase());
        context.put("suitAbbreviation", Card.lookupCardSuitAbbreviation(card.abbrev.substring(1)));
        StringWriter sw = new StringWriter();
        template.merge(context, sw);
        return sw.toString();
    }


    @GetMapping("/setHakem/{hakem}/")
    public void setHakem(@PathVariable("hakem") String hakem) {
        synchronized (this) {
            if (currentSet.hakem != null) {
                match.getPlayer(currentSet.hakem).isHakem = false;
            }
            for (Player p : match.players) {
                p.isHakem = false;
            }
            currentSet.hakem = hakem;
            match.getPlayer(hakem).isHakem = true;
            currentSet.whoseTurn = match.getPlayer(hakem);
        }
        sseQueue.add("HAKEM");
    }

    @GetMapping("/hokm")
    public String getHokm() {
        return currentSet.hokm;
    }

    @GetMapping("/hakem")
    public String getHakem() {
        return currentSet.hakem;
    }

    @GetMapping("/removeCardFromMiddle/{card}/")
    public void removeCardFromMiddle(@PathVariable("card") String cardAbbrev, HttpServletRequest request) throws Exception {
        synchronized (this) {
            String cookiedPlayer = getCookieValue(request, "player");
            Optional<Card> x = currentSet.thrownCards.values().stream().filter(c -> c.abbrev.equals(cardAbbrev)).findFirst();
            if (x.isPresent()) {
                Card card = x.get();
                Player playerWhoWantsToRemoveTheCard = currentSet.getPlayerWhoHasTheThrownCard(card);
                if (!playerWhoWantsToRemoveTheCard.name.equalsIgnoreCase(cookiedPlayer)) {
                    throw new Exception("  عمو، کارت یکی دیگه رو ور ندار  ");
                }
                if (!playerWhoWantsToRemoveTheCard.hand.cards.contains(card)) {
                    playerWhoWantsToRemoveTheCard.hand.cards.add(card);
                    currentSet.thrownCards.remove(playerWhoWantsToRemoveTheCard.name);
                    currentSet.whoseTurn = playerWhoWantsToRemoveTheCard;
                    sseQueue.add("THREW");
                }
                return;
            }
        }
    }


    @GetMapping("/chooseHakem")
    public String chooseHakem() {
        if (currentSet.hakem == null && match.getPlayerCount() == 4) {
            int hak = random.nextInt(3);
            Player h = match.getPlayers().get(hak);
            setHakem(h.name);
        }
        return currentSet.hakem;
    }

    @GetMapping("/getCardsHtml/{playerNumber}")
    public String getCardsHtml(@PathVariable("playerNumber") Integer playerNumber, HttpServletRequest request) throws Exception {
        StringBuilder sb = new StringBuilder();
        synchronized (this) {
            String cookiedPlayer = getCookieValue(request, "player");

            Player player = match.getPlayer(playerNumber);
            if (player == null) {
                return "";
            }
            String playerName = player.name;


            Template template = Velocity.getTemplate("/static/vm/cute.vm");
            List<Card> cards;
            sb.append(playerName + " " + player.teamName + " " + playerNumber);
            if (player.isHakem) {
                sb.append(" (Hakem) ");
            }
            if (cookiedPlayer.equalsIgnoreCase(playerName)) {
                sb.append("***");
            }


            sb.append("            <div class=\"playingCards fourColours faceImages rotateHand\" >");

            if (currentSet.hokm == null) {
                cards = match.getPlayer(playerName).hand.firstHand();
            } else {
                cards = match.getPlayer(playerName).hand.allHand(currentSet.hokm);
            }
            boolean yourTurn = false;
            if (cookiedPlayer.equalsIgnoreCase(playerName)) {
                sb.append("                <ul class=\"table\" >");
            } else {
                sb.append("                <ul class=\"hand\" >");
            }
            if (currentSet.whoseTurn != null && currentSet.whoseTurn.name.equalsIgnoreCase(playerName)) {
                yourTurn = true;
            }
            for (Card card : cards) {
                String rank = card.abbrev.startsWith("T") ? "10" : card.abbrev.substring(0, 1);
                VelocityContext context = new VelocityContext();
//                if (currentSet.hokm != null) {
//                    context.put("hokm", currentSet.hokm);
//                }
//                context.put("isHakem", player.isHakem);
                context.put("yourturn", yourTurn);
                context.put("isCurrentPlayer", cookiedPlayer.equalsIgnoreCase(playerName));
                context.put("cardId", card.abbrev);
                context.put("cardIcon", Card.lookupCardIcon(card.abbrev.substring(1)));
                context.put("rank", rank);
                context.put("rankLowerCase", rank.toLowerCase());
                context.put("suitAbbreviation", Card.lookupCardSuitAbbreviation(card.abbrev.substring(1)));
                StringWriter sw = new StringWriter();
                template.merge(context, sw);
                sb.append(sw.toString());
            }

            sb.append("                </ul>\n" +
                    "            </div>\n");
        }
        return sb.toString();
    }

    private final Object addPlayerLock = new Object();

    @GetMapping("/addPlayer/{playerName}/")
    public Player addPlayer(@PathVariable("playerName") String playerName) throws Exception {
        Player player = null;
        synchronized (this) {
            int playerCount = match.getPlayerCount();
            if (playerCount > 4) {
                return null;
            }
            player = match.getPlayer(playerName);
            if (player == null) {
                player = new Player();
                player.name = playerName;
                match.getPlayers().add(player);
            }
            return player;
        }
    }

    @GetMapping("/playerCount")
    public Integer getPlayerCount() {
        return match.getPlayerCount();
    }

    @GetMapping("/playerNames")
    public List<String> getPlayers() {
        return match.getPlayers().stream().map(x -> x.name).collect(Collectors.toList());
    }

    private String getCookieValue(HttpServletRequest request, String name) throws UnsupportedEncodingException {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        String value = Arrays.stream(cookies)
                .filter(c -> c.getName().equalsIgnoreCase(name))
                .map(c -> c.getValue())
                .collect(Collectors.joining());
        return new String(Base64.getDecoder().decode(value.getBytes()));
    }

}