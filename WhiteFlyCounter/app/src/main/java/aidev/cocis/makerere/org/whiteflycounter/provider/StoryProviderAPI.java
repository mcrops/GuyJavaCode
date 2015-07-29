/*
 * Copyright (C) 2007 The Android Open Source Field
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package aidev.cocis.makerere.org.whiteflycounter.provider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Convenience definitions for NotePadProvider
 */
public final class StoryProviderAPI {
    public static final String AUTHORITY = "aidev.cocis.makerere.org.whiteflycounter.provider.stories";

    // This class cannot be instantiated
    private StoryProviderAPI() {}
    
    // status for stories
    public static final String STATUS_INCOMPLETE = "incomplete";
    public static final String STATUS_COMPLETE = "complete";
    public static final String STATUS_FINALIZED = "finalized";
    public static final String STATUS_SUBMITTED = "submitted";
    public static final String STATUS_SUBMISSION_FAILED = "submissionFailed";
    
    /**
     * Notes table
     */
    public static final class StoryColumns implements BaseColumns {
       
        private StoryColumns() {}
        
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/stories");
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.odk.story";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.odk.story";

        // These are the only things needed for an insert
        public static final String TITLE = "title";
        public static final String STORY_FULLTEXT = "fulltext";
        public static final String  PARTICIPANT_NAME = "pname";
        public static final String  PARTICIPANT_CONTACT = "pcontant";
        public static final String PROJECT_ID = "mFieldId";
        
        // these are generated for you (but you can insert something else if you want)
        public static final String STATUS = "status";
        public static final String CAN_EDIT_WHEN_COMPLETE = "canEditWhenComplete";
        public static final String LAST_STATUS_CHANGE_DATE = "date";
        public static final String DISPLAY_SUBTEXT = "displaySubtext";
    }
}
