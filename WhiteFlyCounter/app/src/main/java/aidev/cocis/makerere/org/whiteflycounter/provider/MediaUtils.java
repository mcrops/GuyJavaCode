/*
 * Copyright (C) 2009 University of Washington
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package aidev.cocis.makerere.org.whiteflycounter.provider;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Video;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Consolidate all interactions with media providers here.
 * 
 * @author Acellam Guy
 *
 */
public class MediaUtils {
	private static final String t = "MediaUtils";
	
	private MediaUtils() {
		// static methods only
	}
	
	private static String escapePath(String path) {
		String ep = path;
		ep = ep.replaceAll("\\!", "!!");
		ep = ep.replaceAll("_", "!_");
		ep = ep.replaceAll("%", "!%");
		return ep;
	}
	
	public static final Uri getImageUriFromMediaProvider(Context ctx, String imageFile) {
        String selection = Images.ImageColumns.DATA + "=?";
        String[] selectArgs = { imageFile };
        String[] fieldion = { Images.ImageColumns._ID };
        Cursor c = null;
        try {
        	c = ctx.getContentResolver().query(
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        fieldion, selection, selectArgs, null);
            if (c.getCount() > 0) {
                c.moveToFirst();
                String id = c.getString(c.getColumnIndex(Images.ImageColumns._ID));

                return Uri.withAppendedPath(
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
            }
            return null;
        } finally {
        	if ( c != null ) {
        		c.close();
        	}
        }
	}
	
	public static final int deleteImageFileFromMediaProvider(Context ctx,String imageFile) {
		ContentResolver cr = ctx.getContentResolver();
        // images
		int count = 0;
        Cursor imageCursor = null;
        try {
            String select =
                    Images.Media.DATA + "=?";   
            String[] selectArgs = { imageFile };

            String[] fieldion = {
                Images.ImageColumns._ID
            };
            imageCursor = cr.query(
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        fieldion, select, selectArgs, null);
            if (imageCursor.getCount() > 0) {
                imageCursor.moveToFirst();
                List<Uri> imagesToDelete = new ArrayList<Uri>();
                do {
	                String id =
	                    imageCursor.getString(imageCursor
	                            .getColumnIndex(Images.ImageColumns._ID));
	
	                	imagesToDelete.add(Uri.withAppendedPath(
	                                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
	                                    id));
                } while ( imageCursor.moveToNext());
                
                for ( Uri uri : imagesToDelete ) {
	                Log.i(t,"attempting to delete: " + uri );
	                count += cr.delete(uri, null, null);
                }
            }
        } catch ( Exception e ) {
        	Log.e(t, e.toString());
        } finally {
        	if ( imageCursor != null ) {
                imageCursor.close();
        	}
        }
        File f = new File(imageFile);
        if ( f.exists() ) {
        	f.delete();
        }
        return count;
	}
	
	public static final int deleteImagesInFolderFromMediaProvider(Context ctx,File folder) {
		ContentResolver cr =ctx.getContentResolver();
        // images
		int count = 0;
        Cursor imageCursor = null;
        try {
            String select =
                    Images.Media.DATA + " like ? escape '!'";   
            String[] selectArgs = { escapePath(folder.getAbsolutePath()) };

            String[] fieldion = {
                Images.ImageColumns._ID
            };
            imageCursor = cr.query(
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        fieldion, select, selectArgs, null);
            if (imageCursor.getCount() > 0) {
                imageCursor.moveToFirst();
                List<Uri> imagesToDelete = new ArrayList<Uri>();
                do {
	                String id =
	                    imageCursor.getString(imageCursor
	                            .getColumnIndex(Images.ImageColumns._ID));
	
	                imagesToDelete.add(Uri.withAppendedPath(
	                                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
	                                    id));
                } while ( imageCursor.moveToNext());
                
                for ( Uri uri : imagesToDelete ) {
	                Log.i(t,"attempting to delete: " + uri );
	                count += cr.delete(uri, null, null);
                }
            }
        } catch ( Exception e ) {
        	Log.e(t, e.toString());
        } finally {
        	if ( imageCursor != null ) {
                imageCursor.close();
        	}
        }
        return count;
	}
	
	
	public static final Uri getAudioUriFromMediaProvider(Context ctx,String audioFile) {
        String selection = Audio.AudioColumns.DATA + "=?";
        String[] selectArgs = { audioFile };
        String[] fieldion = { Audio.AudioColumns._ID };
        Cursor c = null;
        try {
        	c = ctx.getContentResolver().query(
                        android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        fieldion, selection, selectArgs, null);
            if (c.getCount() > 0) {
                c.moveToFirst();
                String id = c.getString(c.getColumnIndex(Audio.AudioColumns._ID));

                return Uri.withAppendedPath(
                    android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
            }
            return null;
        } finally {
        	if ( c != null ) {
        		c.close();
        	}
        }
	}

	public static final int deleteAudioFileFromMediaProvider(Context ctx,String audioFile) {
		ContentResolver cr = ctx.getContentResolver();
        // audio
		int count = 0;
        Cursor audioCursor = null;
        try {
            String select =
                    Audio.Media.DATA + "=?";   
            String[] selectArgs = { audioFile };

            String[] fieldion = {
            		Audio.AudioColumns._ID
            };
            audioCursor = cr.query(
                        android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        fieldion, select, selectArgs, null);
            if (audioCursor.getCount() > 0) {
                audioCursor.moveToFirst();
                List<Uri> audioToDelete = new ArrayList<Uri>();
                do {
	                String id =
	                    audioCursor.getString(audioCursor
	                            .getColumnIndex(Audio.AudioColumns._ID));
	
	                audioToDelete.add(Uri.withAppendedPath(
	                                    android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
	                                    id));
                } while ( audioCursor.moveToNext());
                
                for ( Uri uri : audioToDelete ) {
	                Log.i(t,"attempting to delete: " + uri );
	                count += cr.delete(uri, null, null);
                }
            }
        } catch ( Exception e ) {
        	Log.e(t, e.toString());
        } finally {
        	if ( audioCursor != null ) {
                audioCursor.close();
        	}
        }
        File f = new File(audioFile);
        if ( f.exists() ) {
        	f.delete();
        }
        return count;
	}

	public static final int deleteAudioInFolderFromMediaProvider(Context ctx,File folder) {
		ContentResolver cr =ctx.getContentResolver();
        // audio
		int count = 0;
        Cursor audioCursor = null;
        try {
            String select =
                    Audio.Media.DATA + " like ? escape '!'";   
            String[] selectArgs = { escapePath(folder.getAbsolutePath()) };

            String[] fieldion = {
            		Audio.AudioColumns._ID
            };
            audioCursor = cr.query(
                        android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        fieldion, select, selectArgs, null);
            if (audioCursor.getCount() > 0) {
                audioCursor.moveToFirst();
                List<Uri> audioToDelete = new ArrayList<Uri>();
                do {
	                String id =
	                    audioCursor.getString(audioCursor
	                            .getColumnIndex(Audio.AudioColumns._ID));
	
	                audioToDelete.add(Uri.withAppendedPath(
	                                    android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
	                                    id));
                } while ( audioCursor.moveToNext());
                
                for ( Uri uri : audioToDelete ) {
	                Log.i(t,"attempting to delete: " + uri );
	                count += cr.delete(uri, null, null);
                }
            }
        } catch ( Exception e ) {
        	Log.e(t, e.toString());
        } finally {
        	if ( audioCursor != null ) {
                audioCursor.close();
        	}
        }
        return count;
	}
	
	public static final Uri getVideoUriFromMediaProvider(Context ctx,String videoFile) {
        String selection = Video.VideoColumns.DATA + "=?";
        String[] selectArgs = { videoFile };
        String[] fieldion = { Video.VideoColumns._ID };
        Cursor c = null;
        try {
        	c = ctx.getContentResolver().query(
                        android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        fieldion, selection, selectArgs, null);
            if (c.getCount() > 0) {
                c.moveToFirst();
                String id = c.getString(c.getColumnIndex(Video.VideoColumns._ID));

                return Uri.withAppendedPath(
                    android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id);
            }
            return null;
        } finally {
        	if ( c != null ) {
        		c.close();
        	}
        }
	}
	
	public static final int deleteVideoFileFromMediaProvider(Context ctx,String videoFile) {
		ContentResolver cr =ctx.getContentResolver();
        // video
		int count = 0;
        Cursor videoCursor = null;
        try {
            String select =
                    Video.Media.DATA + "=?";   
            String[] selectArgs = { videoFile };

            String[] fieldion = {
            		Video.VideoColumns._ID
            };
            videoCursor = cr.query(
                        android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        fieldion, select, selectArgs, null);
            if (videoCursor.getCount() > 0) {
                videoCursor.moveToFirst();
                List<Uri> videoToDelete = new ArrayList<Uri>();
                do {
	                String id =
	                    videoCursor.getString(videoCursor
	                            .getColumnIndex(Video.VideoColumns._ID));
	
	                videoToDelete.add(Uri.withAppendedPath(
	                                    android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
	                                    id));
                } while ( videoCursor.moveToNext());
                
                for ( Uri uri : videoToDelete ) {
	                Log.i(t,"attempting to delete: " + uri );
	                count += cr.delete(uri, null, null);
                }
            }
        } catch ( Exception e ) {
        	Log.e(t, e.toString());
        } finally {
        	if ( videoCursor != null ) {
                videoCursor.close();
        	}
        }
        File f = new File(videoFile);
        if ( f.exists() ) {
        	f.delete();
        }
        return count;
	}
	
	public static final int deleteVideoInFolderFromMediaProvider(Context ctx,File folder) {
		ContentResolver cr = ctx.getContentResolver();
        // video
		int count = 0;
        Cursor videoCursor = null;
        try {
            String select =
                    Video.Media.DATA + " like ? escape '!'";   
            String[] selectArgs = { escapePath(folder.getAbsolutePath()) };

            String[] fieldion = {
            		Video.VideoColumns._ID
            };
            videoCursor = cr.query(
                        android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        fieldion, select, selectArgs, null);
            if (videoCursor.getCount() > 0) {
                videoCursor.moveToFirst();
                List<Uri> videoToDelete = new ArrayList<Uri>();
                do {
	                String id =
	                    videoCursor.getString(videoCursor
	                            .getColumnIndex(Video.VideoColumns._ID));
	
	                videoToDelete.add(Uri.withAppendedPath(
	                                    android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
	                                    id));
                } while ( videoCursor.moveToNext());
                
                for ( Uri uri : videoToDelete ) {
	                Log.i(t,"attempting to delete: " + uri );
	                count += cr.delete(uri, null, null);
                }
            }
        } catch ( Exception e ) {
        	Log.e(t, e.toString());
        } finally {
        	if ( videoCursor != null ) {
                videoCursor.close();
        	}
        }
        return count;
	}
}
