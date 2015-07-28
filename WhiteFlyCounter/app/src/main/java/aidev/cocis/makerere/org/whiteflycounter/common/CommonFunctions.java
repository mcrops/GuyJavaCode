/**
 * This class contains some shared functions that are used through out the 
 * application
 * 
 * @author Acellam Guy
 **/
package aidev.cocis.makerere.org.whiteflycounter.common;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Environment;

import java.io.File;

/**
 * Has a group of functions shared through out out the application
 * 
 */
public class CommonFunctions {
	/**
	 * Resize the bitmap to a specified length and width
	 * 
	 * @param bm
	 *            This is the {@link Bitmap} to be resized
	 * @param newHeight
	 *            the preferred new height
	 * @param newWidth
	 *            the preferred new width
	 * @return {@link Bitmap} the resized bitmap object
	 **/
	public static Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
		int width = bm.getWidth();
		int height = bm.getHeight();
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;
		// CREATE A MATRIX FOR THE MANIPULATION
		Matrix matrix = new Matrix();
		// RESIZE THE BIT MAP
		matrix.postScale(scaleWidth, scaleHeight);
		// "RECREATE" THE NEW BITMAP
		Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height,
				matrix, false);
		return resizedBitmap;
	}
	
	   /* Creates required directories on the SDCard (or other external storage)
	     *
	     * @throws RuntimeException if there is no SDCard or the directory exists as a non directory
	     */
	    public static void createWHITEFLYCOUNTERDirs() throws RuntimeException {
	        String cardstatus = Environment.getExternalStorageState();
	        if (!cardstatus.equals(Environment.MEDIA_MOUNTED)) {
	            RuntimeException e =
	                    new RuntimeException("WHITEFLYCOUNTER reports :: SDCard error: "
	                            + Environment.getExternalStorageState());
	            throw e;
	        }

	        String[] dirs = {
	                Constants.WHITEFLYCOUNTER_ROOT, Constants.PROJECTS_PATH, Constants.STORIES_PATH, Constants.CACHE_PATH, Constants.METADATA_PATH
	        };

	        for (String dirName : dirs) {
	            File dir = new File(dirName);
	            if (!dir.exists()) {
	                if (!dir.mkdirs()) {
	                    RuntimeException e =
	                            new RuntimeException("WHITEFLYCOUNTER reports :: Cannot create directory: "
	                                    + dirName);
	                    throw e;
	                }
	            } else {
	                if (!dir.isDirectory()) {
	                    RuntimeException e =
	                            new RuntimeException("WHITEFLYCOUNTER reports :: " + dirName
	                                    + " exists, but is not a directory");
	                    throw e;
	                }
	            }
	        }
	    }
	    
	    public static void createStoryDir(String storydir) throws RuntimeException {
	        String cardstatus = Environment.getExternalStorageState();
	        if (!cardstatus.equals(Environment.MEDIA_MOUNTED)) {
	            RuntimeException e =
	                    new RuntimeException("WHITEFLYCOUNTER reports :: SDCard error: "
	                            + Environment.getExternalStorageState());
	            throw e;
	        }

	        String[] dirs = {
	                Constants.STORIES_PATH+ File.separator+storydir
	        };

	        for (String dirName : dirs) {
	            File dir = new File(dirName);
	            if (!dir.exists()) {
	                if (!dir.mkdirs()) {
	                    RuntimeException e =
	                            new RuntimeException("WHITEFLYCOUNTER reports :: Cannot create directory: "
	                                    + dirName);
	                    throw e;
	                }
	            } else {
	                if (!dir.isDirectory()) {
	                    RuntimeException e =
	                            new RuntimeException("WHITEFLYCOUNTER reports :: " + dirName
	                                    + " exists, but is not a directory");
	                    throw e;
	                }
	            }
	        }
	    }
	    
	    public static void createQuestionAnswerDir(String storyID,String questionID) throws RuntimeException {
	        String cardstatus = Environment.getExternalStorageState();
	        if (!cardstatus.equals(Environment.MEDIA_MOUNTED)) {
	            RuntimeException e =
	                    new RuntimeException("WHITEFLYCOUNTER reports :: SDCard error: "
	                            + Environment.getExternalStorageState());
	            throw e;
	        }

	        String[] dirs = {
	                Constants.STORIES_PATH+ File.separator+storyID+ File.separator+questionID
	        };

	        for (String dirName : dirs) {
	            File dir = new File(dirName);
	            if (!dir.exists()) {
	                if (!dir.mkdirs()) {
	                    RuntimeException e =
	                            new RuntimeException("WHITEFLYCOUNTER reports :: Cannot create directory: "
	                                    + dirName);
	                    throw e;
	                }
	            } else {
	                if (!dir.isDirectory()) {
	                    RuntimeException e =
	                            new RuntimeException("WHITEFLYCOUNTER reports :: " + dirName
	                                    + " exists, but is not a directory");
	                    throw e;
	                }
	            }
	        }
	    }
	    
	    /**
	     * Predicate that tests whether a directory path might refer to an
	     * ODK Tables instance data directory (e.g., for media attachments).
	     *
	     * @param directory
	     * @return
	     */
	    public static boolean isWHITEFLYCOUNTERTablesStoryDataDirectory(File directory) {
			/**
			 * Special check to prevent deletion of files that
			 * could be in use by WHITEFLYCOUNTER Tables.
			 */
	    	String dirPath = directory.getAbsolutePath();
	    	if ( dirPath.startsWith(Constants.WHITEFLYCOUNTER_ROOT) ) {
	    		dirPath = dirPath.substring(Constants.WHITEFLYCOUNTER_ROOT.length());
	    		String[] parts = dirPath.split(File.separator);
	    		// [appName, instances, tableId, instanceId ]
	    		if ( parts.length == 4 && parts[1].equals("stories") ) {
	    			return true;
	    		}
	    	}
	    	return false;
		}
}
