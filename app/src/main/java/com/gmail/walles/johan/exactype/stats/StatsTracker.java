/*
 * Copyright 2020 Johan Walles <johan.walles@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gmail.walles.johan.exactype.stats;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

class StatsTracker {
    private final File backingFile;

    public StatsTracker(File backingFile) {
        this.backingFile = backingFile;
    }

    public void countCharacter(char character) {
        Map<Character, Integer> counts;
        try {
            counts = readCountsFromFile(backingFile);
        } catch (IOException e) {
            Timber.w(e,
                "Failed reading stats from file: %s",
                backingFile.getAbsolutePath());
            return;
        }

        // Bump the count
        Integer count = counts.get(character);
        if (count == null) {
            count = 0;
        }
        counts.put(character, count + 1);

        try {
            writeCountsToFile(counts);
        } catch (IOException e) {
            Timber.w(e,
                "Failed writing stats to file: %s",
                backingFile.getAbsolutePath());
        }
    }

    public static Map<Character, Integer> readCountsFromFile(File countsFile) throws IOException {
        Map<Character, Integer> returnMe = new HashMap<>();
        try (FileReader fileReader = new FileReader(countsFile);
             BufferedReader in = new BufferedReader(fileReader)) {
            while(true) {
                String line = in.readLine();
                if (line == null) {
                    break;
                }

                final String COMPLAINT = "Line not on 'x: 12345' format: ";
                if (line.length() < "x: 5".length()) {
                    // Line not long enough to contain what we want
                    throw new IOException(COMPLAINT + line);
                }

                char character = line.charAt(0);
                if (line.charAt(1) != ':' || line.charAt(2) != ' ') {
                    throw new IOException(COMPLAINT + line);
                }

                int count;
                try {
                    count = Integer.parseInt(line.substring(3));
                } catch (NumberFormatException e) {
                    throw new IOException(COMPLAINT + line, e);
                }

                returnMe.put(character, count);
            }
        }

        return returnMe;
    }

    private void writeCountsToFile(Map<Character, Integer> counts) throws IOException {
        File tempFile = new File(backingFile.getAbsolutePath() + ".tmp");
        try (FileWriter fileWriter = new FileWriter(tempFile);
             PrintWriter out = new PrintWriter(fileWriter)) {
            for (Map.Entry<Character, Integer> entry: counts.entrySet()) {
                out.print(entry.getKey());
                out.print(": ");
                out.println(entry.getValue());
            }
        }

        if (!tempFile.renameTo(backingFile)) {
            throw new IOException(
                "Rename failed: " + tempFile.getAbsolutePath() + "->" + backingFile.getAbsolutePath());
        }
    }
}
