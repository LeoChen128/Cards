import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Point;
import java.util.ArrayList;
import java.awt.Font;
import java.awt.Color;

class DrawPanel extends JPanel implements MouseListener {

    private ArrayList<Card> hand;
    private ArrayList<Card> deck;

    // Rectangle object represents ........ a rectangle.
    private Rectangle button;

    private boolean gameWon;
    private boolean gameLost;

    public DrawPanel() {
        button = new Rectangle(147, 400, 160, 26);
        // represents the "GET NEW CARDS" button
        this.addMouseListener(this);
        deck = Card.buildDeck();
        hand = new ArrayList<>();
        dealNewHand();
        gameWon = false;
        gameLost = false;
    }

    public void replaceCard(int index) {
        if (!deck.isEmpty()) {
            int r = (int)(Math.random()*deck.size());
            Card newCard = deck.remove(r);
            hand.set(index, newCard);
        }
        checkGameStatus();
    }
    public void dealNewHand() {
        hand.clear();
        for (int i = 0; i < 9; i++) {
            if (deck.isEmpty()){
                break;
            }
            int r = (int)(Math.random()*deck.size());
            Card c = deck.remove(r);
            hand.add(c);
        }
        checkGameStatus();
    }

    public void checkGameStatus() {
        if (hand.isEmpty() && deck.isEmpty()) {
            gameWon = true;
            return;
        }

        boolean hasValidMove = checkForValidMoves();

        if (!hasValidMove && !deck.isEmpty()) {
            hasValidMove = true;
        }

        if (!hasValidMove) {
            gameLost = true;
        }
    }

    public boolean checkForValidMoves() {
        boolean hasValidPair = checkForValidPairs();
        boolean hasValidTrio = checkForValidTrios();
        return hasValidPair || hasValidTrio;
    }

    public boolean checkForValidPairs() {
        boolean foundPair = false;
        for (int i = 0; i < hand.size() && !foundPair; i++) {
            for (int j = i + 1; j < hand.size() && !foundPair; j++) {
                if (hand.get(i).isFaceUp() && hand.get(j).isFaceUp() &&
                        hand.get(i).getNumericalValue() + hand.get(j).getNumericalValue() == 11) {
                    foundPair = true;
                }
            }
        }
        return foundPair;
    }

    public boolean checkForValidTrios() {
        boolean hasJack = false;
        boolean hasQueen = false;
        boolean hasKing = false;
        for (Card card : hand) {
            if (card.isFaceUp()) {
                if (card.getValue().equals("J")){
                    hasJack = true;
                }
                if (card.getValue().equals("Q")){
                    hasQueen = true;
                }
                if (card.getValue().equals("K")){
                    hasKing = true;
                }
            }
        }
        return hasJack && hasQueen && hasKing;
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int cardWidth = 71;
        int cardHeight = 96;
        int x = 50;
        int y = 10;

        for (int i = 0; i < 9; i++) {
            if (i < hand.size()) {
                Card c = hand.get(i);
                if (c.getHighlight()) {
                    g.setColor(Color.YELLOW);
                    g.drawRect(x-2, y-2, cardWidth+4, cardHeight+4);
                }
                c.setRectangleLocation(x, y);
                g.drawImage(c.getImage(), x, y, null);
            }

            x += cardWidth + 10;
            if ((i+1) % 3 == 0) {
                x = 50;
                y += cardHeight + 10;
            }
        }

        // drawing the bottom button with a certain font
        g.setFont(new Font("Courier New", Font.BOLD, 20));
        g.drawString("GET NEW CARDS", 150, 420);
        g.drawRect((int)button.getX(), (int)button.getY(), (int)button.getWidth(), (int)button.getHeight());

        g.setColor(Color.BLACK);
        g.drawString("Cards left in deck: " + deck.size(), 150, 380);

        if (gameWon) {
            g.setColor(Color.GREEN);
            g.setFont(new Font("Courier New", Font.BOLD, 30));
            g.drawString("YOU WIN!", 180, 200);
        }
        else if (gameLost) {
            g.setColor(Color.RED);
            g.setFont(new Font("Courier New", Font.BOLD, 30));
            g.drawString("NO VALID MOVES!", 130, 200);
        }
    }

    public void mousePressed(MouseEvent e) {
        // gets cords
        Point clicked = e.getPoint();
        // left click
        if (e.getButton() == 1) {
            // takes in a point object and returns true/false whether a point is in the place
            // if "clicked" is inside the button rectangle
            // aka --> did you click the button?
            if (button.contains(clicked)) {
                dealNewHand();
                gameLost = false;
                return;
            }

            // go through each card
            // check if any of those were clicked on
            // if it was clicked, flip the card
            for (int i = 0; i < hand.size(); i++) {
                Rectangle box = hand.get(i).getCardBox();
                if (box.contains(clicked)) {
                    hand.get(i).flipCard();
                    hand.get(i).setHighlight(false);
                }
            }

            checkGameStatus();
        }

        //middle mutton is valued at '2'

        // right click
        if (e.getButton() == 3) {
            handleRightClick(clicked);
        }
    }

        public void handleRightClick(Point clicked) {
            for (int i = 0; i < hand.size(); i++) {
                Rectangle box = hand.get(i).getCardBox();
                if (box.contains(clicked)) {
                    if (hand.get(i).getHighlight()) {
                        replaceCard(i);
                    } else {
                        hand.get(i).setHighlight(!hand.get(i).getHighlight());
                    }
                }
            }

            ArrayList<Integer> highlightedIndices = getHighlightedIndices();

            if (highlightedIndices.size() == 2) {
                checkAndRemovePair(highlightedIndices);
            }

            if (highlightedIndices.size() == 3) {
                checkAndRemoveTrio(highlightedIndices);
            }
        }

        public ArrayList<Integer> getHighlightedIndices() {
            ArrayList<Integer> indices = new ArrayList<>();
            for (int i = 0; i < hand.size(); i++) {
                if (hand.get(i).getHighlight() && hand.get(i).isFaceUp()) {
                    indices.add(i);
                }
            }
            return indices;
        }

        public void checkAndRemovePair(ArrayList<Integer> indices) {
            Card c1 = hand.get(indices.get(0));
            Card c2 = hand.get(indices.get(1));
            if (c1.getNumericalValue() + c2.getNumericalValue() == 11) {
                for (int i : indices) {
                    replaceCard(i);
                }
            }
        }

        public void checkAndRemoveTrio(ArrayList<Integer> indices) {
            boolean hasJack = false, hasQueen = false, hasKing = false;
            for (int i : indices) {
                String val = hand.get(i).getValue();
                if (val.equals("J")){
                    hasJack = true;
                }
                if (val.equals("Q")){
                    hasQueen = true;
                }
                if (val.equals("K")){
                    hasKing = true;
                }
            }
            if (hasJack && hasQueen && hasKing) {
                for (int i : indices) {
                    replaceCard(i);
                }
            }
        }

    public void mouseReleased(MouseEvent e) { }
    public void mouseEntered(MouseEvent e) { }
    public void mouseExited(MouseEvent e) { }
    public void mouseClicked(MouseEvent e) { }
}