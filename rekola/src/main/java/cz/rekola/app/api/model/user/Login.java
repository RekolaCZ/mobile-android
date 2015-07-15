package cz.rekola.app.api.model.user;

public class Login {
    /**
     * Token for authorized user, it is send with every request in header
     */
    public String apiKey;
    public boolean showWebviewForBikedetail = false;

}
