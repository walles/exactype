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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * An single threaded executor providing its own queue.
 */
public class ExactypeExecutor {
    private final BlockingQueue<Runnable> workQueue;
    private final Executor executor;

    public ExactypeExecutor() {
        workQueue = new LinkedBlockingQueue<>();
        executor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS, workQueue);
    }

    public void execute(Runnable runnable) {
        executor.execute(runnable);
    }

    public boolean isEmpty() {
        return workQueue.isEmpty();
    }
}
