/**
 * 
 */
package info.plateaukao.flickrandom.tasks;

import info.plateaukao.flickrandom.FlickrHelper;

import java.util.HashSet;
import java.util.Set;

import android.app.Activity;
import android.os.AsyncTask;
import android.widget.ImageView;
import android.widget.Toast;

import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.oauth.OAuthToken;
import com.googlecode.flickrjandroid.photos.Photo;

public class AddTagTask extends
		AsyncTask<OAuth, Void, Photo> {
	
	private Activity activity;
	private ImageView iv;
	private Photo photo;
	private String tag = "Favorites";

	/**
	 * @param flickrjAndroidSampleActivity
	 */
	public AddTagTask(Activity activity, ImageView v, Photo p, String tag) {
		this.activity = activity;
		this.iv = v;
		this.photo = p;
		if(null != tag)
			this.tag = tag;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.AsyncTask#doInBackground(Params[])
	 */
	@Override
	protected Photo doInBackground(OAuth... arg0) {
		OAuthToken token = arg0[0].getToken();
		Flickr f = FlickrHelper.getInstance().getFlickrAuthed(
				token.getOauthToken(), token.getOauthTokenSecret());
		Set<String> extras = new HashSet<String>();
		extras.add("url_sq"); //$NON-NLS-1$
		extras.add("url_l"); //$NON-NLS-1$
		extras.add("views"); //$NON-NLS-1$
		try {
			
			String[] tags = new String[1];
			tags[0] = tag;

			f.getPhotosInterface().addTags(photo.getId(), tags);

			return photo;

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 */
	@Override
	protected void onPostExecute(Photo result) {
		boolean isFound = false;
		// check if it's added
		Toast.makeText(activity, "Tag: Favorites added!", Toast.LENGTH_SHORT).show();
		iv.setImageResource(android.R.drawable.btn_star_big_on);
	}

}