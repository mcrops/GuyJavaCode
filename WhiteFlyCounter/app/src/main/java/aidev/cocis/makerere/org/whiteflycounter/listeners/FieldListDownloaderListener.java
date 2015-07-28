package aidev.cocis.makerere.org.whiteflycounter.listeners;

import java.util.HashMap;

import aidev.cocis.makerere.org.whiteflycounter.webservices.field.WSField;
public interface FieldListDownloaderListener {
    void fieldListDownloadingComplete(HashMap<Integer, WSField> value);
}
