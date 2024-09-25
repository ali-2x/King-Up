import java.io.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * The class Gameboard is to print, control the flow of the game and
 * contains a set of players and characters.
 */
public class Gameboard {

    /**
     * The maximum number of characters can be placed in the same position.
     */
    public static final int FULL = 4;
    /**
     * The total number of characters
     */
    public static final int NO_OF_CHARACTER = 13;
    /**
     * The total number of player
     */
    public static final int NO_OF_PLAYERS = 4;
    /**
     * The position of Throne
     */
    public static final int THRONE = 6;
    /**
     * The scores calculation formula
     */
    public static final int[] SCORES = {0, 1, 2, 3, 4, 5, 10};
    /**
     * The name of the characters
     */
    public static final String[] CHARACTER_NAMES = {
            "Aligliero", "Beatrice", "Clemence", "Dario",
            "Ernesto", "Forello", "Gavino", "Irima",
            "Leonardo", "Merlino", "Natale", "Odessa", "Piero"
    };
    /**
     * The name of the players
     */
    public static final String[] PLAYER_NAMES = {
            "You", "Computer 1", "Computer 2", "Computer 3"
    };
    /**
     * Determine if the players are human player or not.
     */
    public static final boolean[] HUMAN_PLAYERS = {true, false, false, false};

    /**
     * A list of character
     */
    private Character[] characters;
    /**
     * A list of player
     */
    private Player[] players;

    public static void main(String[] argv) {
        new Gameboard().runOnce();
    }

    /**
     * Initialize all data attributes. You will need to initialize and create
     * the array players and characters. You should initialize them using the
     * String array PLAYER_NAMES and CHARACTER_NAMES respectively.
     */
    public Gameboard() {
        characters = new Character[NO_OF_CHARACTER];                             //initialize the array
        for (int i = 0; i < characters.length; i++) {
            characters[i] = new Character(CHARACTER_NAMES[i]);                    //instantiate the characters and add them to the array
        }

        players = new Player[NO_OF_PLAYERS];                                      //initialize the array
        for (int j = 0; j < players.length; j++) {
            players[j] = new Player(PLAYER_NAMES[j], characters);
            players[j].initVetoCard(3);
        }
    }

    public void runOnce() {

        print();
        System.out.println("======= Placing stage ======= \n"
                + "Each player will take turns to place three characters on the board.\n"
                + "No character can be placed in the position 0 or 5 or 6 (Throne) at this stage.\n"
                + "A position is FULL when there are four characters placed there already.\n"
                + "The remaining character will be placed at the position 0.\n");

        placingStage();

        print();
        System.out.println("======= Playing stage ======= \n"
                + "Each player will take turn to move a character UP the board.\n"
                + "You cannot move a character that is been killed or its immediate upper position is full.\n"
                + "A voting will be trigger immediately when a character is moved to the Throne (position 6).");

        playingStage();

        print();
        System.out.println("======= Scoring stage ======= \n"
                + "This will trigger if and only if the voting result is ALL positive, i.e., no player play the veto (reject) card. \n"
                + "The score of each player is computed by the secret list of characters owned by each player.");

        scoringStage();
    }


    /**
     * Print the scores of all players correctly.
     */
    private void scoringStage() {
        for (Player p : players) {
            System.out.println(p);
            System.out.println("Score: " + p.getScore());                                                           //the score is calculated in the getScore method and returned here
        }
    }

    private void placingStage() {
        Scanner scanner = new Scanner(System.in);
        int turn = 0;
        for (int i = 0; i < 12; i++) { // four players place 12 times.
            print();
            System.out.println(players[turn] + ", this is your turn to place a character");
            if (HUMAN_PLAYERS[turn % NO_OF_PLAYERS]) {
                Character pick = null;

                do {
                    System.out.println("Please pick a character");
                    String input = scanner.next();
                    for (Character c : characters)
                        if (c.getName().equals(input))
                            pick = c;
                } while (pick == null || pick.getPosition() != -1);
                int[] counts = new int[5];
                for (Character c : characters)
                    if (c.getPosition() != -1)
                        counts[c.getPosition()]++;
                int input;
                do {
                    System.out.println("Please enter the floor you want to place " + pick.getName());
                    input = scanner.nextInt();
                } while (input < 1 || input > 4 || counts[input] >= FULL);
                pick.setPosition(input);
            } else
                players[turn].placeRandomly(characters);
            turn = (turn + 1) % NO_OF_PLAYERS;
        }
        System.out.println("End placing");
        //place the remaining character to zero
        for (Character c : characters)
            if (c.getPosition() == -1)
                c.setPosition(0);
    }

    private boolean vote(Character pick) {
        Scanner scanner = new Scanner(System.in);
        print();
        Boolean voteResult = true;

        for (int i = 0; i < players.length; i++) {
            Player p = players[i];
            if (HUMAN_PLAYERS[i]) {
                System.out.println("Please vote. Type V for veto. Other for accept");
                voteResult &= p.vote(!scanner.next().equals("V"));
            } else
                voteResult &= p.voteSmartly(pick);
        }
        return voteResult;
    }

    /**
     * Perform playing stage. Be careful that human player will need to pick the character to move.
     * You should detect any invalid input and stop human player from doing something nonsense or illegal.
     * Computer players will need to run the code pickCharToMoveRandomly or pickCharToMoveSmartly to pick which character to move.
     */
    private void playingStage() {
        Scanner scanner = new Scanner(System.in);
        int turn = 0;
        do {
            print();
            System.out.println(players[turn]);
            System.out.println("This is your turn to move a character up");
            Character pick = null;
            if (HUMAN_PLAYERS[turn]) {
                while (pick == null) {
                    System.out.println("Please type the character that you want to move.");
                    pick = players[turn].pickCharToMove(characters, scanner.next());
                }
            } else
                pick = players[turn].pickCharToMoveRandomly(characters);

            pick.setPosition(pick.getPosition() + 1);

            if (pick.getPosition() == THRONE) {
                if (vote(pick)) //all support
                    return;
                //else kill the character
                pick.setPosition(-1);
            }
            turn = (turn + 1) % NO_OF_PLAYERS;
        } while (true);
    }

    /**
     * Print the gameboard.
     */
    private void print() {
        String unplaceCharacter = "";
        for (int i = 6; i >= 0; i--) {
            System.out.print("Level " + i + ":");
            for (Character c : characters) {
                if (c.getPosition() == i)
                    System.out.print("\t" + c.getName());
            }
            System.out.println();
        }
        System.out.println("Unplaced/Killed Characters");
        for (Character c : characters)
            if (c.getPosition() == -1)
                System.out.print(c + "\t");

        System.out.println();
        System.out.println();
    }
}
