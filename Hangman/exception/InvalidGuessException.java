// class that gives a custom exception message
package Hangman.exception;
public class InvalidGuessException extends Exception{
    public InvalidGuessException(String message){
        super (message);
    } 
}