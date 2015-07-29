package aidev.cocis.makerere.org.whiteflycounter.provider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Convenience definitions for NotePadProvider
 */
public final class ReportingPeriodProviderAPI {
    public static final String AUTHORITY = "aidev.cocis.makerere.org.whiteflycounter.provider.reportingperiod";

    // This class cannot be instantiated
    private ReportingPeriodProviderAPI() {}
    
    /**
     * Notes table
     */
      public static final class ReportingPeriodColumns implements BaseColumns {
        // This class cannot be instantiated
        private ReportingPeriodColumns() {}


        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/reportingperiods");
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.mande.reportingperiod";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.mande.reportingperiod";

        // These are the only things needed for an insert
        public static final String PROJECT_ID = "mFieldId";
        public static final String RESEARCH_START_DATE = "ResearchStartDate";
        public static final String RESEARCH_END_DATE = "ResearchEndDate";
        public static final String STORY_INPUT_DEADLINE = "StoryInputDeadline";
        public static final String REPORT_PERIOD_CLOSE_DATE = "ReportPeriodCloseDate";
        public static final String PERIOD_DESCRIPTION = "PeriodDescription";
        public static final String PERIOD_ACTIVE = "Active";
        public static final String PERIOD_STATUS = "Status";
        
     // these are generated for you (but you can insert something else if you want)
        public static final String DISPLAY_SUBTEXT = "displaySubtext";
        public static final String DATE = "date";
        
    }
}
