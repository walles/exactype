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

import java.io.PrintWriter;
import java.io.StringWriter;

import timber.log.Timber;

public class LoggingUtils {
    /**
     * If we ever go back to Crashlytics, this class should be replaced by its Crashlytics
     * counterpart.
     */
    public static class CustomEvent {
        private final StringBuilder builder = new StringBuilder();

        public CustomEvent(String name) {
            builder.append("Name: ").append(name);
        }

        public CustomEvent putCustomAttribute(String name, float value) {
            builder.append("  ").append(name).append(": ").append(value);
            return this;
        }

        @Override
        public String toString() {
            return "CustomEvent: " + builder.toString();
        }
    }

    private static Class<Timber> initializedLoggingClass = null;

    private LoggingUtils() {
        // Don't let people instantiate this class
    }

    public static void logCustom(CustomEvent event) {
        if (EmulatorUtils.IS_ON_ANDROID) {
            Timber.d("Custom Event: %s", event.toString());
        }
    }

    public static void setUpLogging() {
        Timber.Tree tree;
        tree = new LocalTree();

        if (initializedLoggingClass != Timber.class) {
            initializedLoggingClass = Timber.class;
            Timber.plant(tree);
            Timber.v("Logging tree planted: %s", tree.getClass());
        }

        Timber.i("Logging configured: DEBUG=%b, Emulator=%b, Android=%b",
            BuildConfig.DEBUG,
            EmulatorUtils.IS_ON_EMULATOR,
            EmulatorUtils.IS_ON_ANDROID);
    }

    private static class LocalTree extends Timber.Tree {
        @Override
        protected void log(int priority, String tag, String message, Throwable throwable) {
            if (BuildConfig.DEBUG) {
                tag = "DEBUG";
            } else if (TextUtils.isEmpty(tag)) {
                tag = "Exactype";
            }

            String stackTraceString;
            if (throwable == null) {
                stackTraceString = "";
            } else if (EmulatorUtils.IS_ON_ANDROID) {
                stackTraceString = "\n" + Log.getStackTraceString(throwable);
            } else {
                // We have a throwable but we are not on Android, assume unit testing
                StringWriter stringWriter = new StringWriter();
                PrintWriter printWriter = new PrintWriter(stringWriter);
                throwable.printStackTrace(printWriter);
                printWriter.close();
                stackTraceString = "\n" + stringWriter.toString();
            }

            if (EmulatorUtils.IS_ON_ANDROID) {
                Log.println(priority, tag, message + stackTraceString);
            } else {
                System.err.println("[" + priority + "] " + tag + ": " + message + stackTraceString);
            }
        }
    }
}
