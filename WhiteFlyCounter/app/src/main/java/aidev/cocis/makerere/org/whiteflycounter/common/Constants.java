/**
 * This class contains the variables that will not change when the application
 * is running.
 **/
package aidev.cocis.makerere.org.whiteflycounter.common;

import android.os.Environment;

import java.io.File;

/**
 * Contains variables that are reused and do no change
 * @author Acellam Guy
 *
 */
public abstract class Constants {
	/**
	 * Should we should logs to the user
	 */
	public static final boolean LOG = true;
	/**
	 * Application preferences name as used in android
	 */
	public static final String PREFS_NAME = "MandePreferences";
	/**
	 * This is the server that we are connecting to
	 */
	public static final String SERVER_URL = "http://192.168.1.128";
	
	
	 // Storage paths
    public static final String WHITEFLYCOUNTER_ROOT = Environment.getExternalStorageDirectory()
            + File.separator + "mande";
    public static final String PROJECTS_PATH = WHITEFLYCOUNTER_ROOT + File.separator + "fields";
    public static final String STORIES_PATH = WHITEFLYCOUNTER_ROOT + File.separator + "stories";
    public static final String CACHE_PATH = WHITEFLYCOUNTER_ROOT + File.separator + ".cache";
    public static final String METADATA_PATH = WHITEFLYCOUNTER_ROOT + File.separator + "metadata";
    public static final String TMPFILE_PATH = CACHE_PATH + File.separator + "tmp.jpg";
    public static final String TMPDRAWFILE_PATH = CACHE_PATH + File.separator + "tmpDraw.jpg";
    public static final String TMPXML_PATH = CACHE_PATH + File.separator + "tmp.xml";
    public static final String LOG_PATH = WHITEFLYCOUNTER_ROOT + File.separator + "log";

    public static final String DEFAULT_FONTSIZE = "21";

}