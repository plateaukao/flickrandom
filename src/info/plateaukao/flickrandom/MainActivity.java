package info.plateaukao.flickrandom;

import info.plateaukao.flickrandom.CV.BROWSE_CATEGORY;
import info.plateaukao.flickrandom.ScaleImageView.SCALE_STATUS;
import info.plateaukao.flickrandom.adapters.LazyAdapter;
import info.plateaukao.flickrandom.adapters.LazyAdapter.OnClickListener;
import info.plateaukao.flickrandom.tasks.GetOAuthTokenTask;
import info.plateaukao.flickrandom.tasks.LoadFavoritePhotostreamTask;
import info.plateaukao.flickrandom.tasks.LoadRandomPhotostreamTask;
import info.plateaukao.flickrandom.tasks.LoadUserTask;
import info.plateaukao.flickrandom.tasks.OAuthTask;
import info.plateaukao.flickrandom.utils.Utils;

import java.util.Locale;

import android.animation.Animator;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.oauth.OAuthToken;
import com.googlecode.flickrjandroid.people.User;
import com.googlecode.flickrjandroid.photos.Photo;

public class MainActivity extends BaseActivity implements OnClickListener {
	public static final String CALLBACK_SCHEME = "flickrandom-oauth"; //$NON-NLS-1$

	private GridView listView;
	private ScaleImageView iv;
	private LazyAdapter adapter;
	
	private BROWSE_CATEGORY browseMode = BROWSE_CATEGORY.CATEGORY_RANDOM;
	
	private OAuth oauth;
	private User user;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		iv = (ScaleImageView) this.findViewById(R.id.imageScale);
		iv.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				ScaleImageView siv = (ScaleImageView)v;
				if(siv.scaleStatus() != SCALE_STATUS.SCALE_DOWN){
					siv.startScaleDownAnimation();
				}
				
			}
			
		});

		listView = (GridView) this.findViewById(R.id.imageList);

		adapter = (LazyAdapter)this.getLastNonConfigurationInstance();
		if(null == adapter){
			adapter = new LazyAdapter(this);
			adapter.setBrowseMode(browseMode);
		}

		this.listView.setAdapter(adapter);
		adapter.setOnClickListener(this);

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
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		browseMode = BROWSE_CATEGORY.valueOf(savedInstanceState.getString("browsemode"));
	}


	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("browsemode", browseMode.name());
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
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem mi_favorite = menu.findItem(R.id.mi_favorite);
		MenuItem mi_random = menu.findItem(R.id.mi_random);
		
		if(browseMode == BROWSE_CATEGORY.CATEGORY_RANDOM){
			mi_random.setChecked(true);
			mi_favorite.setChecked(false);
		}else{
			mi_random.setChecked(false);
			mi_favorite.setChecked(true);
		}
		
		return super.onPrepareOptionsMenu(menu);
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
		case R.id.mi_random: {
			if(browseMode == BROWSE_CATEGORY.CATEGORY_RANDOM)
				return true;
			browseMode = BROWSE_CATEGORY.CATEGORY_RANDOM;
			((LazyAdapter) listView.getAdapter()).clear();
			((LazyAdapter) listView.getAdapter()).notifyDataSetChanged();
			((LazyAdapter) listView.getAdapter()).setBrowseMode(browseMode);

			load(Utils.getOAuthToken());
			return true;
		}
		case R.id.mi_favorite: {
			if(browseMode == BROWSE_CATEGORY.CATEGORY_FAVORITE)
				return true;
			browseMode = BROWSE_CATEGORY.CATEGORY_FAVORITE;
			((LazyAdapter) listView.getAdapter()).clear();
			((LazyAdapter) listView.getAdapter()).notifyDataSetChanged();
			((LazyAdapter) listView.getAdapter()).setBrowseMode(browseMode);
			
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

	@Override
	public void onBackPressed() {
		if(iv.scaleStatus() == SCALE_STATUS.SCALE_UP)
			iv.startScaleDownAnimation();
		else
			super.onBackPressed();
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
			if(browseMode == BROWSE_CATEGORY.CATEGORY_RANDOM)
				new LoadRandomPhotostreamTask(this, adapter, 0).execute(oauth);
			else if (browseMode == BROWSE_CATEGORY.CATEGORY_FAVORITE)
				new LoadFavoritePhotostreamTask(this, adapter, 0).execute(oauth);
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		if(iv.scaleStatus() == SCALE_STATUS.SCALE_UP){
			iv.startScaleDownAnimation();
		}
		
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


	@Override
	public boolean onPhotoClickListener(View view, final Photo photo) {
		final Rect startBounds = new Rect();
		final Rect finalBounds = new Rect();
		final String url = photo.getMediumUrl();
		view.getGlobalVisibleRect(startBounds);
		
        final Point globalOffset = new Point();
        findViewById(R.id.container).getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);

		iv.setImageDrawable(((ImageView)view).getDrawable());
		iv.startScaleUpAnimation(startBounds, new Animator.AnimatorListener() {
			
			@Override
			public void onAnimationStart(Animator animation) {

			}
			
			@Override
			public void onAnimationRepeat(Animator animation) {
				
			}
			
			@Override
			public void onAnimationEnd(Animator animation) {
                Intent intent = new Intent(MainActivity.this, ImagePagerActivity.class);
                String[] IMAGES = new String[2];
                IMAGES[0] = photo.getLargeUrl();
                IMAGES[1] = photo.getLargeUrl();

                intent.putExtra(CV.INTENT_IMAGES, IMAGES);
                startActivity(intent);
                overridePendingTransition(0, 0);
			}
			
			@Override
			public void onAnimationCancel(Animator animation) {
				
			}
		});

		return true;
	}
}
