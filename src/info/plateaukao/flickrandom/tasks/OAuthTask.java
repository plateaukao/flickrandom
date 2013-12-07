/**
 * 
 */
package info.plateaukao.flickrandom.tasks;


import info.plateaukao.flickrandom.FlickrHelper;
import info.plateaukao.flickrandom.MainActivity;
import info.plateaukao.flickrandom.utils.Utils;

import java.net.URL;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.Toast;

import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.auth.Permission;
import com.googlecode.flickrjandroid.oauth.OAuthToken;

/**
 * Represents the task to start the oauth process.
 * 
 * @author yayu
 * 
 */
public class OAuthTask extends AsyncTask<Void, Integer, String> {

	//private static final Logger logger = LoggerFactory.getLogger(OAuthTask.class);
	private static final Uri OAUTH_CALLBACK_URI = Uri.parse(MainActivity.CALLBACK_SCHEME
			+ "://oauth"); //$NON-NLS-1$

	/**
	 * The context.
	 */
	private Context mContext;

	/**
	 * The progress dialog before going to the browser.
	 */
	private ProgressDialog mProgressDialog;

	/**
	 * Constructor.
	 * 
	 * @param context
	 */
	public OAuthTask(Context context) {
		super();
		this.mContext = context;
		mProgressDialog = new ProgressDialog(mContext);

	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		mProgressDialog.setMessage("Generating the authorization request...");
		mProgressDialog.setCanceledOnTouchOutside(true);
		mProgressDialog.setCancelable(true);
		mProgressDialog.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dlg) {
				OAuthTask.this.cancel(true);
			}
		});
		mProgressDialog.show();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.AsyncTask#doInBackground(Params[])
	 */
	@Override
	protected String doInBackground(Void... params) {
		try {
			Flickr f = FlickrHelper.getInstance().getFlickr();
			OAuthToken oauthToken = f.getOAuthInterface().getRequestToken(OAUTH_CALLBACK_URI.toString());
			saveTokenSecrent(oauthToken.getOauthTokenSecret());
			URL oauthUrl = f.getOAuthInterface().buildAuthenticationUrl(Permission.READ, oauthToken);
			return oauthUrl.toString();
		} catch (Exception e) {
			//logger.error("Error to oauth", e); //$NON-NLS-1$
			return "error:" + e.getMessage(); //$NON-NLS-1$
		}
	}

	/**
	 * Saves the oauth token secrent.
	 * 
	 * @param tokenSecret
	 */
	private void saveTokenSecrent(String tokenSecret) {
		//logger.debug("request token: " + tokenSecret); //$NON-NLS-1$
		MainActivity act = (MainActivity) mContext;
		Utils.saveOAuthToken(null, null, null, tokenSecret);
		//logger.debug("oauth token secret saved: {}", tokenSecret); //$NON-NLS-1$
	}

	@Override
	protected void onPostExecute(String result) {
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
		}
		if (result != null && !result.startsWith("error") ) { //$NON-NLS-1$
			mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(result)));
		} else {
			Toast.makeText(mContext, result, Toast.LENGTH_LONG).show();
		}
	}

}
