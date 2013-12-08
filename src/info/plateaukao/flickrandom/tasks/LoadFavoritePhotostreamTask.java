/**
 * 
 */
package info.plateaukao.flickrandom.tasks;

import info.plateaukao.flickrandom.FlickrHelper;
import info.plateaukao.flickrandom.images.LazyAdapter;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import android.app.Activity;
import android.os.AsyncTask;

import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.oauth.OAuthToken;
import com.googlecode.flickrjandroid.people.User;
import com.googlecode.flickrjandroid.photos.PhotoList;
import com.googlecode.flickrjandroid.photos.SearchParameters;

public class LoadFavoritePhotostreamTask extends
		AsyncTask<OAuth, Void, PhotoList> {

	/**
	 * 
	 */
	private LazyAdapter adapter;
	private Activity activity;

	private int pageSize = 20;
	private int pageCount = 0;
	private static int total = 0;
	private final static Random random = new Random();

	/**
	 * @param flickrjAndroidSampleActivity
	 */
	public LoadFavoritePhotostreamTask(Activity activity, LazyAdapter adapter, int nextPageCount) {
		this.activity = activity;
		this.adapter = adapter;
		
		if(nextPageCount != 0)
			pageCount = nextPageCount;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.AsyncTask#doInBackground(Params[])
	 */
	@Override
	protected PhotoList doInBackground(OAuth... arg0) {
		OAuthToken token = arg0[0].getToken();
		Flickr f = FlickrHelper.getInstance().getFlickrAuthed(
				token.getOauthToken(), token.getOauthTokenSecret());
		Set<String> extras = new HashSet<String>();
		extras.add("url_sq"); //$NON-NLS-1$
		extras.add("url_l"); //$NON-NLS-1$
		extras.add("views"); //$NON-NLS-1$
		User user = arg0[0].getUser();
		try {
			// return f.getPeopleInterface().getPhotos(user.getId(), extras, 20,
			// 1);

			SearchParameters params = new SearchParameters();
			String[] str = {"favorite",};
			params.setTags(str);
			params.setUserId(user.getId());
			if(pageCount == 0)
				pageCount = 1;

			PhotoList list = f.getPhotosInterface().search(params, pageSize,
					pageCount);
			total = list.getTotal();

			return list;

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
	protected void onPostExecute(PhotoList result) {
		if (result != null) {
			adapter.addAll(result);
			adapter.setCurrentPageCount(pageCount);
			adapter.notifyDataSetChanged();
		}
	}

}