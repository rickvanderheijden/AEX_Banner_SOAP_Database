package server;

import client.RequestType;
import shared.Fonds;
import shared.Request;

import javax.jws.WebService;
import java.sql.*;
import java.util.*;

@WebService
public class MockEffectenBeurs implements IEffectenBeurs {

    private static final int UPDATE_TIME = 4000;

    private List<Fonds> fondsen;
    private final Random random = new Random();
    private final Timer updateTimer = new Timer();
    private Connection connection = null;

    public MockEffectenBeurs() {
        addFondsen();
        connectToDatabase();
        setTimer();
    }

    private boolean connectToDatabase() {

        boolean result = true;
        try {

            Class.forName("org.apache.derby.jdbc.ClientDriver").newInstance();
            connection = DriverManager.getConnection("jdbc:derby://localhost:1527/effectenbeurs");
            //PreparedStatement statement = connection.prepareStatement("SELECT * from USERS");

            //ResultSet resultSet = statement.executeQuery();
            //while (resultSet.next()) {
                //doStuff
            //}
        } catch (Exception e) {
            result = false;
            //throw e;
        } finally {
            // doStuff?
        }

        return result;
    }

    public List<Fonds> getKoersen(Request request) {
        List<Fonds> fondsenToSend = new ArrayList<>();

        if (request != null) {
            if (request.getRequestType() == RequestType.REQUEST_ALL) {
                fondsenToSend = fondsen;
            } else if (request.getRequestType() == RequestType.REQUEST_FEW || request.getRequestType() == RequestType.REQUEST_ONE) {
                for (Fonds fonds : fondsen) {
                    for (String requestedFonds : request.getRequestFondsen()) {
                        if (requestedFonds.equals(fonds.getNaam())) {
                            fondsenToSend.add(fonds);
                        }
                    }
                }
            }
        }

        return fondsenToSend;
    }

    private void addFondsen() {
        fondsen = new ArrayList<>();
        addFonds("Aegon", 5.618);
        addFonds("KPN", 2.633);
        addFonds("Philips", 31.500);
        addFonds("Randstad", 58.360);
        addFonds("Unilever", 43.485);
    }

    private void addFonds(String naam, double koers) {
        Fonds fonds = new Fonds();
        fonds.setNaam(naam);
        fonds.setKoers(koers);
        fondsen.add(fonds);
    }

    private void updateKoersen() {
        for (Fonds fonds : fondsen) {
            boolean add = random.nextBoolean();
            double koers = fonds.getKoers();
            double difference = random.nextDouble() * (fonds.getKoers() / 10);
            double nieuweKoers = add ? koers + difference : koers - difference;
            fonds.setKoers(nieuweKoers);
        }
    }

    private void setTimer() {
        updateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateKoersen();
                pushKoersenToDatabase();
            }
        }, 0, UPDATE_TIME);
    }

    private void pushKoersenToDatabase() {
        for (Fonds fonds : fondsen) {
            pushKoersToDatabaseWithCurrentDateAndTime(fonds.getNaam(), fonds.getKoers());
        }
    }

    private void pushKoersToDatabaseWithCurrentDateAndTime(String name, double koers) {
        PreparedStatement statement = null;

        if (connection != null) {
            try {
                statement = connection.prepareStatement("INSERT INTO EFFECTENBEURS VALUES ('Rick', 23.3, DEFAULT)");

                if (statement != null) {
                    boolean resultSet = statement.execute();
                    /*while (resultSet.next()) {
                        System.out.println(resultSet.getString("NAME"));
                        System.out.println(resultSet.getString("KOERS"));
                        System.out.println(resultSet.getTimestamp("DATEANDTIME"));
                    }
                    */
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
