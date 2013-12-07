/**
 * 
 */
package info.plateaukao.flickrandom.tasks;


import info.plateaukao.flickrandom.FlickrHelper;
import info.plateaukao.flickrandom.utils.Utils;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.widget.Toast;

import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.oauth.OAuthToken;
import com.googlecode.flickrjandroid.people.User;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;

public class LoadUserTask extends BaseAsyncTask<OAuth, Void, User> {
	/**
	 * 
	 */
	private final Activity activity;
	
	public LoadUserTask(Activity activity) {
		this.activity = activity;
	}
	
	/**
	 * The progress dialog before going to the browser.
	 */
	private ProgressDialog mProgressDialog;
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		mProgressDialog = ProgressDialog.show(activity,
				"", "Loading user information..."); //$NON-NLS-1$ //$NON-NLS-2$
		mProgressDialog.setCanceledOnTouchOutside(true);
		mProgressDialog.setCancelable(true);
		mProgressDialog.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dlg) {
				LoadUserTask.this.cancel(true);
			}
		});
	}

	/* (non-Javadoc)
	 * @see android.os.AsyncTask#doInBackground(Params[])
	 */
	@Override
	protected User doInBackground(OAuth... params) {
		OAuth oauth = params[0];
		User user = oauth.getUser();
		OAuthToken token = oauth.getToken();
		try {
			Flickr f = FlickrHelper.getInstance()
					.getFlickrAuthed(token.getOauthToken(), token.getOauthTokenSecret());
			return f.getPeopleInterface().getInfo(user.getId());
		} catch (Exception e) {
			Toast.makeText(activity, e.toString(), Toast.LENGTH_LONG).show();
			//logger.error(e.getLocalizedMessage(), e);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 */
	@Override
	protected void onPostExecute(User user) {
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
		}
		if (user == null) {
			return;
		}
		
		activity.getActionBar().setTitle(activity.getActionBar().getTitle() + ": "+ user.getUsername());
		
		if (user.getBuddyIconUrl() != null) {
			String buddyIconUrl = user.getBuddyIconUrl();
			imageLoader.loadImage(buddyIconUrl, new ImageLoadingListener(){

				@Override
				public void onLoadingCancelled(String arg0, View arg1) {
					
					
				}

				@Override
				public void onLoadingComplete(String arg0, View arg1,
						Bitmap arg2) {
					Bitmap scaledBmp = Bitmap.createScaledBitmap(arg2, Utils.dpToPx(96), Utils.dpToPx(96), false);
					activity.getActionBar().setIcon(new BitmapDrawable(scaledBmp));
					arg2.recycle();
				}

				@Override
				public void onLoadingFailed(String arg0, View arg1,
						FailReason arg2) {
					
				}

				@Override
				public void onLoadingStarted(String arg0, View arg1) {
					
				}
				
			});
		}
	}
	
	
}