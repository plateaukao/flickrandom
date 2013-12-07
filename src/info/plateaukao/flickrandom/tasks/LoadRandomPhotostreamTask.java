/**
 * 
 */
package info.plateaukao.flickrandom.tasks;


import info.plateaukao.flickrandom.FlickrHelper;
import info.plateaukao.flickrandom.images.LazyAdapter;

import java.util.HashSet;
import java.util.Set;

import android.app.Activity;
import android.os.AsyncTask;
import android.widget.ListView;

import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.oauth.OAuthToken;
import com.googlecode.flickrjandroid.people.User;
import com.googlecode.flickrjandroid.photos.PhotoList;
import com.googlecode.flickrjandroid.photos.SearchParameters;

public class LoadRandomPhotostreamTask extends AsyncTask<OAuth, Void, PhotoList> {

	/**
	 * 
	 */
	private ListView listView;
	private Activity activity;
	
	private int pageSize = 20;
	private int pageCount = 1;

	/**
	 * @param flickrjAndroidSampleActivity
	 */
	public LoadRandomPhotostreamTask(Activity activity,
			ListView listView) {
		this.activity = activity;
		this.listView = listView;
	}

	/* (non-Javadoc)
	 * @see android.os.AsyncTask#doInBackground(Params[])
	 */
	@Override
	protected PhotoList doInBackground(OAuth... arg0) {
		OAuthToken token = arg0[0].getToken();
		Flickr f = FlickrHelper.getInstance().getFlickrAuthed(token.getOauthToken(), 
				token.getOauthTokenSecret());
		Set<String> extras = new HashSet<String>();
		extras.add("url_sq"); //$NON-NLS-1$
		extras.add("url_l"); //$NON-NLS-1$
		extras.add("views"); //$NON-NLS-1$
		User user = arg0[0].getUser();
		try {
			//return f.getPeopleInterface().getPhotos(user.getId(), extras, 20, 1);

			SearchParameters params = new SearchParameters();
			String[] str = {"tobeposted}"};
			params.setTags(str);
			params.setUserId(user.getId());
			return f.getPhotosInterface().search(params, pageSize, pageCount);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 */
	@Override
	protected void onPostExecute(PhotoList result) {
		if (result != null) {
			LazyAdapter adapter = 
					new LazyAdapter(this.activity, result);
			this.listView.setAdapter(adapter);
		}
	}
	
}