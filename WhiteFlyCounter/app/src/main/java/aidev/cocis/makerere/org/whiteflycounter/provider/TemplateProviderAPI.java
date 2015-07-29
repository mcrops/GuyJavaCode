package aidev.cocis.makerere.org.whiteflycounter.provider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Convenience definitions for NotePadProvider
 */
public final class TemplateProviderAPI {
    public static final String AUTHORITY = "aidev.cocis.makerere.org.whiteflycounter.provider.template";

    // This class cannot be instantiated
    private TemplateProviderAPI() {}
    
    /**
     * Notes table
     */
      public static final class TemplateColumns implements BaseColumns {
        // This class cannot be instantiated
        private TemplateColumns() {}


        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/templates");
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.mande.template";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.mande.template";

        // These are the only things needed for an insert
        public static final String PROJECT_ID = "mFieldId";
        public static final String NAME = "Name";
        
     // these are generated for you (but you can insert something else if you want)
        public static final String DISPLAY_SUBTEXT = "displaySubtext";
        public static final String DATE = "date";
        
    }
}
