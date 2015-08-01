public class Events implements Comparable {

    private Game game;
    private String event;
    private int minutesSinceMidnight;
    private int minutesSinceGameStart;

    public Events(Game game, String event, int minutesSinceMidnight, int minutesSinceGameStart) {
        this.game = game;
        this.event = event;
        this.minutesSinceMidnight = minutesSinceMidnight;
        this.minutesSinceGameStart = minutesSinceGameStart;
    }

    public Game getGame() {
        return game;
    }

    public String getEvent() {
        return event;
    }

    public String getTimeString() {
        int hours = minutesSinceMidnight / 60;
        double i = (minutesSinceMidnight / 60.0);
        double j = i - hours;
        int minutes = (int) Math.round(j * 60);

        //            hours             :      minutes                    am or pm
        return String.valueOf(hours) + ":" + pad(minutes) + (minutesSinceMidnight >= 720 ? "pm" : "am");
    }

    private String pad(int num) {
        return num < 10 ? "0" + String.valueOf(num) : String.valueOf(num);
    }

    public int getMinutesSinceMidnight() {
        return minutesSinceMidnight;
    }

    public int getMinutesSinceGameStart() {
        return minutesSinceGameStart;
    }

    @Override
    public int compareTo(Object o) {
        return this.getMinutesSinceMidnight() - ((Events) o).getMinutesSinceMidnight();
    }
}
