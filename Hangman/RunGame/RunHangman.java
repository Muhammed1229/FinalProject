package Hangman.RunGame;

import Hangman.exception.InvalidGuessException;
import Hangman.logic.DatabaseStatsManager;
import Hangman.logic.FileWordProvider;
import Hangman.logic.TemporaryWordProvider;
import Hangman.logic.WordProvider;
import Hangman.ui.GameDisplay;
import Hangman.ui.InputValidator;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class RunHangman {

    // shared temporary word provider so words added this session are not lost between menu options
    private static final TemporaryWordProvider tempWords = new TemporaryWordProvider();;

    // scanner object to accept input
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        IO.println("=========================");
        IO.println("   WELCOME TO HANGMAN    ");
        IO.println("=========================");

        // prompt the player to enter a username, defaults to Guest if left empty
        IO.print("Enter your username: ");
        String username = scanner.nextLine().trim();
        if (username.isEmpty()) {
            username = "Guest";
        }

        // a loop to keep the main menu running as long as the user has not chosen to exit
        while (true) {
            IO.println("\n--- MAIN MENU ---");
            IO.println("1. Play Game");
            IO.println("2. Add Custom Word");
            IO.println("3. View Leaderboard");
            IO.println("4. Exit");
            IO.print("Choose an option: ");

            String menuChoice = scanner.nextLine().trim();

            switch (menuChoice) {
                case "1":
                    runGameSetup(username);
                    break;
                case "2":
                    runAddWordSetup(username);
                    break;
                case "3":
                    // fetches and displays the leaderboard from the stats database
                    IO.println("\n--- LIVE LEADERBOARD ---");
                    DatabaseStatsManager statsManager = new DatabaseStatsManager();
                    GameDisplay.printLeaderboard(statsManager.getLeaderboard());
                    break;
                case "4":
                    IO.println("Thanks for playing, " + username + "! Goodbye!");
                    return;
                default:
                    IO.println("Invalid selection. Try again.");
            }
        }
    }

    // prompts the player to select a word source and difficulty before starting the game
    private static void runGameSetup(String username) {
        IO.println("\nSelect Word Dictionary Source:");
        IO.println("1. Default Word List");
        IO.println("2. Permanent Custom Words List");
        IO.println("3. Temporary Custom Words List");
        IO.print("Selection: ");
        String trackChoice = scanner.nextLine().trim();

        // runtime polymorphism: parent type holds whichever child class the player selected
        WordProvider wordEngine;

        if (trackChoice.equals("1")) {
            wordEngine = new FileWordProvider(username);
        }
        else if (trackChoice.equals("2")) {
            wordEngine = new FileWordProvider(username, true);
        }
        else {
            wordEngine = tempWords;
        }

        IO.print("Select Difficulty (Easy, Medium, Hard): ");
        String difficulty = scanner.nextLine().trim();

        // retrieves a random word from the chosen source, catches invalid difficulty input
        String targetWord;
        try {
            targetWord = wordEngine.getRandomWord(difficulty);
        } catch (IllegalArgumentException e) {
            IO.println("Error: " + e.getMessage());
            return;
        }
        // defaults to EMPTY if no words are available in the selected difficulty
        if (targetWord == null || targetWord.equals("DEFAULT")) {
            IO.println("No words available in this tier category yet! Defaulting to 'EMPTY'.");
            targetWord = "EMPTY";
        }
        executeGameplayLoop(username, targetWord, difficulty);
    }

    // runs the main gameplay loop until the player wins or runs out of attempts
    private static void executeGameplayLoop(String username, String targetWord, String difficulty) {
        Set<Character> guessedLetters = new HashSet<>();
        int wrongGuesses = 0;
        final int MAX_ERRORS = 6;
        boolean gameWon = false;

        IO.println("\nGame started! Good luck, " + username + "!");

        // prints the hangman graphic and current word state each turn
        while (wrongGuesses < MAX_ERRORS) {
            GameDisplay.printHangman(wrongGuesses);
            GameDisplay.printWordState(targetWord, guessedLetters);

            IO.print("\nEnter a letter guess: ");
            String rawInput = scanner.nextLine();

            char guess;
            // validates the input and throws a custom exception if it breaks any game rules
            try {
                guess = InputValidator.validateGuess(rawInput, guessedLetters);
                guessedLetters.add(guess);

                if (targetWord.toLowerCase().indexOf(guess) >= 0) {
                    IO.println("Good guess! '" + guess + "' is in the word.");
                } else {
                    IO.println("Incorrect guess! '" + guess + "' is not in the word.");
                    wrongGuesses++; // wrong guess counter incremented only on an incorrect letter
                    IO.println("You have " + (MAX_ERRORS - wrongGuesses) + " attempts left");
                }

            // counter is protected when an invalid guess is caught, the turn is replayed
            } catch (InvalidGuessException e) {
                IO.println("\n[INPUT ERROR] " + e.getMessage());
                continue;
            }

            // checks if every letter in the target word has been guessed correctly
            boolean allGuessed = true;
            for (char c : targetWord.toCharArray()) {
                if (!guessedLetters.contains(Character.toLowerCase(c))) {
                    allGuessed = false;
                    break;
                }
            }

            if (allGuessed) {
                gameWon = true;
                break;
            }
        }

        // prints the final hangman state and win or loss message
        GameDisplay.printHangman(wrongGuesses);
        if (gameWon) {
            IO.println("\nCONGRATULATIONS! You guessed the word: " + targetWord);
        } else {
            IO.println("\nGAME OVER! The word was: " + targetWord);
        }

        // logs the completed game to the stats database
        DatabaseStatsManager stats = new DatabaseStatsManager();
        stats.logGame(username, targetWord, difficulty, gameWon);
    }

    // prompts the player to choose a storage type then saves the new word to that source
    private static void runAddWordSetup(String username) {
        IO.println("\nWhere do you want to save this word?");
        IO.println("1. Permanent save associated to your username");
        IO.println("2. Temporary save for this one session");
        IO.print("Selection: ");
        String trackChoice = scanner.nextLine().trim();

        // runtime polymorphism: parent type holds whichever child class the player selected
        WordProvider wordEngine;
        if (trackChoice.equals("1")) {
            wordEngine = new FileWordProvider(username);
        }
        else {
            wordEngine = tempWords;
        }

        IO.print("Enter new word: ");
        String newWord = scanner.nextLine().trim();
        IO.print("Enter difficulty classification (Easy, Medium, Hard): ");
        String diff = scanner.nextLine().trim();

        // catches invalid difficulty input and shows an error without crashing
        try {
            wordEngine.addWord(newWord, diff);
            IO.println("Word successfully added!");
        } catch (IllegalArgumentException e) {
            IO.println("Failed to add word: " + e.getMessage());
        }
    }
}