package Hangman.ui;

import java.util.List;
import java.util.Set;

public class GameDisplay {
    public static void printHangman(int wrongGuesses) {

        //Each position in this array represents a stage of the hangman
        String[] stages = {
                """
                 -----
                 |   |
                 |
                 |
                 |
                 |
                =========
                """,

                """
                 -----
                 |   |
                 |   O
                 |
                 |
                 |
                =========
                """,

                """
                 -----
                 |   |
                 |   O
                 |   |
                 |
                 |
                =========
                """,

                """
                 -----
                 |   |
                 |   O
                 |  /|
                 |
                 |
                =========
                """,

                """
                 -----
                 |   |
                 |   O
                 |  /|\\
                 |
                 |
                =========
                """,

                """
                 -----
                 |   |
                 |   O
                 |  /|\\
                 |  /
                 |
                =========
                """,

                """
                 -----
                 |   |
                 |   O
                 |  /|\\
                 |  / \\
                 |
                =========
                """
        };

        // to make sure we don't try to access an index that doesn't exist
        if (wrongGuesses >= 0 && wrongGuesses <= 6) {
            IO.println(stages[wrongGuesses]);
        } else {
            IO.println("Error: Invalid hangman stage.");
        }
    }

    public static void printWordState(String word, Set<Character> guessedLetters) {

        //goes through each letter/character in the secret word
        //If the player has guessed a correct character, it shows the character otherwise keeps it hidden
        for (char c : word.toCharArray()) {
            if (guessedLetters.contains(Character.toLowerCase(c))) {
                IO.print(c + " ");
            } else {
                IO.print("_ ");
            }
        }
        IO.println();
    }

    public static void printLeaderboard(List<String> highScores) {

        IO.println("\n========== LEADERBOARD ==========");

        //in case there are no scores yet
        if (highScores.isEmpty()) {
            IO.println("No scores available.");
            return;
        }

        int rank = 1;

        //Displays each score with its ranking number
        for (String score : highScores) {
            IO.println(rank + ". " + score);
            rank++;
        }

        IO.println("===================================\n");
    }
}