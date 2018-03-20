package client;

import server.IEffectenBeurs;
import shared.Fonds;
import shared.Request;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

class BannerController extends UnicastRemoteObject {

    private static final String DIVIDER = "     ";
    private static final int UPDATE_TIME = 2000;
    private final client.AEXBannerClient banner;
    private final Timer pollingTimer = new Timer();
    private RequestType requestType = RequestType.REQUEST_ALL;
    private IEffectenBeurs effectenBeurs;

    public BannerController(client.AEXBannerClient banner) throws RemoteException {
        this.banner = banner;
        if (createConnection()) {
            startPollingTimer();
        }
    }

    public void stop() {
        pollingTimer.cancel();
        pollingTimer.purge();
    }

    private void updateBanner() {
        List<Fonds> fondsen;
        Request request = new Request();

        switch (requestType) {
            case REQUEST_ALL:
                request.setRequestType(requestType);
                banner.setText("Alle fondsen worden getoond");
                break;
            case REQUEST_FEW:
                List<String> fondsenToRequest = new ArrayList<>();
                fondsenToRequest.add("Aegon");
                fondsenToRequest.add("Philips");
                request.setRequestType(requestType);
                request.setRequestedFondsen(fondsenToRequest);
                banner.setText("Alleen Aegon en Philips worden getoond");
                break;
            case REQUEST_ONE:
                List<String> fondsToRequest = new ArrayList<>();
                fondsToRequest.add("Randstad");
                request.setRequestType(requestType);
                request.setRequestedFondsen(fondsToRequest);
                banner.setText("Alleen Randstad wordt getoond");
                break;
        }

        fondsen = effectenBeurs.getKoersen(request);

        if (fondsen != null && fondsen.size() > 0) {
            StringBuilder fondsenString = new StringBuilder();

            for (Fonds fonds : fondsen) {
                fondsenString.append(fonds.getNaam());
                fondsenString.append(": ");
                String koers = String.format("%.4f", fonds.getKoers());
                fondsenString.append(koers);
                fondsenString.append(DIVIDER);
            }

            banner.setKoersen(fondsenString.toString());
        }
    }

    private boolean createConnection()
    {
        boolean result = true;

        if (effectenBeurs == null)
        {
            URL wsdlURL = null;

            try {
                wsdlURL = new URL("http://localhost:8080/AEXBanner?wsdl");
            }
            catch (MalformedURLException e) {
                result = false;
            }

            if (result) {
                try {
                    QName qname = new QName("http://server/", "MockEffectenBeursService");
                    Service service = Service.create(wsdlURL, qname);
                    QName qnamePort = new QName("http://server/", "MockEffectenBeursPort");
                    effectenBeurs = service.getPort(qnamePort, IEffectenBeurs.class);
                } catch (WebServiceException e) {
                    result = false;
                }
            }
        }

        if (!result) {
            System.err.println("Failed to create connections.");
        }

        return result;
    }

    private void startPollingTimer() {
        pollingTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateBanner();
            }
        }, 0, UPDATE_TIME);
    }

    public void toggleRequestType() {
        switch (requestType) {
            case REQUEST_ALL:
                requestType = RequestType.REQUEST_FEW;
                break;
            case REQUEST_FEW:
                requestType = RequestType.REQUEST_ONE;
                break;
            case REQUEST_ONE:
                requestType = RequestType.REQUEST_ALL;
                break;
        }
    }
}
