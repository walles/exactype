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

public class GestureListenerAdapter implements GestureListener {
    @Override
    public void onSwipe(float dx, float dy) {}

    @Override
    public void onStartSwipe(float dx, float dy) {}

    @Override
    public void onSingleTap(float x, float y) {}

    @Override
    public void onLongPress(float x, float y) {}

    @Override
    public void onLongLongPress(float x, float y) {}

    @Override
    public void onLongPressUp(float x, float y) {}

    @Override
    public void onHold(float x, float y) {}

    @Override
    public void onDown() {}

    @Override
    public void onMove(float x, float y) {}

    @Override
    public void onUp() {}
}
