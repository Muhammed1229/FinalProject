package Hangman.logic;

import java.util.ArrayList;
import java.util.List;

// stores words temporarily in memory for the duration of the session only
public class TemporaryWordProvider extends WordProvider {

    // three separate lists holding words grouped by difficulty level
    private ArrayList<String> easyWords;
    private ArrayList<String> mediumWords;
    private ArrayList<String> hardWords;

    public TemporaryWordProvider() {
        easyWords = new ArrayList<>();
        mediumWords = new ArrayList<>();
        hardWords = new ArrayList<>();
    }

    // method overriding: adds a word to the matching difficulty list for this session
    @Override
    public void addWord(String word, String difficulty) {

        switch (difficulty.toLowerCase()) {
            case "easy":
                easyWords.add(word);
                break;

            case "medium":
                mediumWords.add(word);
                break;

            case "hard":
                hardWords.add(word);
                break;

            default:
                throw new IllegalArgumentException("Invalid difficulty. Must be Easy, Medium, or Hard.");
        }
    }

    // method overriding: returns the word list for the requested difficulty level
    @Override
    protected List<String> getWordList(String difficulty) {
        switch (difficulty) {
            case "EASY":
                return easyWords;
            case "MEDIUM":
                return mediumWords;
            case "HARD":
                return hardWords;
            default:
                return new ArrayList<>();
        }
    }
}
