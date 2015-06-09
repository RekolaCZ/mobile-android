package cz.rekola.app.webapi;

public interface WebApiHandler {

    /**
     * @param paramUrl
     * @return Whether the event was processed.
     */
    public boolean onWebApiEvent(String paramUrl);

}
