package server;

import javax.xml.ws.Endpoint;

class AEXBannerServer {
    private static final String url = "http://localhost:8080/AEXBanner";

    public static void main(String[] args) {
        // Welcome message
        System.out.println("AEX Banner Service is running");

        // Create Effectenbeurs
        Endpoint.publish(url, new MockEffectenBeurs());
    }
}
