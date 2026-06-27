package Hangman.logic;

import java.io.*;
import java.sql.*;
import java.util.*;

// handles word retrieval from two separate sources: words.txt and the custom words database
public class FileWordProvider extends WordProvider {

    // stores the current player's username.
    private final String username;

    // represents the words.txt file.
    private final File wordsFile;

    // sqlite database connection URL.
    private final String url = "jdbc:sqlite:hangman_words.db";

    // controls which word source is used: false = words.txt, true = custom words database
    private final boolean useDatabase;

    // default constructor: sets the source to words.txt
    public FileWordProvider(String username) {
        this(username, false);
    }

    // parameterized constructor: receives the active player's username and the chosen word source.
    public FileWordProvider(String username, boolean useDatabase) {
        this.username = username;
        this.useDatabase = useDatabase;
        this.wordsFile = new File("words.txt");

        loadDriver();
        createWordsFileIfMissing();
        createCustomWordsTable();
    }

    // loads the SQLite JDBC driver so Java can communicate with the database.
    private void loadDriver() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            IO.println("SQLite JDBC Driver not found: " + e.getMessage());
        }
    }

    // creates and returns a database connection.
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url);
    }

    // creates words.txt with starter words if the file does not already exist.
    private void createWordsFileIfMissing() {
        if (wordsFile.exists() && wordsFile.length() > 0) {
            return;
        }
        String[] words = {
                "APPLE:EASY", "BANANA:EASY", "CHAIR:EASY", "TABLE:EASY", "CLOCK:EASY",
                "SPOON:EASY", "TIGER:EASY", "LION:EASY", "BEAR:EASY", "SHIRT:EASY",
                "SHOES:EASY", "GRASS:EASY", "WATER:EASY", "TRAIN:EASY", "PLANT:EASY",
                "BREAD:EASY", "HOUSE:EASY", "MOUSE:EASY", "SNAKE:EASY", "HEART:EASY",
                "CLOUD:EASY", "STORM:EASY", "BEACH:EASY", "RIVER:EASY", "STONE:EASY",
                "FLOWER:EASY", "LEMON:EASY", "MAGIC:EASY", "OCEAN:EASY", "PAPER:EASY",
                "RADIO:EASY", "SHEEP:EASY", "SMILE:EASY", "SUGAR:EASY", "TRUCK:EASY",
                "VOICE:EASY", "WHEEL:EASY", "WORLD:EASY", "YOUTH:EASY", "ZEBRA:EASY",
                "GUITAR:MEDIUM", "PILLOW:MEDIUM", "WINDOW:MEDIUM", "ORANGE:MEDIUM", "PENCIL:MEDIUM",
                "POCKET:MEDIUM", "BLANKET:MEDIUM", "CAMERA:MEDIUM", "ISLAND:MEDIUM", "DOCTOR:MEDIUM",
                "FOREST:MEDIUM", "PLANET:MEDIUM", "MIRROR:MEDIUM", "SILVER:MEDIUM", "YELLOW:MEDIUM",
                "BOTTLE:MEDIUM", "CASTLE:MEDIUM", "DRAGON:MEDIUM", "TENNIS:MEDIUM", "COFFEE:MEDIUM",
                "TICKET:MEDIUM", "BASKET:MEDIUM", "ROCKET:MEDIUM", "MONKEY:MEDIUM", "ANIMAL:MEDIUM",
                "BRIDGE:MEDIUM", "CANDLE:MEDIUM", "CHEESE:MEDIUM", "DESERT:MEDIUM", "ENGINE:MEDIUM",
                "FAMILY:MEDIUM", "FARMER:MEDIUM", "GARDEN:MEDIUM", "HUNTER:MEDIUM", "JUNGLE:MEDIUM",
                "KITTEN:MEDIUM", "LIZARD:MEDIUM", "MARKET:MEDIUM", "NATURE:MEDIUM", "PALACE:MEDIUM",
                "PEPPER:MEDIUM",
                "ASTRONAUT:HARD", "BICYCLE:HARD", "ELEPHANT:HARD", "KANGAROO:HARD", "UMBRELLA:HARD",
                "ZOMBIE:HARD", "ALLIGATOR:HARD", "BUTTERFLY:HARD", "CHOCOLATE:HARD", "DINOSAUR:HARD",
                "HELICOPTER:HARD", "MOTORCYCLE:HARD", "SUBMARINE:HARD", "TELESCOPE:HARD", "XYLOPHONE:HARD",
                "MYSTERY:HARD", "RHYTHM:HARD", "JAZZ:HARD", "ZIGZAG:HARD", "PHARAOH:HARD",
                "SYNDROME:HARD", "AWKWARD:HARD", "BLIZZARD:HARD", "OXYGEN:HARD", "SQUIRREL:HARD",
                "PNEUMONIA:HARD", "MICROWAVE:HARD", "KNAPSACK:HARD", "JELLYFISH:HARD", "HYPOCRITE:HARD",
                "GYMNASTICS:HARD", "FREEZING:HARD", "EXCELLENT:HARD", "CROCODILE:HARD", "CHANDELIER:HARD",
                "BACKPACK:HARD", "AVALANCHE:HARD", "AMBULANCE:HARD"
        };

        try (PrintWriter writer = new PrintWriter(new FileWriter(wordsFile))) {
            for (String word : words) {
                writer.println(word);
            }

            } catch (IOException e) {
                IO.println("Error creating words.txt: " + e.getMessage());
            }
        }

    // creates the custom_words database table if it does not already exist.
    private void createCustomWordsTable() {

        String sql = """
                CREATE TABLE IF NOT EXISTS custom_words (
                    word TEXT,
                    difficulty TEXT,
                    username TEXT
                )
                """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(sql);

        } catch (SQLException e) {
            IO.println("Error creating custom_words table: " + e.getMessage());
        }
    }

    // method overriding: returns words from words.txt or the database depending on the useDatabase flag
    @Override
    protected List<String> getWordList(String difficulty) {
        List<String> words = new ArrayList<>();
        if (useDatabase) {
            loadWordsFromDatabase(words, difficulty);
        } else {
            loadWordsFromFile(words, difficulty);
        }
        return words;
    }

    // reads words from words.txt and adds matching difficulty words to the list.
    private void loadWordsFromFile(List<String> words, String difficulty) {

        try (BufferedReader reader = new BufferedReader(new FileReader(wordsFile))) {

            String line;

            while ((line = reader.readLine()) != null) {

                String[] parts = line.split(":");

                if (parts.length == 2) {

                    String word = parts[0].trim();
                    String wordDifficulty = parts[1].trim();

                    if (wordDifficulty.equalsIgnoreCase(difficulty)) {
                        words.add(word);
                    }
                }
            }

        } catch (IOException e) {
            IO.println("Error reading words.txt: " + e.getMessage());
        }
    }

    // retrieves custom words belonging to the current player from the database.
    private void loadWordsFromDatabase(List<String> words, String difficulty) {

        String sql = """
                SELECT word FROM custom_words
                WHERE difficulty = ? AND username = ?
                """;

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, difficulty);
            pstmt.setString(2, username);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                words.add(rs.getString("word"));
            }

        } catch (SQLException e) {
            IO.println("Error loading custom words: " + e.getMessage());
        }
    }

    // method overriding: saves a new custom word to the database for the current player
    // the word is stored permanently and does not modify words.txt.

    @Override
    public void addWord (String word, String difficulty) {

        String sql = """
                INSERT INTO custom_words (word, difficulty, username)
                VALUES (?, ?, ?)
                """;

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, word.toUpperCase());
            pstmt.setString(2, difficulty.toUpperCase());
            pstmt.setString(3, username);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            IO.println("Error saving custom word: " + e.getMessage());
        }
    }
}
