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

package com.gmail.walles.johan.exactype.util;

import android.text.TextUtils;
import android.util.Log;

import com.gmail.walles.johan.exactype.BuildConfig;

import timber.log.Timber;

public class LoggingUtils {
    private static Class<Timber> initializedLoggingClass = null;

    public static void setUpLogging() {
        Timber.Tree tree;
        tree = new LocalTree();

        if (initializedLoggingClass != Timber.class) {
            initializedLoggingClass = Timber.class;
            Timber.plant(tree);
            Timber.v("Logging tree planted: %s", tree.getClass());
        }
    }

    private static class LocalTree extends Timber.Tree {
        @Override
        protected void log(int priority, String tag, String message, Throwable t) {
            if (BuildConfig.DEBUG) {
                tag = "DEBUG";
            } else if (TextUtils.isEmpty(tag)) {
                tag = "Exactype";
            }

            if (t != null) {
                message += "\n" + Log.getStackTraceString(t);
            }
            Log.println(priority, tag, message);
        }
    }
}
