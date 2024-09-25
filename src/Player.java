import java.util.concurrent.ThreadLocalRandom;

/**
 * Model the player
 */
public class Player {
    /**
     * The length of the list
     */
    private static final int LIST_LENGTH = 6;
    /**
     * A secret list of a player. This list would not be changed during the game
     */
    private Character[] myList;
    /**
     * The number of veto (reject) voting card. Decrement it when the player vote
     * reject.
     */
    private int vetoCard;
    /**
     * The name of the player.
     */
    private String name;

    /**
     * Compute the score of a player. Each player should have a list of character
     *
     * @return the score of a player.
     */
    public int getScore() {
        int result = 0;
        for (Character c : myList) {
            if (c.getPosition() != Character.OUT_OF_GAME)
                result += Gameboard.SCORES[c.getPosition()];
        }
        return result;
    }

    /**
     * Return the name of a player.
     *
     * @return the name of the player
     */
    public String getName() {
        return name;
    }

    /**
     * Initialize the number of veto card
     *
     * @param card the number of veto card
     */
    public void initVetoCard(int card) {
        this.vetoCard = card;
    }

    /**
     * Initialize all data attributes.
     *
     * @param name the name of the player
     * @param list a whole list of characters and the player should pick
     *             LIST_LENGTH of <b>unique</b> characters
     */
    public Player(String name, Character[] list) {

        this.name = name;
        myList = new Character[LIST_LENGTH];

        boolean[] selected = new boolean[list.length];
        for (int i = 0; i < LIST_LENGTH; ) {
            int r = ThreadLocalRandom.current().nextInt(0, Gameboard.NO_OF_CHARACTER);
            if (selected[r] == true)
                continue;
            selected[r] = true;
            myList[i] = list[r];
            i++;   //we put i++ here because if selected[r] == true we need to re-roll for the same i
        }
    }

    /**
     * A method to vote according to the preference of the parameter support.
     * A player is forced to vote yes (support) if he/she has no more veto card.
     *
     * @param support The preference of the player
     * @return true if the player support it. False if the player reject (veto).
     */
    public boolean vote(boolean support) {
        if (vetoCard <= 0)
            return true;
        if (!support)
            vetoCard--;
        return support;
    }

    /**
     * Vote randomly. The player will be forced to vote support(true) if he is
     * running out of veto card.
     *
     * @return true if the player support it. False if the player reject (veto).
     */
    public boolean voteRandomly() {
        return vote(ThreadLocalRandom.current().nextBoolean());
    }

    /**
     * Randomly place a character during the placing stage. This method should pick a
     * random unplaced character and place it to a random valid position.
     *
     * @param list The list of all characters. Some of these characters may have been
     *             placed already
     * @return the character that just has been placed.
     */
    public Character placeRandomly(Character[] list) {
        int[] count = new int[5]; //you can only place level 0 to level 4
        for (Character c : list) {
            if (c.getPosition() == Character.OUT_OF_GAME)
                continue;
            count[c.getPosition()]++;
        }
        while (true) {
            Character c = list[ThreadLocalRandom.current().nextInt(0, Gameboard.NO_OF_CHARACTER)];
            if (c.getPosition() != Character.OUT_OF_GAME)
                continue;
            int randomPosition;
            while (true) {
                randomPosition = ThreadLocalRandom.current().nextInt(1, 5); //can only placed at level 1 to level 4
                if (count[randomPosition] != Gameboard.FULL) {
                    c.setPosition(randomPosition);
                    return c;
                }
            }
        }
    }

    /**
     * The player shall vote smartly (i.e., its decision will be based on if the player has that
     * character in his/her list.) If the player is running out of veto card, he/she will be forced
     * to vote support (true).
     *
     * @param character The character that is being vote.
     * @return true if the player support, false if the player reject(veto).
     */
    public boolean voteSmartly(Character character) {
        for (Character c : myList) {
            if (character == c)
                return vote(true);
        }
        return vote(false);
    }

    /**
     *
     * @param list The entire list of characters
     * @return the character being picked to move. It never returns null.
     */
    public Character pickCharToMoveRandomly(Character[] list) {
        while (true) {
            Character c = list[ThreadLocalRandom.current().nextInt(0, Gameboard.NO_OF_CHARACTER)];
            if (isValidMove(list, c)) {
                return c;
            }
        }
    }

    /**
     * This method return the character who's name is the same as the
     * variable name if the character is <i>movable</i>. Movable means
     * the character has not yet be killed and the position right above
     * it is not full.
     * <p>
     * If the name of the character can't be found from the list or the
     * character is not movable, this method returns null.
     * <p>
     * Note: this method should not change the position of the character.
     *
     * @param list The entire list of characters
     * @param name The name of the character being picked.
     * @return the character being picked to move or null if the character
     * can't be found or it is not movable.
     */

    public Character pickCharToMove(Character[] list, String name) {
        for (Character c : list) {
            if (c.getName().equals(name)) {
                if (isValidMove(list, c))
                    return c;
                else
                    return null;
            }
        }
        return null;
    }

    private boolean isValidMove(Character[] list, Character character) {
        int[] slots = new int[7];
        if (character.getPosition() == Character.OUT_OF_GAME)
            return false;
        for (Character c : list) {
            if (c.getPosition() != Character.OUT_OF_GAME)
                slots[c.getPosition()]++;
        }
        if (slots[character.getPosition() + 1] == Gameboard.FULL)
            return false;
        return true;
    }

    /**
     * @param list The list of character
     * @return the character to be moved. It never returns null.
     */
    public Character pickCharToMoveSmartly(Character[] list) {
        //move my lowest valid move, not being killed
        int lowest = 6;
        Character character = null;
        for (Character i : myList)
            if (i.getPosition() < lowest && isValidMove(list, i)) {
                character = i;
                lowest = character.getPosition();
            }
        if (character != null) {
            return character;
        }
        //move a random candidate if none of my candidate is movable
        return pickCharToMoveRandomly(list);
    }

    /**
     * This returns the name of the player and the secret list of the characters.
     * as a string
     *
     * @return The name of the player followed by the secret list of the characters.
     */
    public String toString() {
        String result = name + "\t Veto Card :" + vetoCard + "\n";
        for (Character c : myList)
            result += c.toString() + "\t";
        return result;
    }
}
