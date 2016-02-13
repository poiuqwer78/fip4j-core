package ch.poiuqwer.saitek.fip4j;

import java.util.Collection;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Copyright 2015 Hermann Lehner
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@SuppressWarnings("unused")
public class CallbackHandler {

    private static final CallbackHandler instance = new CallbackHandler();

    public static CallbackHandler getInstance() {
        return instance;
    }

    private AbstractExecutorService executorService = new ForkJoinPool();

    public void setExecutorService(AbstractExecutorService executorService) {
        this.executorService = executorService;
    }

    public static <T> void execute(Consumer<T> callback, T argument) {
        instance.executorService.execute(() -> callback.accept(argument));
    }

    public static <T> void executeAll(Collection<Consumer<T>> callbacks, T argument) {
        for (Consumer<T> callback : callbacks) {
            execute(callback, argument);
        }
    }

    public static <T, U> void execute(BiConsumer<T, U> callback, T argumentT, U argumentU) {
        instance.executorService.execute(() -> callback.accept(argumentT, argumentU));
    }

    public static <T, U> void executeAll(Collection<BiConsumer<T, U>> callbacks, T argumentT, U argumentU) {
        for (BiConsumer<T, U> callback : callbacks) {
            execute(callback, argumentT, argumentU);
        }
    }

}
