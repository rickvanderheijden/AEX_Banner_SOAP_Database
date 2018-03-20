package shared;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Fonds {

    private String naam;
    private double koers;

    public Fonds() {
        this.naam = "";
    }

    public void setNaam(String naam) {
        this.naam = naam;
    }

    public String getNaam() {
        return naam;
    }

    public double getKoers() {
        return koers;
    }

    public void setKoers(double koers) {
        this.koers = (this.koers + koers < 0.0) ? 0.0 : koers;
    }
}
