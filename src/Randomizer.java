import java.util.Random;

/**
 * Created by clark on 4/9/16.
 */
public class Randomizer {
    private Integer ROWS = 15;
    private Integer COLUMNS = 15;
    private Integer NO_OF_TREASURE = 60;
    private String[][] grid;
    public Random random = new Random();

    public Randomizer(Integer ROWS, Integer COLUMNS, Integer NO_OF_TREASURE, String[][] grid) {
        this.ROWS = ROWS;
        this.COLUMNS = COLUMNS;
        this.NO_OF_TREASURE = NO_OF_TREASURE;
        this.grid = grid;
    }

    public Randomizer(String[][] grid) {
        this.grid = grid;
    }

    public String[][] loadInitTreasures() {
        int counter = 0;
        int randRow, randCol;
        while (counter < NO_OF_TREASURE) {
            randRow = random.nextInt(ROWS);
            randCol = random.nextInt(COLUMNS);

            if (grid[randRow][randCol] != null && grid[randRow][randCol] != "O") {
                continue;
            }

            grid[randRow][randCol] = "x";
            counter++;
        }

        return grid;
    }

    public Integer[] setRandomLocation(Boolean isPlayer, String playerName, String[][] stateGrid) {
        int randRow, randCol;
        while (true) {
            randRow = random.nextInt(ROWS);
            randCol = random.nextInt(COLUMNS);

            if (stateGrid[randRow][randCol] != null && !stateGrid[randRow][randCol].equals("O")) {
                continue;
            }
            Integer[] newPos = {randRow, randCol};
            return newPos;
        }
    }
}
