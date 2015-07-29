package aidev.cocis.makerere.org.whiteflycounter.listeners;

import java.util.HashMap;

import aidev.cocis.makerere.org.whiteflycounter.webservices.field.WSField;

public interface FieldDownloaderListener {
    void fieldsDownloadingComplete( HashMap<WSField,String> result);
    void progressUpdate(String currentField, int progress, int total);
}
