package aidev.cocis.makerere.org.whiteflycounter.provider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Convenience definitions for NotePadProvider
 */
public final class QuestionAnswerProviderAPI {
    public static final String AUTHORITY = "aidev.cocis.makerere.org.whiteflycounter.provider.questionanswer";

    // This class cannot be instantiated
    private QuestionAnswerProviderAPI() {}
    
    /**
     * Notes table
     */
      public static final class QuestionAnswerColumns implements BaseColumns {
        // This class cannot be instantiated
        private QuestionAnswerColumns() {}


        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/questionanswers");
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.mande.questionanswer";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.mande.questionanswer";

        // These are the only things needed for an insert
        public static final String PROJECT_ID = "mFieldId";
        public static final String QUESTION_ID = "mQuestionId";
        public static final String QUESTION = "question";
        public static final String QUESTION_ANSWER = "answer";
        public static final String STORY_ID = "storytId";
        public static final String STATUS = "questionanswer_status";
        
        // these are generated for you (but you can insert something else if you want)
        public static final String DISPLAY_SUBTEXT = "displaySubtext";
        public static final String DATE = "date";
        
    }
}
