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

import com.gmail.walles.johan.exactype.util.LoggingUtils;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class StatsTrackerTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @BeforeClass
    public static void beforeClass() {
        LoggingUtils.setUpLogging();
    }

    private static void assertFileContents(File file, Object ... contents) throws IOException {
        if (contents.length % 2 != 0) {
            throw new IllegalArgumentException("Even number of arguments required, got " + contents.length);
        }

        Map<String, Integer> counts = new HashMap<>();
        for (int i = 0; i < contents.length; i += 2) {
            String character = (String)contents[i];
            int count = (int)contents[i + 1];
            counts.put(character, count);
        }

        Assert.assertThat(new StatsTracker(file).getCounts(), Matchers.is(counts));
    }

    @Test
    public void testCountCharacter() throws IOException {
        File backingFile = folder.newFile();
        StatsTracker testMe = new StatsTracker(backingFile);
        assertFileContents(backingFile);

        testMe.countCharacter("a");
        assertFileContents(backingFile, "a", 1);

        testMe.countCharacter("a");
        assertFileContents(backingFile, "a", 2);

        testMe.countCharacter("b");
        assertFileContents(backingFile, "a", 2, "b", 1);

        testMe.countCharacter("b");
        testMe.countCharacter("b");
        assertFileContents(backingFile, "a", 2, "b", 3);
    }

    @Test
    public void testCountColon() throws IOException {
        File backingFile = new File(folder.getRoot(), "backing-file.txt");
        StatsTracker testMe = new StatsTracker(backingFile);

        testMe.countCharacter(":");
        assertFileContents(backingFile, ":", 1);
        testMe.countCharacter(":");
        assertFileContents(backingFile, ":", 2);
    }

    @Test
    public void testCountWord() throws IOException {
        File backingFile = new File(folder.getRoot(), "backing-file.txt");
        StatsTracker testMe = new StatsTracker(backingFile);

        testMe.countCharacter("word");
        assertFileContents(backingFile, "word", 1);
        testMe.countCharacter("word");
        assertFileContents(backingFile, "word", 2);
    }

    @Test
    public void testPersistence() throws IOException {
        File backingFile = new File(folder.getRoot(), "initial");
        StatsTracker initial = new StatsTracker(backingFile);
        initial.countCharacter("c");
        initial.countCharacter("c");
        initial.countCharacter("c");

        File backingFileCopy = new File(folder.getRoot(), "secondary");
        Assert.assertThat(backingFile.renameTo(backingFileCopy), Matchers.is(true));

        assertFileContents(backingFileCopy, "c", 3);
    }

    @Test
    public void testReadNoFile() throws IOException {
        File doesNotExist = new File(folder.getRoot(), "doesnotexist");

        // This is what happens if somebody asks for stats before having run anything, "no file"
        // should be treated as "no counts".
        assertFileContents(doesNotExist);
    }
}
