
# A console Hangman game with persistent word banks and live stats

It's a console game where you guess a hidden word one letter at a time before the hangman finishes drawing. You can pull words from three different sources, and every finished game gets logged to a SQLite database so your stats and leaderboard actually stick around between runs instead of resetting every time you close the program.

## What it does:

- **Three Word Sources:** Pick a built-in Default Word List, a Permanent Custom Words bank saved to a database under your username, or a Temporary Custom Words list that only lives for the current session.
- **Auto-Generated Word File:** If `words.txt` doesn't already exist, the game creates it on first run and fills it with a starter list of Easy, Medium, and Hard words so the Default Word List always has something to pull from.
- **Difficulty Tiers:** Words are sorted into Easy, Medium, and Hard, and only the tier you select gets pulled from.
- **ASCII Hangman:** Redraws a 7-stage gallows after every wrong guess, and the round ends once you've missed 6 times.
- **Input Validation:** Rejects empty guesses, multi-letter guesses, numbers/symbols, and letters you've already tried — without burning a turn.
- **Stats & Leaderboard:** Every completed game is logged to a SQLite database, and the main menu can pull up a live leaderboard ranked by wins.
- **Add Custom Word:** Lets you grow either your permanent or temporary word bank straight from the main menu.

## OOP Concepts Used:

- **Classes & Single-Responsibility Objects (OOP V1.0):** Instead of cramming everything into one `main` method, the game is split into objects that each do one job — `GameDisplay` draws the gallows and leaderboard, `InputValidator` checks guesses, `WordProvider` and its children supply words, and `DatabaseStatsManager` talks to the database. `RunHangman.java` just wires them all together.
- **Encapsulation & Access Modifiers (OOP V1.0 & V2.0):** Fields like `username`, `wordsFile`, `url`, and `useDatabase` inside `FileWordProvider.java` (lines 11-20) are all `private`, so the rest of the program can only interact with a word provider through its `public` methods, like `getRandomWord()` in `WordProvider.java` (line 13).
- **Constructor Overloading & Chaining (OOP V2.0):** `FileWordProvider.java` has two constructors — `FileWordProvider(String username)` (line 23) chains into the parameterized `FileWordProvider(String username, boolean useDatabase)` (line 28) using `this(username, false)` (line 24), so picking the default word list doesn't repeat any setup logic.
- **Static Members (OOP V2.0):** `GameDisplay`'s `printHangman()` (line 7), `printWordState()` (line 90), and `printLeaderboard()` (line 104), along with both methods in `InputValidator.java` (lines 10 & 34), are all `static` since none of them need their own object. `RunHangman.java` keeps `tempWords` and `scanner` as `static` fields (lines 17 & 20) for the same reason.
- **Inheritance & Abstract Classes (OOP V3.0):** `WordProvider.java` is an `abstract` parent class (line 7) with two abstract methods, `getWordList()` (line 21) and `addWord()` (line 24). `FileWordProvider.java` (line 8) and `TemporaryWordProvider.java` (line 7) both `extends WordProvider`, giving Hierarchical Inheritance with two completely different word sources sitting underneath the same contract.
- **Protected Members & `super()` (OOP V3.0):** The shared `random` field in `WordProvider.java` is declared `protected` (line 10) so both child classes can reuse the same `Random` instance. `InvalidGuessException.java` extends `Exception` (line 3) and calls `super(message)` (line 5) to pass its custom text up to the parent constructor.
- **Polymorphism — Upcasting & Dynamic Dispatch (OOP V4.0):**
  - *Upcasting:* `RunHangman.java` declares `WordProvider wordEngine` (line 77) and assigns it either a `FileWordProvider` or `TemporaryWordProvider` depending on the menu choice (lines 80-87), without the rest of the method ever needing to know which one it actually got.
  - *Dynamic Method Dispatch:* When `wordEngine.getRandomWord(difficulty)` runs (line 95), it calls `WordProvider`'s template method (line 13), which in turn calls `getWordList()` — and the JVM resolves that call to whichever subclass is actually sitting in memory.
  - *Method Overriding:* Both `getWordList()` and `addWord()` carry `@Override` in `FileWordProvider.java` (lines 117 & 183) and `TemporaryWordProvider.java` (lines 43 & 21).
- **Exception Handling (OOP V5.0):**
  - Built a custom checked exception, `InvalidGuessException.java`, used purely for bad player input.
  - `InputValidator.validateGuess()` declares `throws InvalidGuessException` (line 10) and throws it for empty input (line 12), multi-character input (line 16), non-letter symbols (line 20), and repeat guesses (line 27).
  - `RunHangman.executeGameplayLoop()` wraps the guess logic in `try` (line 127) and `catch (InvalidGuessException e)` (line 140), then uses `continue` so the player's try counter is protected instead of the program crashing.
  - Unchecked `IllegalArgumentException` is thrown from `WordProvider.validateDifficulty()` (lines 29 & 32) and caught back in `RunHangman.java` (lines 96 & 200) so a bad difficulty string never takes down the menu.
  - try-with-resources wraps every JDBC `Connection`/`PreparedStatement` pair (e.g. `DatabaseStatsManager.logGame()`, line 62) so resources always close, even if something goes wrong.
- **Files and Streams (OOP V6.0):**
  - `FileWordProvider.java` builds a `File` object for `words.txt` (line 31) and checks `wordsFile.exists()` (line 54) before deciding whether to regenerate it.
  - `createWordsFileIfMissing()` writes the starter word list using a `PrintWriter` wrapped around a `FileWriter` (line 85).
  - `loadWordsFromFile()` reads it back with a `BufferedReader` wrapped around a `FileReader` (line 131), pulling it in one line at a time with `readLine()` (line 135).
- **Database Programming / JDBC (OOP V7.0):**
  - Both `DatabaseStatsManager.loadDriver()` (line 22) and `FileWordProvider.loadDriver()` (line 41) call `Class.forName("org.sqlite.JDBC")` to load the SQLite driver.
  - `getConnection()` in both classes opens the connection with `DriverManager.getConnection(url)` (`DatabaseStatsManager.java` line 30, `FileWordProvider.java` line 49).
  - Plain `Statement` is used for fixed SQL like `CREATE TABLE IF NOT EXISTS` (`DatabaseStatsManager.createTable()`, line 45), while `PreparedStatement` with `?` placeholders handles anything involving real data, like `logGame()` (line 63) and `FileWordProvider.loadWordsFromDatabase()` (line 164).
  - `DatabaseStatsManager.getLeaderboard()` loops through the results with `while (rs.next())` (line 95) and reads columns by name, e.g. `rs.getString("player_name")` (line 96), to build the ranked leaderboard.

## How to run it:

1. Clone the repository, or download and extract the zip file. Move the Hangman folder to your desired classpath — do not run it from inside the HangmanGame-main folder.
2. Open the project in your IDE, and make sure the SQLite JDBC driver `.jar` is added to the classpath (it's required by `DatabaseStatsManager` and `FileWordProvider`).
3. Find the `RunHangman.java` file inside the `Hangman.RunGame` package and run it.
4. Enter a username, pick an option from the main menu, and follow the text prompts to start guessing!
