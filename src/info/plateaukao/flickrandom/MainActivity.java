package info.plateaukao.flickrandom;

import info.plateaukao.flickrandom.images.LazyAdapter;
import info.plateaukao.flickrandom.tasks.GetOAuthTokenTask;
import info.plateaukao.flickrandom.tasks.LoadRandomPhotostreamTask;
import info.plateaukao.flickrandom.tasks.LoadUserTask;
import info.plateaukao.flickrandom.tasks.OAuthTask;
import info.plateaukao.flickrandom.utils.Utils;

import java.util.Locale;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.oauth.OAuthToken;
import com.googlecode.flickrjandroid.people.User;

public class MainActivity extends BaseActivity {
	public static final String CALLBACK_SCHEME = "flickrandom-oauth"; //$NON-NLS-1$

	private ListView listView;
	private LazyAdapter adapter;

	private OAuth oauth;
	private User user;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		this.listView = (ListView) this.findViewById(R.id.imageList);
		adapter = (LazyAdapter)this.getLastNonConfigurationInstance();
		if(null == adapter)
			adapter = new LazyAdapter(this);
		this.listView.setAdapter(adapter);

		oauth = Utils.getOAuthToken();
		if (oauth == null || oauth.getUser() == null) {
			OAuthTask task = new OAuthTask(this);
			task.execute();
		} else {
			if(adapter.isEmpty())
				load(oauth);
		}
	}

	
	@Override
	@Deprecated
	public Object onRetainNonConfigurationInstance() {
		return adapter;
	}
	


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.mi_refresh: {
			((LazyAdapter) listView.getAdapter()).clear();
			((LazyAdapter) listView.getAdapter()).notifyDataSetChanged();

			load(Utils.getOAuthToken());
			return true;
		}
		case R.id.mi_loginout:
		{
			
			return true;
		}
		default:
		}

		return super.onMenuItemSelected(featureId, item);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onNewIntent(android.content.Intent)
	 */
	@Override
	protected void onNewIntent(Intent intent) {
		// this is very important, otherwise you would get a null Scheme in the
		// onResume later on.
		setIntent(intent);
	}

	private void load(OAuth oauth) {
		if (oauth != null) {
			if (null == user)
				new LoadUserTask(this).execute(oauth);
			// new LoadPhotostreamTask(this, listView).execute(oauth);
			new LoadRandomPhotostreamTask(this, adapter, 0).execute(oauth);
		}
	}

	@Override
	public void onResume() {
		super.onResume();


		Intent intent = getIntent();
		String scheme = intent.getScheme();
		
		if (CALLBACK_SCHEME.equals(scheme)
				&& (oauth == null || oauth.getUser() == null)) {
			Uri uri = intent.getData();
			String query = uri.getQuery();
			//logger.debug("Returned Query: {}", query); //$NON-NLS-1$
			String[] data = query.split("&"); //$NON-NLS-1$
			if (data != null && data.length == 2) {
				String oauthToken = data[0].substring(data[0].indexOf("=") + 1); //$NON-NLS-1$
				String oauthVerifier = data[1]
						.substring(data[1].indexOf("=") + 1); //$NON-NLS-1$
				//logger.debug("OAuth Token: {}; OAuth Verifier: {}", oauthToken, oauthVerifier); //$NON-NLS-1$

				OAuth oauth = Utils.getOAuthToken();
				if (oauth != null && oauth.getToken() != null
						&& oauth.getToken().getOauthTokenSecret() != null) {
					GetOAuthTokenTask task = new GetOAuthTokenTask(this);
					task.execute(oauthToken, oauth.getToken()
							.getOauthTokenSecret(), oauthVerifier);
				}
			}
		}

		if(null != oauth)
			user = oauth.getUser();
	}

	public void onOAuthDone(OAuth result) {
		if (result == null) {
			Toast.makeText(this, "Authorization failed", //$NON-NLS-1$
					Toast.LENGTH_LONG).show();
		} else {
			user = result.getUser();
			OAuthToken token = result.getToken();
			if (user == null || user.getId() == null || token == null
					|| token.getOauthToken() == null
					|| token.getOauthTokenSecret() == null) {
				Toast.makeText(this, "Authorization failed", //$NON-NLS-1$
						Toast.LENGTH_LONG).show();
				return;
			}
			String message = String
					.format(Locale.US,
							"Authorization Succeed: user=%s, userId=%s, oauthToken=%s, tokenSecret=%s", //$NON-NLS-1$
							user.getUsername(), user.getId(),
							token.getOauthToken(), token.getOauthTokenSecret());
			Toast.makeText(this, message, Toast.LENGTH_LONG).show();
			Utils.saveOAuthToken(user.getUsername(), user.getId(),
					token.getOauthToken(), token.getOauthTokenSecret());
			if (null != result)
				oauth = result;
			load(oauth);
		}
	}
}
