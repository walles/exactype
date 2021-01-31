/*
 * Copyright 2015 Johan Walles <johan.walles@gmail.com>
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

package com.gmail.walles.johan.exactype;

import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.HashSet;
import java.util.Set;

public class ExactypeTest {
    @Test
    public void testNumericLayout() {
        Assert.assertEquals(3, Exactype.NUMERIC.length);

        Assert.assertEquals("1234567890", Exactype.NUMERIC[0]);

        int line2_length = Exactype.NUMERIC[1].length();
        int line3_length = Exactype.NUMERIC[2].length() + 2; // Add mode switch and backspace
        int diff = line2_length - line3_length;

        Assert.assertTrue("Move some keys from line 2 to line 3", diff <= 1);
        Assert.assertTrue("Move some keys from line 3 to line 2", diff >= -1);
    }

    @Test
    public void checkNumericLayoutForDups() {
        String lastTwoLines = Exactype.NUMERIC[1] + Exactype.NUMERIC[2];
        for (char checkMe: lastTwoLines.toCharArray()) {
            long checkMeCount = lastTwoLines.chars().filter(c -> c == checkMe).count();
            Assert.assertEquals(
                "Char must occur exactly once on the numeric keyboard: <" + checkMe + ">",
                1, checkMeCount);
        }
    }

    private void assertIndexComparable(int characterIndexInFirst, String first, String second) {
        if (characterIndexInFirst == 0) {
            // First is always comparable
            return;
        }

        if (characterIndexInFirst == (first.length() - 2)) {
            // Last index is always comparable
            return;
        }

        // Character is neither first nor last
        Assert.assertEquals(
            String.format("Should have same lengths for 0 based index %d to be comparable:\n%s\n%s",
                characterIndexInFirst, first, second),
            first.length(),
            second.length());
    }

    /**
     * Check that long pressing character will get you symbol.
     */
    private void assertSymbolUnderCharacter(char symbol, char character) {
        int rowIndex = 1;
        String characterRow = Exactype.UNSHIFTED[rowIndex];
        if (!characterRow.contains(Character.toString(character))) {
            rowIndex++;
            characterRow = Exactype.UNSHIFTED[rowIndex];
        }
        Assert.assertThat(characterRow, CoreMatchers.containsString(Character.toString(character)));
        int characterIndex = characterRow.indexOf(character);
        assert characterIndex >= 0;

        String symbolRow = Exactype.NUMERIC[rowIndex];
        assertIndexComparable(characterIndex, characterRow, symbolRow);

        int symbolIndex = symbolRow.indexOf(symbol);
        assert symbolIndex >= 0;

        Assert.assertEquals(String.format(
            "Symbol '%c' must be under char '%c'\n%s\n%s", symbol, character, characterRow, symbolRow),
            characterIndex, symbolIndex);
    }

    /**
     * Check that first and second are right next to each other in that order.
     */
    private void assertSymbolOrder(char first, char second) {
        String symbolRow = Exactype.NUMERIC[1];
        if (!symbolRow.contains(Character.toString(first))) {
            symbolRow = Exactype.NUMERIC[2];
        }

        Assert.assertThat(symbolRow, CoreMatchers.containsString("" + first + second));
    }

    @Test
    public void testNoDuplicateSymbols() {
        Set<Character> symbols = new HashSet<>();
        for (int row = 1; row <= 2; row++) {
            for (char symbol : Exactype.NUMERIC[row].toCharArray()) {
                assert !symbols.contains(symbol);
                symbols.add(symbol);
            }
        }
    }

    @Test
    public void testErgonomics() {
        // Because * looks kind of like x
        assertSymbolUnderCharacter('*', 'x');

        // Because / is a partial Z
        assertSymbolUnderCharacter('/', 'z');

        // Because @ looks like a decorated a
        assertSymbolUnderCharacter('@', 'a');

        // What else?
        assertSymbolUnderCharacter('$', 's');

        // Because ( looks like a C
        assertSymbolUnderCharacter('(', 'c');

        // Because f isn't entirely unlike +
        assertSymbolUnderCharacter('+', 'f');

        // Similarities and commonalities
        assertSymbolUnderCharacter('!', '.');
        assertSymbolUnderCharacter('?', ',');
        assertSymbolAboveCharacter(':', '.');
        assertSymbolAboveCharacter(';', ',');

        assertSymbolOrder('(', ')');
        assertSymbolOrder('<', '>');

        // This matches the order of , and .
        assertSymbolOrder(';', ':');
    }

    /**
     * Check that long pressing above character will get you symbol.
     */
    private void assertSymbolAboveCharacter(char expectedSymbol, char character) {
        String middleSymbolRow = Exactype.NUMERIC[1];

        // "1" for "123" button, "B" for backspace button
        String bottomCharacterRow = "1" + Exactype.UNSHIFTED[2] + "B";

        int index = bottomCharacterRow.indexOf(character);
        assert index >= 0;

        assertIndexComparable(index, bottomCharacterRow, middleSymbolRow);

        // Are they the same?
        char actualSymbol = middleSymbolRow.charAt(index);
        Assert.assertEquals(
            String.format("Expected %c when long pressing above %c but found %c",
                expectedSymbol, character, actualSymbol),
            expectedSymbol, actualSymbol);
    }

    @Test
    public void testOnActionTapped() {
        final InputConnection inputConnection = Mockito.mock(InputConnection.class);
        final EditorInfo editorInfo = new EditorInfo();
        editorInfo.imeOptions = EditorInfo.IME_ACTION_SEARCH + EditorInfo.IME_FLAG_FORCE_ASCII;
        Exactype exactype = new Exactype() {
            @Override
            public InputConnection getCurrentInputConnection() {
                return inputConnection;
            }

            @Override
            public EditorInfo getCurrentInputEditorInfo() {
                return editorInfo;
            }

            @Override
            public void enqueue(Runnable runnable) {
                runnable.run();
            }
        };

        exactype.onActionTapped();

        Mockito.verify(inputConnection).performEditorAction(EditorInfo.IME_ACTION_SEARCH);
    }
}
