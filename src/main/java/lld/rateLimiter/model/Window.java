package lld.rateLimiter.model;

public class Window {
    long windowStart; // start time (epoch millis) of the current window
    int count;        // requests seen in this window

    public Window(long windowStart) {
        this.windowStart = windowStart;
        this.count = 0;
    }
}

