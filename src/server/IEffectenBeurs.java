package server;

import shared.Fonds;
import shared.Request;

import javax.jws.WebService;
import java.util.List;

@WebService
public interface IEffectenBeurs {
    List<Fonds> getKoersen(Request request);
}
