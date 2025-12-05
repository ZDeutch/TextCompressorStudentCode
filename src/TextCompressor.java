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
        // Read all the input into a string and then break that down into chars
        String input = BinaryStdIn.readString();
        Character[] chars = new Character[input.length()];
        for (int i = 0; i < input.length(); i++) {
            chars[i] = input.charAt(i);
        }

        // Count how often each character appears through extended ASCII
        int[] frequency = new int[256];
        for (int i = 0; i < chars.length; i++) {
            frequency[chars[i]]++;
        }

        // Find which characters are actually used to narrow down possible characters
        ArrayList<Character> uniqueChars = new ArrayList<>();
        for (int i = 0; i < frequency.length; i++) {
            if (frequency[i] > 0) {
                uniqueChars.add((char) i);
            }
        }

        // Sort characters by frequency through a simple bubble sort algorithm
        // This isn't most efficient but the list is relatively small so it's fine
        for (int i = 0; i < uniqueChars.size() - 1; i++) {
            for (int j = 0; j < uniqueChars.size() - i - 1; j++) {
                char first = uniqueChars.get(j);
                char second = uniqueChars.get(j + 1);
                if (frequency[first] < frequency[second]) {
                    // Swap the second and first chars if the second is more frequent
                    uniqueChars.set(j, second);
                    uniqueChars.set(j + 1, first);
                }
            }
        }

        // Assign shorter codes to more frequent characters through a hashmap
        HashMap<Character, Integer> charCode = new HashMap<>();
        for(int i = 0; i < uniqueChars.size(); i++) {
            charCode.put(uniqueChars.get(i), i);
        }

        // Figure out how many bits are needed for the codes
        int numChars = uniqueChars.size();
        int bits = 1;

        // Need some way to figure out how many bits we need for codes
        // Not sure how to do that yet

        // Write how many unique characters there are
        BinaryStdOut.write(numChars, 8);

        // Then write each character in order of their code
        for(int i = 0; i < uniqueChars.size(); i++) {
            BinaryStdOut.write(uniqueChars.get(i), 8);
        }

        // Write how many bits per code
        // Again I'm not sure how to do this yet
        BinaryStdOut.write(bits, 8);

        // Write original length
        BinaryStdOut.write(chars.length);


        BinaryStdOut.close();
    }

    private static void expand() {

        // TODO: Complete the expand() method

        BinaryStdOut.close();
    }

    public static void main(String[] args) {
        if (args[0].equals("-")) compress();
        else if (args[0].equals("+")) expand();
        else throw new IllegalArgumentException("Illegal command line argument");
    }
}
