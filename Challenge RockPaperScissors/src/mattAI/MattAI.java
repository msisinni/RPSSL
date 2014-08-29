package mattAI;

import java.util.Random;
import java.util.Scanner;

class Storage {
	// String with choices;
	// Order chosen so that x loses to x+1 and x+3;
	final static String[] WORDS = { "Rock", "Paper", "Scissors", "Spock",
			"Lizard" };
	// Matrix of outcomes;
	// Row (first []) is the player / input;
	// Column (second []) is the computer;
	// so [0][1] = -1 = Rock vs Paper; Player loses to computer;
	final static int[][] GRID = { { 0, -1, 1, -1, 1 }, { 1, 0, -1, 1, -1 },
			{ -1, 1, 0, -1, 1 }, { 1, -1, 1, 0, -1 }, { -1, 1, -1, 1, 0 } };
	final static int LENGTH = WORDS.length;
}

class Conversions {
	// This converts the text input to its place in the above WORDS array;
	int toNum(String input) {
		// marker's initialization value chosen as -1 because
		// 0 is a valid choice (Rock);
		// If the input doesn't match a choice, -1 will be returned, which
		// ends the game;
		int marker = -1;

		try {
			int value = Integer.parseInt(input);
			if (value >= 0 && value <= 4) {
				marker = value;
			}
		} catch (NumberFormatException e) {
			for (int i = 0; i < Storage.LENGTH; i++) {
				if (input.equalsIgnoreCase(Storage.WORDS[i])) {
					marker = i;
					break;
				}
			}
		}

		return marker;
	}

}

class Artificial {
	Random random = new Random();

	// outputs choice for AI;
	// RedditAI counterpicks based on the highest set of choices in the player
	// choice history;
	// MattAI chooses randomly 5% of the time, counterpicks based on the second
	// highest set of choices in the player choice history 15% of the time, and
	// follows RedditAI's choices the remaining 80% of the time;
	int choose(int[] record, boolean flag) {
		int marker = 0;
		double flagPercent = Math.random();

		if (flag && flagPercent < 0.05) {
			marker = random.nextInt(Storage.LENGTH);
			return marker;
		}
		marker = tieFinder(record);

		if (flag && flagPercent < 0.2) {
			marker = tieFinder(secondHighest(record, marker));
		}

		if (marker < 10) {
			marker = (marker + 1 + (random.nextInt(2) + 2) % 3)
					% Storage.LENGTH;
		} else if (marker > 10000) {
			marker = random.nextInt(Storage.LENGTH);
		} else {
			marker = tieChoice(marker);
		}

		return marker;
	}

	int tieFinder(int[] record) {
		// This will determine which values have been played the most;
		// Ties values are recorded as multidigit ints;
		int marker = 0;
		int playCount = 1;
		int digit = 10;

		for (int i = 0; i < Storage.LENGTH; i++) {
			if (record[i] == 0) { // no point in analyzing 0 play choices;
				continue;
			} else if (playCount < record[i]) {
				marker = i; // i is the int value of the choice;
				playCount = record[i]; // playCount updates;
				digit = 10; // digit resets to 10 in case there was a tie;
			} else if (playCount == record[i]) {
				// newest tie choice added to front of marker;
				marker = marker + i * digit;
				// digit increased in order so that subsequent tie values added
				// to front of marker;
				digit = digit * 10;
			}
		}

		return marker;
	}

	int tieChoice(int marker) {
		int[] sum = new int[Storage.LENGTH];
		// find length of tie marker;
		int markerDigits = Integer.toString(marker).length();
		int currentDigit = 0;

		// sum the choices;
		// first loop through digits of marker;
		// then loop through the outcome matrix;
		for (int i = 0; i < markerDigits; i++) {
			currentDigit = marker % 10;
			for (int j = 0; j < Storage.LENGTH; j++) {
				// -= because this allows me to use tieFinder;
				// goal changes from finding smallest negative to finding
				// biggest positive;
				sum[j] -= Storage.GRID[currentDigit][j];
			}
			// /= 10 drops the last digit
			marker /= 10;
		}
		// possibly returns another multidigit tie of potential choices;
		int results = tieFinder(sum);
		int resultsDigits = Integer.toString(results).length();
		// if there is a tie, selects a random value between 0 and the number of
		// digits of the tie;
		int interimChoice = random.nextInt(resultsDigits);
		// final choice is the digit of results chosen above;
		int choice = ((results / ((int) Math.pow(10, interimChoice))) % 10)
				% Storage.LENGTH;

		return choice;
	}

	int[] secondHighest(int[] record, int marker) {
		int[] tempRecord = new int[Storage.LENGTH];
		System.arraycopy(record, 0, tempRecord, 0, Storage.LENGTH);
		int markerDigits = Integer.toString(marker).length();
		int currentDigit = 0;
		for (int i = 0; i < markerDigits; i++) {
			currentDigit = marker % 10;
			tempRecord[currentDigit] = tempRecord[currentDigit] / 2 + 1;
			marker /= 10;
		}
		return tempRecord;
	}

}

public class MattAI {

	public static void main(String[] args) {
		int inPlayer, inAI;
		int playerWins = 0;
		int ties = 0;
		int gameCount = 0;
		int[] record = new int[Storage.LENGTH];

		Artificial artificial = new Artificial();
		Conversions conversions = new Conversions();

		Scanner gameMode = new Scanner(System.in);
		Scanner player = new Scanner(System.in);

		System.out
				.printf("Choose your opponent:\nSelect (1) for random AI.\nSelect (2) for Reddit's AI.\nSelect (3) for custom AI.\n\n");
		int version = 0;
		try {
			version = gameMode.nextInt();
		} catch (Exception e) {
			System.out.println("A valid option was not chosen!");
		}

		if (version < 1 || version > 3) {
			gameMode.close();
			player.close();
			return;
		}

		System.out
				.printf("Choose Rock, Paper, Scissors, Spock, or Lizard! Entries are case insensitive.\n");
		System.out
				.printf("0, 1, 2, 3, and 4 are also valid choices. Enter another value to quit!\n");
		do {
			System.out.printf("\nGame #%d:\n", gameCount + 1);

			inPlayer = conversions.toNum(player.nextLine());

			if (version == 1 || gameCount == 0) {
				inAI = artificial.random.nextInt(Storage.LENGTH);
			} else if (version == 2) {
				inAI = artificial.choose(record, false);
			} else {
				inAI = artificial.choose(record, true);
			}

			if (inPlayer == -1)
				break;
			if (Storage.GRID[inPlayer][inAI] == 1) {
				System.out.printf(
						"Player chose %s; Computer chose %s;\nPlayer Wins!\n",
						Storage.WORDS[inPlayer], Storage.WORDS[inAI]);
				playerWins++;
			} else if (Storage.GRID[inPlayer][inAI] == -1) {
				System.out
						.printf("Player chose %s; Computer chose %s;\nComputer Wins!\n",
								Storage.WORDS[inPlayer], Storage.WORDS[inAI]);
			} else {
				System.out.printf(
						"Player chose %s; Computer chose %s;\nTie!\n",
						Storage.WORDS[inPlayer], Storage.WORDS[inAI]);
				ties++;
			}

			record[inPlayer]++;
			gameCount++;

		} while (true); // inPlayer != -1

		gameMode.close();
		player.close();

		if (gameCount > 0) {
			System.out.printf("\nOutcome:\n");
			System.out
					.printf("Player has won %d games out of %d.\nWin percentage:  %2.2f%%\n",
							playerWins, gameCount, playerWins * 100.0
									/ gameCount);
			System.out
					.printf("There were %d tie games out of %d.\nTie percentage:  %2.2f%%\n",
							ties, gameCount, ties * 100.0 / gameCount);
		}

	}
	// thoughts - if game is modified to include more than 10 choices, will need
	// to change current tie system and it's reliance on digits in base 10;
	// Maybe create an array of length # of ties (would require two loops) or
	// look into lists;
}