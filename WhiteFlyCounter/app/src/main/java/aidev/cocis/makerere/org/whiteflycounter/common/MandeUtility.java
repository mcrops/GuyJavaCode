/**
 * Functions used here are mostly system functions.
 */
package aidev.cocis.makerere.org.whiteflycounter.common;

import android.R.bool;
import android.R.string;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;

import java.io.ByteArrayOutputStream;

/**
 * @author Acellam Guy
 * 
 *         Handy Functions used through out the App
 * 
 */

public class MandeUtility {

	public static final String PREFS_NAME = Constants.PREFS_NAME;

	/**
	 * General framework that allows you to save and retrieve persistent
	 * key-value pairs of primitive data used in Mande
	 * 
	 * @param err
	 *            is a {@link bool} to indicate if the log is a error *
	 * @param msg
	 *            is the {@link string} that should be shown
	 * @return {@link Void}
	 */
	public static void Log(Boolean err, String msg) {
		if (msg == null || !Constants.LOG)
			return;
		if (!err)
			android.util.Log.d("WHITEFLYCOUNTER", msg);
		else
			android.util.Log.e("WHITEFLYCOUNTER", msg);
	}

	/**
	 * Used to check for Internet connectivity
	 * 
	 * @param context
	 *            the current application context we are in
	 * @return <code>true</code> or <code>false</code> to indicate that the
	 *         phone data transfer is on or off
	 */
	public static boolean hasInternetConnection(Context context) {

		ConnectivityManager mgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		return (mgr.getActiveNetworkInfo() != null
				&& mgr.getActiveNetworkInfo().isAvailable() && mgr
				.getActiveNetworkInfo().isConnected());
	}

	/**
	 * Used to encode an image to base64
	 * 
	 * @param image
	 *            is a Bitmap to be encode to base64
	 * @return {@link String} the encode base64 image
	 */
	public static String encodeTobase64(Bitmap image) {
		Bitmap immagex = image;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		immagex.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		byte[] b = baos.toByteArray();
		String imageEncoded = android.util.Base64.encodeToString(b,
				android.util.Base64.DEFAULT);
		return imageEncoded;
	}

	/**
	 * Used to decode an input from base64 into binary
	 * 
	 * @param input
	 *            a base64 string to be decoded
	 * @return
	 */
	public static Bitmap decodeBase64(String input) {
		byte[] decodedByte = android.util.Base64.decode(input, 0);
		return BitmapFactory
				.decodeByteArray(decodedByte, 0, decodedByte.length);
	}

	/**
	 * Handy function to create shorten a long string. useful when displaying
	 * content string on the list view.
	 * 
	 * @param str
	 *            the {@link String} to be shortened
	 * @param maxChars
	 *            the number of characters by which we should shorten the str
	 * @return {@link String} that was shortened
	 */
	public String shortenStringByChars(String str, int maxChars) {
		if (str.length() > maxChars) {
			str = str.substring(0, (maxChars - 3)) + "...";
		}

		return str;
	}

	/**
	 * Used to retrieve email from preferences
	 * 
	 * @param preferences
	 *            the name of the android {@link SharedPreferences} from which
	 *            the email should be got
	 * @return {@link String} of the email requested
	 */
	public static String getEmail(SharedPreferences preferences) {
		return preferences.getString("Email", "");
	}

	/**
	 * Saves email to android {@link SharedPreferences}
	 * 
	 * @param email
	 *            the email of type {@link String} that should be saved
	 * @param preferences
	 *            the name of the android {@link SharedPreferences} to which the
	 *            email should be saved
	 */
	public static void saveEmail(String email, SharedPreferences preferences) {
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString("Email", email);
		editor.commit();
	}

	/**
	 * Used to retrieve password from preferences
	 * 
	 * @param preferences
	 *            the name of the android {@link SharedPreferences} from which
	 *            the password should be got
	 * @return {@link String} of the password requested
	 */
	public static String getPassword(SharedPreferences preferences) {
		return preferences.getString("Password", "");
	}

	/**
	 * Saves password to android {@link SharedPreferences}
	 * 
	 * @param password
	 *            the password of type {@link String} that should be saved
	 * @param preferences
	 *            the name of the android {@link SharedPreferences} to which the
	 *            password should be saved
	 */
	public static void savePassword(String password,
			SharedPreferences preferences) {
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString("Password", password);
		editor.commit();
	}

}
