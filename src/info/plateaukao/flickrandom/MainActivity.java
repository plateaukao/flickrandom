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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.oauth.OAuthToken;
import com.googlecode.flickrjandroid.people.User;

public class MainActivity extends BaseActivity{
	public static final String CALLBACK_SCHEME = "flickrandom-oauth"; //$NON-NLS-1$


	private ListView listView;
	private TextView textUserTitle;
	private TextView textUserName;
	private TextView textUserId;
	private ImageView userIcon;
	private ImageButton refreshButton;
	
	private OAuth oauth;
	private User user;

	private LoadRandomPhotostreamTask mLoadRandomPhotosTask;
	private LazyAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		this.textUserTitle = (TextView) this
				.findViewById(R.id.profilePageTitle);
		this.textUserName = (TextView) this.findViewById(R.id.userScreenName);
		this.textUserId = (TextView) this.findViewById(R.id.userId);
		this.userIcon = (ImageView) this.findViewById(R.id.userImage);
		this.listView = (ListView) this.findViewById(R.id.imageList);
		adapter = new LazyAdapter(this);
		this.listView.setAdapter(adapter);
		
		this.refreshButton = (ImageButton) this
				.findViewById(R.id.btnRefreshUserProfile);

		this.refreshButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				((LazyAdapter)listView.getAdapter()).clear();
				((LazyAdapter)listView.getAdapter()).notifyDataSetChanged();

				load(Utils.getOAuthToken());
			}
		});

		oauth = Utils.getOAuthToken();
		if (oauth == null || oauth.getUser() == null) {
			OAuthTask task = new OAuthTask(this);
			task.execute();
		} else {
			load(oauth);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.main, menu);
		return true;
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
			if(null == user)
				new LoadUserTask(this, userIcon).execute(oauth);
			// new LoadPhotostreamTask(this, listView).execute(oauth);
			new LoadRandomPhotostreamTask(this, adapter, 0).execute(oauth);
		}
	}

	public void setUser(User user) {
		textUserTitle.setText(user.getUsername());
		textUserName.setText(user.getRealName());
		textUserId.setText(user.getId());
	}

	public ImageView getUserIconImageView() {
		return this.userIcon;
	}

	@Override
	public void onResume() {
		super.onResume();
		Intent intent = getIntent();
		String scheme = intent.getScheme();
		OAuth savedToken = Utils.getOAuthToken();
		user = savedToken.getUser();
		
		if (CALLBACK_SCHEME.equals(scheme)
				&& (savedToken == null || savedToken.getUser() == null)) {
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
			if(null!=result)
				oauth = result;
			load(oauth);
		}
	}
}
