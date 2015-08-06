import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Timer;

public class Main {

    static ArrayList<Game> allGames = new ArrayList<Game>();
    static Timer timer = new Timer();
    static int currentTimeSinceMidnightinMinutes;
    static int count = 0;
    static List<String> descriptions = Arrays.asList("A storming break by the centre leads to a try under the posts.",
                                                     "Fantastic long range try under the posts in Calypso style.",
                                                     "A beautifully worked play ending in a try just left of the posts.",
                                                     "A well formed lineout drive finishes with a try to the openside flanker.",
                                                     "Try out wide to the number 8 after a tighthead scrum.",
                                                     "Sustained pressure from the forwards left the defense vulnerable out wide where the right wing strolled over for an easy try.");

    public static void main(String[] args) {
        start();
    }

    private static void start() {
        String currentTime = new SimpleDateFormat("HH:mm").format(Calendar.getInstance().getTime());
        currentTimeSinceMidnightinMinutes = Integer.parseInt(currentTime.substring(0, 2)) * 60 +
                Integer.parseInt(currentTime.substring(3, 5));
        allGames = getAllGames();
        setTimers();
    }

    private static ArrayList<Game> getAllGames() {
        ArrayList<Game> games = new ArrayList<>();
        String result = "";

        try {
            HttpClient httpclient = HttpClientBuilder.create().build();
            HttpPost httppost = new HttpPost("http://www.possumpam.com/rugby-scoring-app-scripts/Test/get_all_games.php");

            // Return all games between start and end dates
            HttpResponse response = httpclient.execute(httppost);

            // Retrieve json data to be processed
            HttpEntity entity = response.getEntity();
            InputStream is = entity.getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            is.close();
            result = sb.toString();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "An error has occurred while retrieving the game list.\n" +
                    "Please check your internet connection and try again later.");
            e.printStackTrace();
        }

        try {
            // Check if any games were retrieved. This prevents most JSONExceptions.
            if (!result.equals("")) {
                JSONParser parser = new JSONParser();
                try {
                    Object obj = parser.parse(result);
                    JSONArray array = (JSONArray) obj;
                    for (Object anArray : array) {
                        JSONObject g = (JSONObject) anArray;
                        /*games.add(new Game(
                                Long.parseLong(String.valueOf(g.get("GameID"))),
                                String.valueOf(g.get("homeTeamName")),
                                Integer.parseInt(String.valueOf(g.get("homeTeamScore"))),
                                String.valueOf(g.get("awayTeamName")),
                                Integer.parseInt(String.valueOf(g.get("awayTeamScore"))),
                                String.valueOf(g.get("ref")),
                                String.valueOf(g.get("assRef1")),
                                String.valueOf(g.get("assRef2")),
                                String.valueOf(g.get("location")),
                                Integer.parseInt(String.valueOf(g.get("minutesPlayed"))),
                                String.valueOf(g.get("time")),
                                new ArrayList<>(),
                                String.valueOf(g.get("changed"))));*/
                        games.add(new Game(
                                Long.parseLong(String.valueOf(g.get("GameID"))),
                                String.valueOf(g.get("homeTeamName")),
                                0,
                                String.valueOf(g.get("awayTeamName")),
                                0,
                                String.valueOf(g.get("ref")),
                                String.valueOf(g.get("assRef1")),
                                String.valueOf(g.get("assRef2")),
                                String.valueOf(g.get("location")),
                                0,
                                String.valueOf(g.get("time")),
                                new ArrayList<>(),
                                String.valueOf(g.get("changed"))));
                    }
                } catch (ParseException pe) {
                    System.out.println("position: " + pe.getPosition());
                    pe.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return games;
    }

    private static void setTimers() {
        //String today = new SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().getTime());
        String today = "20150620";

        for (Game g : allGames) {
            String gameDate = String.valueOf(g.getGameID()).substring(0, 8);
            if (gameDate.equals(today)) {
                String gameStartTime = g.getTime();
                if (gameStartTime.length() > 0) {
                    // find time until game starts in milliseconds

                    int timeDiff = (g.getMinutesSinceMidnight() - currentTimeSinceMidnightinMinutes) * 60 * 1000;

                    // If the game has already started then set simulation to start now
                    timeDiff = (timeDiff < 0 ? 0 : timeDiff);

                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            gameSimulator(g);
                        }
                    }, timeDiff + g.getMinutesSinceMidnight());
                }
            }
        }
    }

    private static void gameSimulator(Game g) {
        Random rand = new Random();

        int numTries = rand.nextInt(10);
        int numPen = rand.nextInt(10);
        // Theres a 10% chance of a drop goal
        int n = rand.nextInt(1000) + 1;
        int numDrop = 0;
        if (n == 1) {
            numDrop = 3;
        } else if (n < 10) {
            numDrop = 2;
        } else if (n < 100) {
            numDrop = 1;
        }

        // Simulate the tries
        for (int i = 1; i <= numTries; i += 1) {
            // Did the home or away team score it (0=home, 1=away)
            String event = (rand.nextInt(2) == 0 ? "homeTry" : "awayTry");
            int descNum = rand.nextInt(4);
            String description = descriptions.get(descNum);
            // Try is scored at a time between 1 and 80 minutes (inclusive)
            int timeScored = rand.nextInt(80) + 1;
            int timeDiff = (g.getMinutesSinceMidnight() + timeScored - currentTimeSinceMidnightinMinutes) * 60 * 1000;

            // If the game has already started then set simulation to start now
            timeDiff = (timeDiff < 0 ? 0 : timeDiff);

            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    uploadEvent(event, g, timeScored, description);
                }
            }, timeDiff + timeScored * 2 + g.getMinutesSinceMidnight());

            // Try is converted 65-95% of the time based on kick position which is based on which
            // description is chosen above.
            int conNum = rand.nextInt(100);
            int conPer = (95 - 9 * descNum);
            if (conNum >= conPer) {
                String conevent = event.substring(0,4) + "Conversion";
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        uploadEvent(conevent, g, timeScored, "");
                    }
                }, timeDiff + timeScored * 2 + 1 + g.getMinutesSinceMidnight());
            }
        }

        // Simulate the penalties
        for (int i = 1; i < numPen; i += 1) {
            // Did the home or away team score it (0=home, 1=away)
            String event = (rand.nextInt(1) == 0 ? "homePenalty" : "awayPenalty");
            // Try is scored at a time between 1 and 80 minutes (inclusive)
            int timeScored = rand.nextInt(80) + 1;
            int timeDiff = (g.getMinutesSinceMidnight() + timeScored - currentTimeSinceMidnightinMinutes) * 60 * 1000;

            // If the game has already started then set simulation to start now
            timeDiff = (timeDiff < 0 ? 0 : timeDiff);

            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    uploadEvent(event, g, timeScored, "");
                }
            }, timeDiff + timeScored * 2 + g.getMinutesSinceMidnight());
        }

        // Simulate the drop goals
        for (int i = 1; i < numDrop; i += 1) {
            // Did the home or away team score it (0=home, 1=away)
            String event = (rand.nextInt(1) == 0 ? "homeDropGoal" : "awayDropGoal");
            // Try is scored at a time between 1 and 80 minutes (inclusive)
            int timeScored = rand.nextInt(80) + 1;
            int timeDiff = (g.getMinutesSinceMidnight() + timeScored - currentTimeSinceMidnightinMinutes) * 60 * 1000;

            // If the game has already started then set simulation to start now
            timeDiff = (timeDiff < 0 ? 0 : timeDiff);

            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    uploadEvent(event, g, timeScored, "");
                }
            }, timeDiff + timeScored * 2 + g.getMinutesSinceMidnight());
        }

        // Simulate start of game
        int timeDiff = (g.getMinutesSinceMidnight() - currentTimeSinceMidnightinMinutes) * 60 * 1000;

        timeDiff = (timeDiff < 0 ? 0 : timeDiff);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                uploadEvent("strtGame", g, 0, "");
            }
        }, timeDiff + g.getMinutesSinceMidnight());

        // Simulate half time
        timeDiff = (g.getMinutesSinceMidnight() + 40 - currentTimeSinceMidnightinMinutes) * 60 * 1000;

        timeDiff = (timeDiff < 0 ? 0 : timeDiff);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                uploadEvent("halfTime", g, 40, "");
            }
        }, timeDiff + 40 * 2 + g.getMinutesSinceMidnight());

        // Simulate full time
        timeDiff = (g.getMinutesSinceMidnight() + 80 - currentTimeSinceMidnightinMinutes) * 60 * 1000;

        timeDiff = (timeDiff < 0 ? 0 : timeDiff);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                uploadEvent("fullTime", g, 80, "");
            }
        }, timeDiff + 80 * 2 + g.getMinutesSinceMidnight());
    }

    private static void changeScore(String event, Game g) {
        if (event.substring(0,4).equals("home")) {
            switch (event.substring(4)) {
                case "Try":
                    g.setHomeTeamScore(g.getHomeTeamScore() + 5);
                    break;
                case "DropGoal":
                case "Penalty":
                    g.setHomeTeamScore(g.getHomeTeamScore() + 3);
                    break;
                case "Conversion":
                    g.setHomeTeamScore(g.getHomeTeamScore() + 2);
                    break;
            }
        } else if (event.substring(0, 4).equals("away")) {
            switch (event.substring(4)) {
                case "Try":
                    g.setAwayTeamScore(g.getAwayTeamScore() + 5);
                    break;
                case "DropGoal":
                case "Penalty":
                    g.setAwayTeamScore(g.getAwayTeamScore() + 3);
                    break;
                case "Conversion":
                    g.setAwayTeamScore(g.getAwayTeamScore() + 2);
                    break;
            }
        }
    }

    private static void displayEvent(String event, Game g, int timeScored) {
        // Get hours past midday or midnight and the minutes past hour if game doesn't start on the hour
        int hours;
        int minutes;
        if (g.getTime().contains(":")) {
            hours = Integer.parseInt(g.getTime().substring(0, g.getTime().indexOf(":")));
            minutes = Integer.parseInt(g.getTime().substring(g.getTime().indexOf(":") + 1, g.getTime().indexOf(":") + 3));
        } else {
            hours = Integer.parseInt(g.getTime().substring(0, g.getTime().length() - 2));
            minutes = 0;
        }
        // Get am or pm
        String amorpm = g.getTime().substring(g.getTime().length() - 2);
        // Get minutes since midnight
        minutes += timeScored;
        if (minutes >= 120) {
            minutes -= 120;
            hours += 2;
            if (hours >= 12) {
                amorpm = "pm";
                hours -= (hours > 12 ? 12 : 0);
            }
        } else if (minutes >= 60) {
            minutes -= 60;
            hours += 1;
            if (hours >= 12) {
                amorpm = "pm";
                hours -= (hours > 12 ? 12 : 0);
            }
        }

        String timeString = padHours(hours) + ":" + pad(minutes) + amorpm + " ";

        List<String> divisions = Arrays.asList("Div 1", "Women", "Div 2", "Div 3", "Colts",
                "  U18", "  U16", "U14.5", "  U13", "U11.5", "  U10", " U8.5", "   U7");
        int divID = Integer.parseInt(String.valueOf(g.getGameID()).substring(12, 14));
        String div = divisions.get(divID) + " ";
        if (event.substring(0, 4).equals("home") || event.substring(0, 4).equals("away")) {
            // Example: 9:34am Dunsandel 35 vs Leeston 15 (75') - Try to Dunsandel
            String scoringPlay = "";
            scoringPlay += (event.substring(4).equals("DropGoal") ? "Drop Goal" : event.substring(4));
            scoringPlay += " to ";
            scoringPlay += (event.substring(0, 4).equals("home") ? g.getHomeTeamName() : g.getAwayTeamName());
            System.out.println(timeString + " " +
                    div + " " +
                    g.getHomeTeamName() + " " + g.getHomeTeamScore() +
                    " vs " +
                    g.getAwayTeamName() + " " + g.getAwayTeamScore() +
                    " (" + timeScored + "') - " + scoringPlay);
        } else if (event.substring(0, 4).equals("strt")) {
            System.out.println(timeString + " " +
                    div + " GAME START " +
                    g.getHomeTeamName() +
                    " vs " +
                    g.getAwayTeamName());
        } else if (event.substring(0, 4).equals("half")) {
            System.out.println(timeString + " " +
                    div + " HALF TIME " +
                    g.getHomeTeamName() + " " + g.getHomeTeamScore() +
                    " vs " +
                    g.getAwayTeamName() + " " + g.getAwayTeamScore());
        } else if (event.substring(0, 4).equals("full")) {
            System.out.println(timeString + " " +
                    div + " FULL TIME " +
                    g.getHomeTeamName() + " " + g.getHomeTeamScore() +
                    " vs " +
                    g.getAwayTeamName() + " " + g.getAwayTeamScore());
        }
    }

    private static String pad(int num) {
        return num < 10 ? "0" + String.valueOf(num) : String.valueOf(num);
    }

    private static String padHours(int num) {
        return num < 10 ? " " + String.valueOf(num) : String.valueOf(num);
    }

    private static void uploadEvent(String event, Game g, int timeScored, String description) {
        changeScore(event, g);
        displayEvent(event, g, timeScored);
        HttpClient httpclient = HttpClientBuilder.create().build();
        HttpPost httppost = new HttpPost("http://www.possumpam.com/rugby-scoring-app-scripts/Test/update_game.php");
        try {
            // Store game info in List and add to httppost
            List<NameValuePair> nameValuePairs = new ArrayList<>();
            nameValuePairs.add(new BasicNameValuePair("gameID", String.valueOf(g.getGameID())));
            nameValuePairs.add(new BasicNameValuePair("minutesPlayed", String.valueOf(timeScored)));
            nameValuePairs.add(new BasicNameValuePair("scoringPlay", event));
            nameValuePairs.add(new BasicNameValuePair("description", description));
            nameValuePairs.add(new BasicNameValuePair("homeScore", String.valueOf(g.getHomeTeamScore())));
            nameValuePairs.add(new BasicNameValuePair("awayScore", String.valueOf(g.getAwayTeamScore())));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            // Execute httppost and retrieve response
            HttpResponse response = httpclient.execute(httppost);

            // Convert response to String
            HttpEntity entity = response.getEntity();
            InputStream is = entity.getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
            is.close();
            if (sb.toString().equals("failure")) {
                System.out.println(sb);
            }
        } catch (Exception e) {
            // If there's a problem serverside, display error
            e.printStackTrace();
        }
    }
}
