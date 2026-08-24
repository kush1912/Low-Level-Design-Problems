package lld.DesignProblems.bookmyshow.model;

public class Show {
    private final Movie movie;
    private final long startTime;
    private final long endTime;
    private final Screen screen;

    public Show(Movie movie, long startTime, long endTime, Screen screen){
        if (endTime <= startTime) {
            throw new IllegalArgumentException(
                    "Show end time must be after start time"
            );
        }

        this.movie = movie;
        this.startTime = startTime;
        this.endTime = endTime;
        this.screen = screen;
    }

    public boolean overlapsWith(Show other) {
        return startTime < other.endTime
                && other.startTime < endTime;
    }

    public Movie getMovie(){
        return movie;
    }

    public long getStartTime(){
        return startTime;
    }

    public long getEndTime(){
        return endTime;
    }

    public Screen getScreen(){
        return screen;
    }
}
