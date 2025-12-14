/******************************************************************************
 *  Compilation:  javac TextCompressor.java
 *  Execution:    java TextCompressor - < input.txt   (compress)
 *  Execution:    java TextCompressor + < input.txt   (expand)
 *  Dependencies: BinaryIn.java BinaryOut.java
 *  Data files:   abra.txt
 *                jabberwocky.txt
 *                shakespeare.txt
 *                virus.txt
 *
 *  % java DumpBinary 0 < abra.txt
 *  136 bits
 *
 *  % java TextCompressor - < abra.txt | java DumpBinary 0
 *  104 bits    (when using 8-bit codes)
 *
 *  % java DumpBinary 0 < alice.txt
 *  1104064 bits
 *  % java TextCompressor - < alice.txt | java DumpBinary 0
 *  480760 bits
 *  = 43.54% compression ratio!
 ******************************************************************************/

import java.util.ArrayList;
import java.util.HashMap;

/**
 * The {@code TextCompressor} class provides static methods for compressing
 * and expanding natural language through textfile input.
 *
 * @author Zach Blick, Zander Deutch
 */
public class TextCompressor {

    private static void compress() {
        // TST stores the dictionary of known sequences and turns string patterns into numeric codes
        TST dictionary = new TST();

        // Insert all extended ASCII values into the dictionary at the beginning
        for (int i = 0; i < 128; i++) {
            dictionary.insert("" + (char) i, i);
        }

        // Tracks next available code pattern index in TST
        int nextCode = 129;

        // Maximum amount of codes that can be used
        int maxCodes = 256;

        // 12-bit codes per entry
        int codeWidth = 8;

        // Signals end of file
        int EOF_Code = 128;

        // Read entire input of file and track the current positon in the file
        String input = BinaryStdIn.readString();
        int position = 0;

        // Process input from start to finish
        while (position < input.length()) {
            // build the match manually without skipping over letters
            String match = "" + input.charAt(position);

            // Get the numeric index for this pattern in ASCII
            int code = dictionary.lookup(match);

            // Try to extend the match through searching if the longer index is in the TST
            while (position + match.length() < input.length()) {
                // Identify the next possible char in the file
                char nextChar = input.charAt(position + match.length());
                String longer = match + nextChar;
                // Then lookup that word
                int longerCode = dictionary.lookup(longer);

                // If empty then move on
                // Otherwise update your match and the index of that match in the TST
                if (longerCode == TST.EMPTY) {
                    break;
                }

                match = longer;
                code = longerCode;
            }

            // Write the code using 8 bits
            BinaryStdOut.write(code, codeWidth);


            // Create the next possible pattern as long as the dictionary isn't full yet
            if (position + match.length() < input.length() && nextCode < maxCodes) {
                // Build new pattern with the current word and the first character of next pattern
                // Add to dictionary with next available code and then increment nextCode
                String newPattern = match + input.charAt(position + match.length());
                dictionary.insert(newPattern, nextCode);
                nextCode++;
            }

            // Jump past the pattern we just encoded
            position += match.length();
        }

        // Write out the special code to signal end of file
        BinaryStdOut.write(EOF_Code, codeWidth);
        BinaryStdOut.close();
    }

    private static void expand() {
        // Array to store dictionary with the code into the string
        String[] dictionary = new String[256];
        int EOF_Code = 128;

        // Initialize the dictionary with single characters
        for (int i = 0; i < 256; i++) {
            dictionary[i] = "" + (char) i;
        }
        // Same method as above
        int nextCode = 129;
        int codeWidth = 8;
        int maxCodes = 256;

        // Read first code from the compressed file
        // Look up what string it represents
        // Output that string
        int currentCode = BinaryStdIn.readInt(codeWidth);
        String previous = dictionary[currentCode];
        BinaryStdOut.write(previous);
        currentCode = BinaryStdIn.readInt(codeWidth);

        // Read next code and repeat process above until you hit the EOF marker
        while (currentCode != EOF_Code) {
            String currentString;

            // To address special case: store the current code as the previous pattern and the first char of the previous one
            if (currentCode == nextCode) {
                currentString = previous + previous.charAt(0);
            } else {
                // Otherwise simply look up the string for this code
                currentString = dictionary[currentCode];
            }

            // Output decoded string
            BinaryStdOut.write(currentString);

            // Now build the pattern by the previous word and the first string of the current word
            if (nextCode < maxCodes) {
                dictionary[nextCode] = previous + currentString.charAt(0);
                // Increment for the next pattern
                nextCode++;
            }
            // Current becomes the previous for the next loop
            previous = currentString;
            // Read next code from compressed file
            currentCode = BinaryStdIn.readInt(codeWidth);
        }
        BinaryStdOut.close();
    }

    public static void main(String[] args) {
        if (args[0].equals("-")) compress();
        else if (args[0].equals("+")) expand();
        else throw new IllegalArgumentException("Illegal command line argument");
    }
}
