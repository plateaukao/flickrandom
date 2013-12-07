package info.plateaukao.flickrandom.tasks;

import android.os.AsyncTask;

import com.nostra13.universalimageloader.core.ImageLoader;

public class BaseAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {

	protected ImageLoader imageLoader = ImageLoader.getInstance();

	
	@Override
	protected Result doInBackground(Params... arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
