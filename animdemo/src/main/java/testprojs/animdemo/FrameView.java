package testprojs.animdemo;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/4/18.
 */

public class FrameView extends SurfaceView implements SurfaceHolder.Callback {
    private int[] IMAGES = {
            R.drawable.bg_1,R.drawable.bg_2,R.drawable.bg_3,R.drawable.bg_4,
            R.drawable.bg_5,R.drawable.bg_6
    };
    private final static int MSG_DECODE_IMAGE = 100;
    private final static int MSG_DESTORY = 101;

    private final static int CACHE_SIZE = 5;
//    private final static int FPS = 1000 / 60;
        private final static int FPS = 300;

    private Context mContext;
    private int mIndex = -1;
    private boolean mDrawing = false;
    private Handler mDecodeHandler;
    private Map<Integer, Bitmap> mFramePool = new HashMap<>(CACHE_SIZE);

    private Thread mDrawThread;

    private Rect mRect ;

    private boolean mBigFrame = true;
    private AssetManager mAssetManager;

    public FrameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        getHolder().addCallback(this);

        if(mBigFrame) {
            mAssetManager = context.getAssets();
        }

        startDecodeThread();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if(mBigFrame) {
            setMeasuredDimension(400, 400);
        } else {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            Bitmap bmp = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.bg_1, options);

            Log.d("FrameView", "width->" + options.outWidth + ", height->" + options.outHeight);

            setMeasuredDimension(options.outWidth, options.outHeight);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mRect = new Rect(0, 0, width, height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stopAnim();
    }

    public void startAnim() {
        mIndex = 0;
        mDrawing = true;

        if(mFramePool.isEmpty()) {
            Message msg = mDecodeHandler.obtainMessage();
            msg.what = MSG_DECODE_IMAGE;
            msg.arg1 = -1;
            mDecodeHandler.sendMessage(msg);
        }

        mDrawThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (mDrawing) {
                    long now = System.currentTimeMillis();
                    drawFrameImage();
                    long delta = System.currentTimeMillis() - now;
                    try {
                        Thread.sleep((FPS - delta) > 0 ? (FPS - delta) : 0);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        mDrawThread.start();
    }

    public void stopAnim() {
        mDrawing = false;
        mDrawThread.interrupt();
        mIndex = 0;
        mFramePool.clear();
    }



    private void startDecodeThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();

                mDecodeHandler = new Handler(Looper.myLooper()) {
                    @Override
                    public void handleMessage(Message msg) {
                        if(msg.what == MSG_DECODE_IMAGE) {
                            decodeBitmap(msg.arg1);
                        } else if(msg.what == MSG_DESTORY) {
                            getLooper().quit();
                        }
                    }
                };
                decodeBitmap(-1);
                Looper.loop();
            }
        }).start();
    }

    private void drawFrameImage() {
        Log.d("FrameView", "drawFrame index is " + mIndex);

        if(!mFramePool.containsKey(mIndex)) {
            Log.d("FrameView", "drawFrame not contains ");
            return;
        }

        Bitmap bmp = mFramePool.get(mIndex);

        Message msg = mDecodeHandler.obtainMessage();
        msg.what = MSG_DECODE_IMAGE;
        msg.arg1 = mIndex;
        mDecodeHandler.sendMessage(msg);

        Canvas canvas = getHolder().lockCanvas();
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        canvas.drawColor(Color.WHITE);
//        canvas.drawBitmap(bmp, 0, 0, null);
        canvas.drawBitmap(bmp, null, mRect, null);
        getHolder().unlockCanvasAndPost(canvas);

        bmp.recycle();

        if(mBigFrame) {
            if (mIndex == (981 - 1)) {
                mIndex = 0;
            } else {
                mIndex++;
            }
        } else {
            if (mIndex == IMAGES.length - 1) {
                mIndex = 0;
            } else {
                mIndex++;
            }
        }
    }

    private void decodeBitmap(int curIndex) {
        if(mBigFrame) {
            try {
                if(curIndex == -1) {
                    mFramePool.clear();
                    for(int i = 0; i< CACHE_SIZE; i++) {
                        mFramePool.put(i, BitmapFactory.decodeStream(
                                mAssetManager.open("w_rename/image_" + (curIndex + 1) + ".png")));
                    }
                } else {
                    mFramePool.remove(curIndex);
                    if (curIndex + CACHE_SIZE < 981) {
                        mFramePool.put(curIndex + CACHE_SIZE, BitmapFactory.decodeStream(
                                        mAssetManager.open("w_rename/image_" + (curIndex + CACHE_SIZE) + ".png")));
                    } else {
                        mFramePool.put((curIndex + CACHE_SIZE) - 981, BitmapFactory.decodeStream(
                                        mAssetManager.open("w_rename/image_" + (curIndex + CACHE_SIZE - 981) + ".png")));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {

            if (curIndex == -1) {
                mFramePool.clear();
                Log.d("FrameView", "preparing...");
                for (int i = 0; i < CACHE_SIZE; i++) {
                    mFramePool.put(i, BitmapFactory.decodeResource(mContext.getResources(), IMAGES[i]));
                }
            } else {
                mFramePool.remove(curIndex);
                if (curIndex + CACHE_SIZE < IMAGES.length) {
                    mFramePool.put(curIndex + CACHE_SIZE,
                            BitmapFactory.decodeResource(mContext.getResources(), IMAGES[curIndex + CACHE_SIZE]));
                } else {
                    mFramePool.put((curIndex + CACHE_SIZE) - IMAGES.length,
                            BitmapFactory.decodeResource(mContext.getResources(), IMAGES[curIndex + CACHE_SIZE - IMAGES.length]));
                }
            }
        }
    }


}
