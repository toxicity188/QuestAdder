package kor.toxicity.questadder.api.concurrent;

import org.jetbrains.annotations.NotNull;

public interface LazyRunnable {
    LazyRunnable EMPTY = new LazyRunnable() {
        @Override
        public long getDelay() {
            return 0;
        }

        @Override
        public void run() {

        }
    };
    static @NotNull LazyRunnable emptyOf(@NotNull Runnable runnable) {
        return new LazyRunnable() {
            @Override
            public long getDelay() {
                return 0;
            }

            @Override
            public void run() {
                runnable.run();
            }
        };
    }
    long getDelay();
    void run();
}
