package Hangman.logic;

import java.util.List;
import java.util.Random;

// abstract parent class that child word providers inherit from
public abstract class WordProvider {

    // shared random instance used by child classes to pick a word
    protected final Random random = new Random();

    // template method that handles validation and random selection so child classes dont repeat it
    public String getRandomWord(String difficulty) {
        String validated = validateDifficulty(difficulty);
        List<String> words = getWordList(validated);
        if (words == null || words.isEmpty()) return null;
        return words.get(random.nextInt(words.size()));
    }

    // method to be inherited and overridden by child classes to return their word list
    protected abstract List<String> getWordList(String difficulty);

    // method to be inherited and overridden by child classes to save a new word
    public abstract void addWord(String word, String difficulty);

    // validates and uppercases the difficulty before passing it to child classes
    protected String validateDifficulty(String difficulty) {
        if (difficulty == null)
            throw new IllegalArgumentException("Difficulty cannot be null.");
        String formatted = difficulty.trim().toUpperCase();
        if (!formatted.equals("EASY") && !formatted.equals("MEDIUM") && !formatted.equals("HARD"))
            throw new IllegalArgumentException("Invalid difficulty. Must be EASY, MEDIUM, or HARD.");
        return formatted;
    }
}