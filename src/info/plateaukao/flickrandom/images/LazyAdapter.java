/**
 * 
 */
package info.plateaukao.flickrandom.images;

import info.plateaukao.flickrandom.R;
import info.plateaukao.flickrandom.tasks.LoadRandomPhotostreamTask;
import info.plateaukao.flickrandom.utils.Utils;

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
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

/**
 * @author Toby Yu(yuyang226@gmail.com)
 * 
 */
public class LazyAdapter extends BaseAdapter {

	private static int STATUS_NORMAL = 0;
	private static int STATUS_LOADING = 1;
	private int status = STATUS_NORMAL;

	private static int PRELOAD_WINDOW = 10;

	protected ImageLoader imageLoader = ImageLoader.getInstance();
	private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();

	private Activity activity;
	private PhotoList photos;
	private static LayoutInflater inflater = null;

	DisplayImageOptions options;

	private int currentPageCount;

	public void setCurrentPageCount(int pageCount) {
		currentPageCount = pageCount;
	};

	public LazyAdapter(Activity a) {
		activity = a;
		photos = new PhotoList();

		inflater = (LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				a).threadPriority(Thread.NORM_PRIORITY - 2)
				.denyCacheImageMultipleSizesInMemory()
				//.discCacheFileNameGenerator(new Md5FileNameGenerator())
				.tasksProcessingOrder(QueueProcessingType.LIFO)
				//.writeDebugLogs() // Remove for release app
				.build();

		// Initialize ImageLoader with configuration.
		ImageLoader.getInstance().init(config);

		options = new DisplayImageOptions.Builder()
				.showImageOnLoading(R.drawable.ic_stub)
				.showImageForEmptyUri(R.drawable.ic_empty)
				.showImageOnFail(R.drawable.ic_error).cacheInMemory(true)
				.imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
				// .cacheOnDisc(true)
				.considerExifParams(true)
				.build();
				//.displayer(new RoundedBitmapDisplayer(20)).build();
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

	public void addAll(PhotoList list) {
		photos.addAll(list);
		status = STATUS_NORMAL;
	}

	public void clear() {
		photos.clear();
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View vi = convertView;
		// trigger loadmore
		if (getCount() - position < PRELOAD_WINDOW && status == STATUS_NORMAL) {
			new LoadRandomPhotostreamTask(activity, this, currentPageCount + 1)
					.execute(Utils.getOAuthToken());

			status = STATUS_LOADING;
		}

		PhotoViewHolder holder;
		if (convertView == null) {
			vi = inflater.inflate(R.layout.row, null);
			holder = new PhotoViewHolder(vi);
			vi.setTag(holder);
		} else {
			holder = (PhotoViewHolder) vi.getTag();
		}

		holder.updateData(photos.get(position));

		vi.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String Url = ((PhotoViewHolder)v.getTag()).photo.getUrl();

				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri
						.parse(Url));
				activity.startActivity(browserIntent);

			}
		});

		return vi;
	}

	private static class AnimateFirstDisplayListener extends
			SimpleImageLoadingListener {

		static final List<String> displayedImages = Collections
				.synchronizedList(new LinkedList<String>());

		@Override
		public void onLoadingComplete(String imageUri, View view,
				Bitmap loadedImage) {
			if (loadedImage != null) {
				ImageView imageView = (ImageView) view;
				boolean firstDisplay = !displayedImages.contains(imageUri);
				if (firstDisplay) {
					FadeInBitmapDisplayer.animate(imageView, 500);
					displayedImages.add(imageUri);
				}
			}
		}
	}

	private class PhotoViewHolder {
		TextView tvTitle;
		TextView tvSet;
		TextView tvDate;
		ImageView ivPhoto;

		private Photo photo;

		public PhotoViewHolder(View v) {

			ivPhoto = (ImageView) v.findViewById(R.id.imageIcon);
			tvTitle = (TextView) v.findViewById(R.id.imageTitle);
			tvSet = (TextView) v.findViewById(R.id.imageSet);
			tvDate = (TextView) v.findViewById(R.id.imageDate);

			ivPhoto.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					String Url = photo.getLargeUrl();
					Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri
							.parse(Url));
					activity.startActivity(browserIntent);

				}
			});
		}

		public void updateData(Photo p) {
			this.photo = p;

			tvTitle.setText(photo.getTitle());
			tvSet.setText("");
			if(photo.getDateTaken() != null)
				tvDate.setText(photo.getDateTaken().toString());
			imageLoader.displayImage(photo.getMediumUrl(), ivPhoto, options,
					animateFirstListener);
		}
	}
}
