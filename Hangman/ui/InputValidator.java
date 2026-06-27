package Hangman.ui;

import Hangman.exception.InvalidGuessException;
import java.util.Set;

public class InputValidator {

    // validates a player's letter guess/input against game rules.

    public static char validateGuess(String input, Set<Character> guessedLetters)throws InvalidGuessException{
        if(input == null || input.isEmpty()){
            throw new InvalidGuessException("Input cannot be empty ");

        }
        if (input.length()>1){
            throw new InvalidGuessException("Input length exceeds one character. Please guess only a single letter.");
        }
        char guess = input.charAt(0);
        if(!Character.isLetter(guess)){
            throw new InvalidGuessException("Input contains non-alphabetic symbols. Letters only!");
        }

        // a set containing lowercase characters already guessed.

        char LowerGuess = Character.toLowerCase(guess);
        if (guessedLetters != null &&guessedLetters.contains(LowerGuess)){
            throw new InvalidGuessException("The letter : " + guess +" ' has already been guessed previously. Try a different letter.");
        }
        return LowerGuess;
    }

    // validates menu and difficulty selections to prevent invalid inpuut and application crashes.

    public static int validateMenuSelection(String input , int min, int max){
        try{
            int choice= Integer.parseInt(input.trim());
            if(choice < min || choice > max){
                IO.println("Invalid navigation : please enter a number between " + min +" and " + max + " .");
                return -1;
            }
            return choice;
        }catch(NumberFormatException e){
            IO.println("Invalid format. Please enter a numerical value.");
            return -1;
        }
    }
}
