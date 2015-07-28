package aidev.cocis.makerere.org.whiteflycounter.listeners;

import java.util.HashMap;

import aidev.cocis.makerere.org.whiteflycounter.webservices.field.WSFieldSetting;

public interface FieldSettingsDownloaderListener {
    void fieldSettingsDownloadingComplete(HashMap<WSFieldSetting, String> result);
    void FieldReportingPeriodDownloadingComplete(HashMap<WSFieldSetting, String> result);
    void FieldReportingPeriodTemplateDownloadingComplete(HashMap<WSFieldSetting, String> result);
    void FieldReportingPeriodTemplateQuestionDownloadingComplete(HashMap<WSFieldSetting, String> result);
    //void progressUpdate(String field,String currentSetting,String msg, int progress, int total);
}
