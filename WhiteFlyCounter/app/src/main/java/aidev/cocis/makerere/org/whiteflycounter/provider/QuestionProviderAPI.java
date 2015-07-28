package aidev.cocis.makerere.org.whiteflycounter.provider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Convenience definitions for NotePadProvider
 */
public final class QuestionProviderAPI {
    public static final String AUTHORITY = "aidev.cocis.makerere.org.whiteflycounter.provider.question";

    // This class cannot be instantiated
    private QuestionProviderAPI() {}
    
    /**
     * Notes table
     */
      public static final class QuestionColumns implements BaseColumns {
        // This class cannot be instantiated
        private QuestionColumns() {}


        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/questions");
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.mande.question";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.mande.question";

        // These are the only things needed for an insert
        public static final String PROJECT_ID = "mFieldId";
        public static final String QUESTION_ID = "mQuestionId";
        public static final String QUESTION = "question";
        public static final String QUESTION_ANSWER = "answer";
        public static final String QUESTION_ORDER = "question_order";
        public static final String STORY_ID = "storytId";
        //public static final String STATUS = "question_status";
        
     // these are generated for you (but you can insert something else if you want)
        public static final String DISPLAY_SUBTEXT = "displaySubtext";
        public static final String DATE = "date";
        
    }
}
