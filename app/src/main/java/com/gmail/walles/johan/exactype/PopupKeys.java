/*
 * Copyright 2016 Johan Walles <johan.walles@gmail.com>
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

import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * What popup keys should be available for which keys of a given keyboard?
 */
public class PopupKeys {
    private final Map<Character, String> popupKeys;

    public PopupKeys(String upper[], String lower[], String numeric[]) {
        popupKeys = new HashMap<>();

        // FIXME: Maybe we should implicitly have the base key at the end of each of these lists?

        // FIXME: The popups shouldn't include keys that are already on any of the primary keyboards

        popupKeys.put('a', "@áàa");
        popupKeys.put('A', "@ÁÀA");
        popupKeys.put('e', "éèëe");
        popupKeys.put('E', "ÉÈË€E");
    }

    @Nullable
    public String getPopupKeysForKey(char baseKey) {
        String keys = popupKeys.get(baseKey);
        if (keys != null && keys.isEmpty()) {
            keys = null;
        }
        return keys;
    }
}
