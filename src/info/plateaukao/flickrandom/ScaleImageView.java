
package info.plateaukao.flickrandom;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

/**
 * do scale animation for image
 */
public class ScaleImageView extends ImageView {
    private final static int ANIM_DURATION = 300;

    public enum SCALE_STATUS {
        SCALE_UP, SCALING, SCALE_DOWN
    }

    private Rect startBounds, canvasClipBounds, imagebounds;

    private Paint paint;

    private Animator scaleUpAnimator;

    private Animator scaleDownAnimator;

    private SCALE_STATUS scaleStatus = SCALE_STATUS.SCALE_DOWN;

    public SCALE_STATUS scaleStatus() {
        return scaleStatus;
    }


    private float expandRatio;

    private Bitmap bitmap;

    private AnimatorListener startAnimListener;

    @SuppressWarnings("unused")
    private void setExpandRatio(float ratio) {
        this.expandRatio = ratio;
        invalidate();
    }

    public ScaleImageView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public ScaleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public ScaleImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        paint = new Paint();
        canvasClipBounds = new Rect();

        scaleUpAnimator = ObjectAnimator.ofFloat(this, "expandRatio", 0.0f, 1.0f);
        scaleUpAnimator.setInterpolator(new DecelerateInterpolator());
        scaleUpAnimator.setDuration(ANIM_DURATION);
        scaleUpAnimator.addListener(new AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
                if (null != startAnimListener)
                    startAnimListener.onAnimationStart(animation);

                scaleStatus = SCALE_STATUS.SCALING;

                setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                if (null != startAnimListener)
                    startAnimListener.onAnimationRepeat(animation);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                scaleStatus = SCALE_STATUS.SCALE_UP;
                if (null != startAnimListener)
                    startAnimListener.onAnimationEnd(animation);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                scaleStatus = SCALE_STATUS.SCALE_DOWN;
                if (null != startAnimListener)
                    startAnimListener.onAnimationCancel(animation);
            }
        });

        scaleDownAnimator = ObjectAnimator.ofFloat(this, "expandRatio", 1.0f, 0.0f);
        scaleDownAnimator.setInterpolator(new DecelerateInterpolator());
        scaleDownAnimator.setDuration(ANIM_DURATION);
        scaleDownAnimator.addListener(new AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
                scaleStatus = SCALE_STATUS.SCALING;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                setVisibility(View.GONE);
                scaleStatus = SCALE_STATUS.SCALE_DOWN;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                setVisibility(View.GONE);
                scaleStatus = SCALE_STATUS.SCALE_DOWN;
            }
        });
    }

    public void startScaleUpAnimation(Rect startBounds, AnimatorListener listener) {
        this.startBounds = startBounds;
        bitmap = ((BitmapDrawable)getDrawable()).getBitmap();

        imagebounds = getDrawable().getBounds();

        scaleUpAnimator.start();

        startAnimListener = listener;
    }

    public void startScaleDownAnimation() {
        scaleDownAnimator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {

        int originalBoundWidth = startBounds.right - startBounds.left;
        int originalBoundHeight = startBounds.bottom - startBounds.top;

        float imageWidth = imagebounds.right - imagebounds.left;
        float imageHeight = imagebounds.bottom - imagebounds.top;

        float xRatio = originalBoundWidth / imageWidth;
        float yRatio = originalBoundHeight / imageHeight;
        
        float xFinalRatio = canvas.getWidth() / imageWidth;
        float yFinalRatio = canvas.getHeight() / imageHeight;
        float finalRatio = (xFinalRatio >= yFinalRatio)?yFinalRatio:xFinalRatio;

        // scale the image to outer bounds

        if (yRatio <= xRatio) {
        	int offset = canvas.getWidth() / 2 - (startBounds.right + startBounds.left)/2;
        	
        	canvasClipBounds.left = (int)(startBounds.left + (imageWidth*(xRatio-yRatio))/2);
        	canvasClipBounds.left -=  (finalRatio - yRatio)*imageWidth/2 * expandRatio;
        	canvasClipBounds.left += offset * expandRatio;
        	
        	canvasClipBounds.right = (int)(startBounds.right - (imageWidth*(xRatio - yRatio))/2);
        	canvasClipBounds.right +=  (finalRatio - yRatio)*imageWidth/2 * expandRatio;
        	canvasClipBounds.right += offset * expandRatio;

        	canvasClipBounds.top = (int)(startBounds.top * (1 - expandRatio));
        	canvasClipBounds.bottom = (int)(startBounds.bottom + (canvas.getHeight() - startBounds.bottom) * expandRatio);


        } else {
        	
        	int offset = canvas.getHeight() / 2 - (startBounds.bottom+startBounds.top)/2;
        	
        	canvasClipBounds.left = (int)(startBounds.left * (1 - expandRatio));
        	canvasClipBounds.right = (int)(startBounds.right + (canvas.getWidth() - startBounds.right) * expandRatio);

        	canvasClipBounds.top = (int)(startBounds.top + (imageHeight *(yRatio-xRatio))/2);
        	canvasClipBounds.top -=  (finalRatio - xRatio)*imageHeight/2 * expandRatio;
        	canvasClipBounds.top += offset * expandRatio;
        	
        	canvasClipBounds.bottom = (int)(startBounds.bottom - (imageHeight*(yRatio - xRatio))/2);
        	canvasClipBounds.bottom +=  (finalRatio - xRatio)*imageHeight/2 * expandRatio;
        	canvasClipBounds.bottom += offset * expandRatio;
        }

        paint.setAlpha((int)(255*expandRatio*expandRatio*expandRatio));
        canvas.drawColor(Color.BLACK);
        paint.setAlpha(255);
        canvas.drawBitmap(bitmap, null, canvasClipBounds, paint);
    }
}
