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

package com.gmail.walles.johan.exactype.activities;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.GuardedBy;
import androidx.annotation.VisibleForTesting;
import timber.log.Timber;

public class StatsTracker {
    @GuardedBy("countQueueLock")
    private final List<String> countQueue = new ArrayList<>();
    private final Object countQueueLock = new Object();

    private final File backingFile;

    @VisibleForTesting
    StatsTracker(File backingFile) {
        this.backingFile = backingFile;

        final Thread flusher = new Thread(() -> {
            try {
                Timber.i("Stats flushing thread started");
                threadFlusher();
            } catch (InterruptedException e) {
                Timber.w(e, "Stats flushing thread interrupted");
            }
        }, "Stats flusher for " + backingFile.getName());
        flusher.setDaemon(true);
        flusher.start();
    }

    public StatsTracker(Context context) {
        this(getBackingFile(context));
    }

    private static File getBackingFile(Context context) {
        return new File(context.getApplicationInfo().dataDir, "stats.txt");
    }

    private void threadFlusher() throws InterruptedException {
        //noinspection InfiniteLoopStatement
        while (true) {
            synchronized (countQueueLock) {
                if (countQueue.isEmpty()) {
                    // Wait for keypresses to show up
                    countQueueLock.wait();
                }
            }

            // We have been notified that there's something to flush, wait a bit to let more things
            // accumulate. It's probably good if the wait time is a good bit longer than the time it
            // takes to flush.
            //noinspection BusyWait
            Thread.sleep(3000);

            flush();
        }
    }

    @VisibleForTesting
    void flush() {
        long t0 = System.currentTimeMillis();
        List<String> toFlushNow;
        synchronized (countQueueLock) {
            toFlushNow = new ArrayList<>(countQueue);
            countQueue.clear();
        }

        countCharactersSynchronously(toFlushNow);
        long t1 = System.currentTimeMillis();
        long dtMillis = t1 - t0;
        Timber.v("Flushed %d stats entries in %dms", toFlushNow.size(), dtMillis);
    }

    public void countCharacter(String character) {
        synchronized (countQueueLock) {
            countQueue.add(character);
            countQueueLock.notifyAll();
        }
    }

    private void countCharactersSynchronously(Collection<String> characters) {
        if (characters.isEmpty()) {
            return;
        }

        Map<String, Integer> counts;
        try {
            counts = getCounts(backingFile);
        } catch (IOException e) {
            Timber.w(e,
                "Failed reading stats from file: %s",
                backingFile.getAbsolutePath());
            return;
        }

        // Bump the counts
        for (String character: characters) {
            Integer count = counts.get(character);
            if (count == null) {
                count = 0;
            }
            counts.put(character, count + 1);
        }

        try {
            writeCountsToFile(counts);
        } catch (IOException e) {
            Timber.w(e,
                "Failed writing stats to file: %s",
                backingFile.getAbsolutePath());
            //noinspection UnnecessaryReturnStatement
            return;
        }
    }

    public static Map<String, Integer> getCounts(Context context) throws IOException {
        return getCounts(getBackingFile(context));
    }

    @VisibleForTesting
    static Map<String, Integer> getCounts(File backingFile) throws IOException {
        Map<String, Integer> returnMe = new HashMap<>();
        try (FileReader fileReader = new FileReader(backingFile);
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

                int lastColonIndex = line.lastIndexOf(':');
                if (lastColonIndex == -1) {
                    // No colon found
                    throw new IOException(COMPLAINT + line);
                }
                if (lastColonIndex == 0) {
                    // Colon in first position
                    throw new IOException(COMPLAINT + line);
                }
                if (line.length() < (lastColonIndex + 2)) {
                    // No room for any number
                    throw new IOException(COMPLAINT + line);
                }

                String character = line.substring(0, lastColonIndex);

                if (line.charAt(lastColonIndex + 1) != ' ') {
                    // No ": " after the word
                    throw new IOException(COMPLAINT + line);
                }

                int count;
                try {
                    count = Integer.parseInt(line.substring(lastColonIndex + 2));
                } catch (NumberFormatException e) {
                    throw new IOException(COMPLAINT + line, e);
                }

                returnMe.put(character, count);
            }
        } catch (FileNotFoundException e) {
            // This happens if somebody asks for stats before having pressed any key
            Timber.w(e,
                "Stats file not found, pretending it was empty: %s",
                backingFile.getAbsolutePath());
            return returnMe;
        }

        return returnMe;
    }

    private void writeCountsToFile(Map<String, Integer> counts) throws IOException {
        File tempFile = new File(backingFile.getAbsolutePath() + ".tmp");
        try (FileWriter fileWriter = new FileWriter(tempFile);
             PrintWriter out = new PrintWriter(fileWriter)) {
            for (Map.Entry<String, Integer> entry: counts.entrySet()) {
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
