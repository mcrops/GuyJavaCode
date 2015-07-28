package aidev.cocis.makerere.org.whiteflycounter.listeners;

import java.util.HashMap;

public interface FileDeleteListener {
    void fileDeleteComplete( HashMap<String,String> result);
    void progressUpdate(String currentFile, int progress, int total);
}
