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

package com.gmail.walles.johan.exactype.gestures;

/**
 * Created by johan on 2015-12-18.
 */
public interface GestureListener {
    void onSwipe(float dx, float dy);

    void onStartSwipe(float dx, float dy);

    void onSingleTap(float x, float y);

    void onLongPress(float x, float y);

    void onLongLongPress(float x, float y);

    void onLongPressUp(float x, float y);

    void onHold(float x, float y);

    void onDown();

    void onMove(float x, float y);

    void onUp();
}
