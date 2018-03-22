package server;

import bannerclient.RequestType;
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
        clearDataBase();
    }

    private void connectToDatabase() {
        try {
            Class.forName("org.apache.derby.jdbc.ClientDriver").newInstance();
            connection = DriverManager.getConnection("jdbc:derby://localhost:1527/effectenbeurs");
        } catch (Exception e) {
            System.out.println("Unable to connect to database");
        }

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
        addFonds("Aegon", 35.618);
        addFonds("KPN", 42.633);
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

    private void clearDataBase() {
        executeStatement("DELETE FROM EFFECTENBEURS");
    }
    private void pushKoersenToDatabase() {
        for (Fonds fonds : fondsen) {
            pushKoersToDatabaseWithCurrentDateAndTime(fonds.getNaam(), fonds.getKoers());
        }
    }

    private void pushKoersToDatabaseWithCurrentDateAndTime(String name, double koers) {
        executeStatement("INSERT INTO EFFECTENBEURS VALUES ('"+name+"',"+koers +", DEFAULT)");
    }

    private void executeStatement(String statement) {
        if (connection != null) {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(statement);
                if (statement != null) {
                    preparedStatement.execute();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }
}
