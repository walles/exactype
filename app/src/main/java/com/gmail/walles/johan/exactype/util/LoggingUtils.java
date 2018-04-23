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

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.gmail.walles.johan.exactype.BuildConfig;

import io.fabric.sdk.android.Fabric;
import timber.log.Timber;

public class LoggingUtils {
    public static final boolean IS_CRASHLYTICS_ENABLED = isCrashlyticsEnabled();

    private static Class<Timber> initializedLoggingClass = null;

    private LoggingUtils() {
        // Don't let people instantiate this class
    }

    private static boolean isCrashlyticsEnabled() {
        if (!BuildConfig.DEBUG) {
            // We don't want Internet access in release builds, so no Crashlytics there
            return false;
        }
        if (EmulatorUtils.IS_ON_EMULATOR) {
            return false;
        }
        if (!EmulatorUtils.IS_ON_ANDROID) {
            return false;
        }

        return true;
    }

    public static void logCustom(CustomEvent event) {
        if (EmulatorUtils.IS_ON_ANDROID) {
            Timber.d("Custom Event: %s", event.toString());
        }
        if (IS_CRASHLYTICS_ENABLED) {
            event.putCustomAttribute("App Version", BuildConfig.VERSION_NAME); //NON-NLS
            Answers.getInstance().logCustom(event);
        }
    }

    public static void setUpLogging(Context context) {
        Timber.Tree tree;
        if (IS_CRASHLYTICS_ENABLED) {
            tree = new CrashlyticsTree(context);
        } else {
            tree = new LocalTree();
        }

        if (initializedLoggingClass != Timber.class) {
            initializedLoggingClass = Timber.class;
            Timber.plant(tree);
            Timber.v("Logging tree planted: %s", tree.getClass());
        }

        Timber.i("Logging configured: Crashlytics=%b, DEBUG=%b, Emulator=%b, Android=%b",
            IS_CRASHLYTICS_ENABLED,
            BuildConfig.DEBUG,
            EmulatorUtils.IS_ON_EMULATOR,
            EmulatorUtils.IS_ON_ANDROID);
    }

    private static class CrashlyticsTree extends Timber.Tree {
        public CrashlyticsTree(Context context) {
            Fabric.with(context, new Crashlytics());
        }

        @Override
        protected void log(int priority, String tag, String message, Throwable t) {
            if (BuildConfig.DEBUG) {
                tag = "DEBUG";
            } else if (TextUtils.isEmpty(tag)) {
                tag = "Exactype";
            }

            // This call logs to *both* Crashlytics and LogCat, and will log the Exception backtrace
            // to LogCat on exceptions.
            Crashlytics.log(priority, tag, message);

            if (t != null) {
                Crashlytics.logException(t);
            }
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
