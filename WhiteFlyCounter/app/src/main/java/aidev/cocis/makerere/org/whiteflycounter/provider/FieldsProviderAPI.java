package aidev.cocis.makerere.org.whiteflycounter.provider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Convenience definitions for NotePadProvider
 */
public final class FieldsProviderAPI {
    public static final String AUTHORITY = "aidev.cocis.makerere.org.whiteflycounter.provider.fields";

    // This class cannot be instantiated
    private FieldsProviderAPI() {}
    
    /**
     * Notes table
     */
    public static final class FieldsColumns implements BaseColumns {
        // This class cannot be instantiated
        private FieldsColumns() {}


        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/fields");
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.mande.field";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.mande.field";

        // These are the only things needed for an insert
        public static final String NAME = "name";
        public static final String DESCRIPTION = "description";  // can be null
        public static final String WHITEFLYCOUNTER_PROJECT_ID = "mFieldId";

        // these are generated for you (but you can insert something else if you want)
        public static final String DISPLAY_SUBTEXT = "displaySubtext";
        public static final String DATE = "date";        
        
        
        // this is null on create, and can only be set on an update.
        public static final String LANGUAGE = "language";
        
        
    }
}
