/**
 * Android Font Manager
 **/
package aidev.cocis.makerere.org.whiteflycounter.common;

import android.content.Context;
import android.graphics.Typeface;

import java.util.HashMap;
import java.util.Map;

/**
 * Used to manage the fonts that are used for the app
 * @author Acellam Guy
 *
 */
public class FontManager {
	
/**
 * The type face font that we wanna create
 */
private static Map<String, Typeface> TYPEFACE = new HashMap<String, Typeface>();

	/**
	 * Gets the Font details and returns the {@link Typeface} that should be
	 * used
	 * @param context
	 * @param name the name of the font that we want to create. Make sure that 
	 * the extension is also included
	 * @return {@link Typeface} of the font wanted, it is <code>null</code> if 
	 * not found
	 */
	public static Typeface getFonts(Context context, String name) { 
	    Typeface typeface = TYPEFACE.get(name);
	    if (typeface == null) {
	        typeface = Typeface.createFromAsset(context.getAssets(), "fonts/"
	                + name);
	        TYPEFACE.put(name, typeface);
	    }
	    return typeface;
	}
}
