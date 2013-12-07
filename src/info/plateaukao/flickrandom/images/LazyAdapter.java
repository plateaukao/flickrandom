/**
 * 
 */
package info.plateaukao.flickrandom.images;

import info.plateaukao.flickrandom.R;
import info.plateaukao.flickrandom.images.ImageUtils.DownloadedDrawable;
import info.plateaukao.flickrandom.tasks.ImageDownloadTask;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.googlecode.flickrjandroid.photos.Photo;
import com.googlecode.flickrjandroid.photos.PhotoList;

/**
 * @author Toby Yu(yuyang226@gmail.com)
 *
 */
public class LazyAdapter extends BaseAdapter {
    
    private Activity activity;
    private PhotoList photos;
    private static LayoutInflater inflater=null;
    
    public LazyAdapter(Activity a, PhotoList d) {
        activity = a;
        photos = d;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
        if(convertView == null)
            vi = inflater.inflate(R.layout.row, null);

        vi.setTag(photos.get(position));
        vi.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String Url = ((Photo)v.getTag()).getUrl();

				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Url));
				activity.startActivity(browserIntent);
				
			}
		});

        TextView text=(TextView)vi.findViewById(R.id.imageTitle);;
        ImageView image=(ImageView)vi.findViewById(R.id.imageIcon);
        image.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String Url = ((Photo)((View) v.getParent()).getTag()).getLargeUrl();
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Url));
				activity.startActivity(browserIntent);
				
			}
		});

        Photo photo = photos.get(position);
        text.setText(photo.getTitle());
        if (image != null) {
        	ImageDownloadTask task = new ImageDownloadTask(image);
            Drawable drawable = new DownloadedDrawable(task);
            image.setImageDrawable(drawable);
            //task.execute(photo.getSmallSquareUrl());
            task.execute(photo.getThumbnailUrl());
        }
        
        ImageView viewIcon = (ImageView)vi.findViewById(R.id.viewIcon);
        if (photo.getViews() >= 0) {
        	viewIcon.setImageResource(R.drawable.views);
        	TextView viewsText = (TextView)vi.findViewById(R.id.viewsText);
        	viewsText.setText(String.valueOf(photo.getViews()));
        } else {
        	viewIcon.setImageBitmap(null);
        }
        
        return vi;
    }
}
