/**
 * 
 */
package info.plateaukao.flickrandom.images;

import info.plateaukao.flickrandom.R;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.googlecode.flickrjandroid.photos.Photo;
import com.googlecode.flickrjandroid.photos.PhotoList;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

/**
 * @author Toby Yu(yuyang226@gmail.com)
 * 
 */
public class LazyAdapter extends BaseAdapter {

	protected ImageLoader imageLoader = ImageLoader.getInstance();
	private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
	
	private Activity activity;
	private PhotoList photos;
	private static LayoutInflater inflater = null;

	DisplayImageOptions options;

	public LazyAdapter(Activity a, PhotoList d) {
		activity = a;
		photos = d;
		inflater = (LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				a).threadPriority(Thread.NORM_PRIORITY - 2)
				.denyCacheImageMultipleSizesInMemory()
				.discCacheFileNameGenerator(new Md5FileNameGenerator())
				.tasksProcessingOrder(QueueProcessingType.LIFO)
				.writeDebugLogs() // Remove for release app
				.build();
		
		// Initialize ImageLoader with configuration.
		ImageLoader.getInstance().init(config);

		options = new DisplayImageOptions.Builder()
				.showImageOnLoading(R.drawable.ic_stub)
				.showImageForEmptyUri(R.drawable.ic_empty)
				.showImageOnFail(R.drawable.ic_error)
				.cacheInMemory(true)
				// .cacheOnDisc(true)
				.considerExifParams(true)
				.displayer(new RoundedBitmapDisplayer(20)).build();
	}

	public int getCount() {
		return photos.size();
	}

	public Object getItem(int position) {
		return position;
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View vi = convertView;
		if (convertView == null)
			vi = inflater.inflate(R.layout.row, null);

		vi.setTag(photos.get(position));
		vi.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String Url = ((Photo) v.getTag()).getUrl();

				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri
						.parse(Url));
				activity.startActivity(browserIntent);

			}
		});

		TextView text = (TextView) vi.findViewById(R.id.imageTitle);
		;
		ImageView image = (ImageView) vi.findViewById(R.id.imageIcon);
		image.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String Url = ((Photo) ((View) v.getParent()).getTag())
						.getLargeUrl();
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri
						.parse(Url));
				activity.startActivity(browserIntent);

			}
		});

		Photo photo = photos.get(position);
		text.setText(photo.getTitle());
		if (image != null) {
			imageLoader.displayImage(photo.getSmallSquareUrl(), image, options,
					animateFirstListener);
			/*
			 * ImageDownloadTask task = new ImageDownloadTask(image); Drawable
			 * drawable = new DownloadedDrawable(task);
			 * image.setImageDrawable(drawable);
			 * //task.execute(photo.getSmallSquareUrl());
			 * task.execute(photo.getThumbnailUrl());
			 */
		}

		ImageView viewIcon = (ImageView) vi.findViewById(R.id.viewIcon);
		if (photo.getViews() >= 0) {
			viewIcon.setImageResource(R.drawable.views);
			TextView viewsText = (TextView) vi.findViewById(R.id.viewsText);
			viewsText.setText(String.valueOf(photo.getViews()));
		} else {
			viewIcon.setImageBitmap(null);
		}

		return vi;
	}
	
    private static class AnimateFirstDisplayListener extends SimpleImageLoadingListener {

        static final List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                if (loadedImage != null) {
                        ImageView imageView = (ImageView) view;
                        boolean firstDisplay = !displayedImages.contains(imageUri);
                        if (firstDisplay) {
                                FadeInBitmapDisplayer.animate(imageView, 300);
                                displayedImages.add(imageUri);
                        }
                }
        }
}
}
