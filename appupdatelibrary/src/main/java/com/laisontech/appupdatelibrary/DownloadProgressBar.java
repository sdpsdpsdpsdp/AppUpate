package com.laisontech.appupdatelibrary;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.Xfermode;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ProgressBar;

import com.laisontech.appupdatelibrary.interfaces.OnProgressStateChangeListener;


/**
 * 进度条下载进度条
 */
public class DownloadProgressBar extends ProgressBar implements View.OnClickListener {
    public static final int STATUS_PROGRESS_BAR_START_DOWNLOAD = 0;//开始下载
    public static final int STATUS_PROGRESS_BAR_DOWNLOADING = 1;//下载之中
    public static final int STATUS_PROGRESS_BAR_PAUSE = 2;//暂停下载
    public static final int STATUS_PROGRESS_BAR_INSTALL = 3;//下载完成
    public static final int STATUS_PROGRESS_BAR_OPEN = 4;//打开
    public static final int STATUS_PROGRESS_BAR_WAIT = 5;//等待
    public static final int STATUS_PROGRESS_BAR_ERROR = 6;//错误
    public static final int STATUS_PROGRESS_BAR_UPDATE = 7;//更新
    private int mCurrentState = STATUS_PROGRESS_BAR_START_DOWNLOAD;
    private int mProgressTextColor = Color.parseColor("#4599E9");//进度条的颜色
    private int mProgressBarColor = Color.parseColor("#D4E9FA");//进度条中文字的颜色
    private int mProgressTextSize = 15;//进度条中字体的大小
    private int mProgressMinHeight = 20;//进度条的最小高度
    private int mProgressMinWidth = 30;//进度条的最小宽度
    private Paint mPaint;//画笔
    private Path mRoundRectPath;//View本身圆角路径
    private Path mProgressPath;//View中的进度路径
    private int mValidWidth;//获取View本身的有效绘制宽度，padding不绘制
    private int mValidHeight;//获取View本身的有效绘制高度，padding不绘制
    private OnProgressStateChangeListener mStateChangeListener;//接口监听器
    private String textShowInfo;
    private Paint textPaint;
    private Rect textRect;

    public void setStateChangeListener(OnProgressStateChangeListener stateChangeListener) {
        this.mStateChangeListener = stateChangeListener;
    }

    public void setState(int state) {
        this.mCurrentState = state;
        postInvalidate();
    }

    public int getCurrentState() {
        return mCurrentState;
    }

    public String getTextShowInfo() {
        return textShowInfo;
    }

    public void setTextShowInfo(String textShowInfo) {
        this.textShowInfo = textShowInfo;
    }

    public DownloadProgressBar(Context context) {
        this(context, null);
    }

    public DownloadProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DownloadProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        obtainStyledAttributes(attrs);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(mProgressTextSize);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(mProgressTextSize);

        textRect = new Rect();
        setOnClickListener(this);
    }


    /* 获取用户自定义设置的属性值*/
    private void obtainStyledAttributes(AttributeSet attrs) {
        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.DownloadProgressBar);
        mProgressTextColor = ta.getColor(R.styleable.DownloadProgressBar_download_progress_text_color, mProgressTextColor);
        mProgressTextSize = (int) ta.getDimension(R.styleable.DownloadProgressBar_download_progress_text_size, mProgressTextSize);
        mProgressBarColor = ta.getColor(R.styleable.DownloadProgressBar_download_progress_bar_color, mProgressBarColor);
        mProgressMinHeight = (int) ta.getDimension(R.styleable.DownloadProgressBar_download_progress_min_height, mProgressMinHeight);
        mProgressMinWidth = (int) ta.getDimension(R.styleable.DownloadProgressBar_download_progress_min_width, mProgressMinWidth);
        ta.recycle();
    }


    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (heightMode == MeasureSpec.AT_MOST && widthMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(measureWidthForAtMost(widthSize), measureHeightForAtMost(heightSize));
        } else if (heightMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(widthSize, measureHeightForAtMost(heightSize));
        } else if (widthMeasureSpec == MeasureSpec.AT_MOST) {
            setMeasuredDimension(measureWidthForAtMost(widthSize), heightSize);
        }
    }

    private int measureHeightForAtMost(int heightSize) {
        int textHeight = (int) (mPaint.descent() - mPaint.ascent()) + convertDPtoPX(10);
        int h = Math.max(textHeight, Math.min(heightSize, mProgressMinHeight));
        return h;
    }

    private int measureWidthForAtMost(int widthSize) {
        int width = Math.min(mProgressMinWidth, widthSize);
        return width;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mValidWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        mValidHeight = getHeight() - getPaddingTop() - getPaddingBottom();
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        switch (mCurrentState) {
            case STATUS_PROGRESS_BAR_START_DOWNLOAD:
                drawProgressOnStart(canvas);
                break;
            case STATUS_PROGRESS_BAR_UPDATE:
                drawProgressOnUpdate(canvas);
                break;
            case STATUS_PROGRESS_BAR_DOWNLOADING:
                drawProgressOnDownload(canvas);
                break;
            case STATUS_PROGRESS_BAR_PAUSE:
                drawProgressOnPause(canvas);
                break;
            case STATUS_PROGRESS_BAR_INSTALL:
                drawProgressOnFinished(canvas);
                break;
            case STATUS_PROGRESS_BAR_OPEN:
                drawProgressOnOpen(canvas);
                break;
            case STATUS_PROGRESS_BAR_WAIT:
                drawProgressOnWait(canvas);
                break;
            case STATUS_PROGRESS_BAR_ERROR:
                drawProgressOnError(canvas);
                break;
        }
    }

    private void drawProgressOnUpdate(Canvas canvas) {
        drawProgressRectBackground(canvas);
        drawProgressText(canvas, getResources().getString(R.string.Update));
    }

    //下载的时候
    private void drawProgressOnDownload(Canvas canvas) {
        drawProgressRectBackground(canvas);
        drawProgressRectWithClip(canvas, (int) (mValidWidth * (getProgress() * 1.0f / getMax())));
        int progress = (int) (100 * (getProgress() * 1.0f / getMax()));
        Log.e("onShowProgress", "drawProgressOnDownload: " + progress);
        String progressValue = TextUtils.concat(String.valueOf(progress), "%").toString();
        drawProgressText(canvas, progressValue);
        if (getProgress() == getMax()) {
            postInvalidateDelayed(50);
            mCurrentState = STATUS_PROGRESS_BAR_INSTALL;
        }
    }

    //暂停的时候
    private void drawProgressOnPause(Canvas canvas) {
        drawProgressRectBackground(canvas);
        drawProgressRectWithClip(canvas, (int) (mValidWidth * (getProgress() * 1.0f / getMax())));
        drawProgressText(canvas, getResources().getString(R.string.DownloadResume));
    }

    //下载完成的时候
    private void drawProgressOnFinished(Canvas canvas) {
        drawProgressRectBackground(canvas);
        drawProgressRectWithClip(canvas, mValidWidth * 100);
        drawProgressText(canvas, getResources().getString(R.string.DownloadInstall));
    }

    //还没有开始下载的时候
    private void drawProgressOnStart(Canvas canvas) {
        drawProgressRectBackground(canvas);
        drawProgressText(canvas, getResources().getString(R.string.Download));
    }

    //绘制一个圆角矩形路径
    private void drawRoundRectPath() {
        if (mRoundRectPath == null) {
            mRoundRectPath = new Path();
        } else {
            mRoundRectPath.reset();
        }
        mRoundRectPath.moveTo(mValidHeight / 2, mValidHeight);
        mRoundRectPath.arcTo(new RectF(0, 0, mValidHeight, mValidHeight), 90, 180);
        mRoundRectPath.lineTo(mValidWidth - mValidHeight / 2, 0);
        mRoundRectPath.arcTo(new RectF(mValidWidth - mValidHeight, 0, mValidWidth, mValidHeight), -90, 180);
        mRoundRectPath.lineTo(mValidWidth - mValidHeight / 2, mValidHeight);
        mRoundRectPath.lineTo(mValidHeight / 2, mValidHeight);
        mRoundRectPath.close();
    }

    //绘制一个进度路径
    private void drawProgressPath(int progress) {
        if (mProgressPath == null) {
            mProgressPath = new Path();
        } else {
            mProgressPath.reset();
        }
        RectF rectF = new RectF(0, 0, progress, mValidHeight);
        mProgressPath.addRect(rectF, Path.Direction.CCW);
    }


    //  绘制矩形的下载进度(这里使用的是裁剪的方法)
    private void drawProgressRectWithClip(Canvas canvas, int progress) {
        mPaint.setStyle(Paint.Style.FILL);
        //根据进度比率计算出当前的进度值对应的宽度
        canvas.save();
        canvas.translate(getPaddingLeft(), getPaddingTop());
        //裁剪矩形路径
        drawRoundRectPath();
        canvas.clipPath(mRoundRectPath);
        //裁剪进度路径
        drawProgressPath(progress);
        canvas.clipPath(mProgressPath, Region.Op.INTERSECT);
        canvas.drawColor(mProgressBarColor);
        canvas.restore();
    }

    private Xfermode mXfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);

    //绘制矩形的下载进度(也可以使用的是PorterDuffXfremode的方法)
    private void drawProgressRectWithXfermode(Canvas canvas) {
        mPaint.setStyle(Paint.Style.FILL);
        //根据进度比率计算出当前的进度值对应的宽度
        int progress = (int) (mValidWidth * (getProgress() * 1.0f / getMax()));
        int layer = canvas.saveLayer(0, 0, progress, getHeight(), null, Canvas.ALL_SAVE_FLAG);
        canvas.translate(getPaddingLeft(), getPaddingTop());
        drawRoundRectPath();
        mPaint.setColor(mProgressBarColor);
        canvas.drawPath(mRoundRectPath, mPaint);
        drawProgressPath(progress);
        mPaint.setXfermode(mXfermode);
        canvas.drawPath(mProgressPath, mPaint);
        canvas.restoreToCount(layer);
        mPaint.setXfermode(null);

    }


    //  绘制背景矩形
    private void drawProgressRectBackground(Canvas canvas) {
        canvas.save();
        canvas.translate(getPaddingLeft(), getPaddingTop());
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(mProgressBarColor);
        drawRoundRectPath();
        canvas.drawPath(mRoundRectPath, mPaint);
        canvas.restore();
    }

    //  绘制进度中的文字
    private void drawProgressText(Canvas canvas, String text) {
        setTextShowInfo(text);
        drawProgressText(text, canvas);
        drawColorProgressText(text, canvas);
    }

    /**
     * 进度提示文本
     *
     * @param canvas
     */
    private void drawProgressText(String progressText, Canvas canvas) {
        canvas.save();
        if (mCurrentState == STATUS_PROGRESS_BAR_ERROR) {
            textPaint.setColor(mProgressTextColor);
        } else {
            textPaint.setColor(mProgressTextColor);
        }
        textPaint.getTextBounds(progressText, 0, progressText.length(), textRect);
        int tWidth = textRect.width();
        int tHeight = textRect.height();
        float xCoordinate = (getMeasuredWidth() - tWidth) / 2 - convertDPtoPX(1);
        ;
        float yCoordinate = (float) ((getMeasuredHeight() + tHeight) / 2 - convertDPtoPX(1));
        canvas.drawText(progressText, xCoordinate, yCoordinate, textPaint);
        canvas.restore();
    }

    /**
     * 变色处理
     *
     * @param canvas
     */
    @SuppressLint("WrongConstant")
    private void drawColorProgressText(String progressText, Canvas canvas) {
        textPaint.setColor(Color.WHITE);
        int tWidth = textRect.width();
        int tHeight = textRect.height();
        float xCoordinate = (getMeasuredWidth() - tWidth) / 2 - convertDPtoPX(1);
        float yCoordinate = (float) ((getMeasuredHeight() + tHeight) / 2 - convertDPtoPX(1));
        float progressWidth;
        if (mCurrentState == STATUS_PROGRESS_BAR_OPEN) {
            progressWidth = (100f / 100f) * getMeasuredWidth();
        } else {
            progressWidth = (getProgress() / 100f) * getMeasuredWidth();
        }
        if (progressWidth > xCoordinate) {
            canvas.save(Canvas.CLIP_SAVE_FLAG);
            float right = Math.min(progressWidth, xCoordinate + tWidth * 1.1f);
            canvas.clipRect(xCoordinate, 0, right, getMeasuredHeight());
            canvas.drawText(progressText, xCoordinate, yCoordinate, textPaint);
            canvas.restore();
        }
    }

    //绘制打开的显示
    private void drawProgressOnOpen(Canvas canvas) {
        drawProgressRectBackground(canvas);
        drawProgressRectWithClip(canvas, mValidWidth * 100);
        drawProgressText(canvas, getResources().getString(R.string.Open));
    }

    //绘制等待的显示
    private void drawProgressOnWait(Canvas canvas) {
        drawProgressRectBackground(canvas);
        drawProgressRectWithClip(canvas, (int) (mValidWidth * (getProgress() * 1.0f / getMax())));
        drawProgressText(canvas, getResources().getString(R.string.DownloadWaiting));
    }

    //绘制错误的显示
    private void drawProgressOnError(Canvas canvas) {
        drawProgressRectBackground(canvas);
        drawProgressRectWithClip(canvas, (int) (mValidWidth * (getProgress() * 1.0f / getMax())));
        drawProgressText(canvas, getResources().getString(R.string.Error));
    }

    @Override
    public final void onClick(View v) {
        int progress = getProgress();
        int max = getMax();
        if (progress == 0 && mCurrentState == STATUS_PROGRESS_BAR_START_DOWNLOAD) {
            if (mStateChangeListener != null) {
                mStateChangeListener.onProgressBarStateChange(STATUS_PROGRESS_BAR_START_DOWNLOAD);
            }
        } else if (mCurrentState == STATUS_PROGRESS_BAR_UPDATE) {
            if (mStateChangeListener != null) {
                mStateChangeListener.onProgressBarStateChange(STATUS_PROGRESS_BAR_UPDATE);
            }
        } else if (progress >= 0 && progress < max && mCurrentState == STATUS_PROGRESS_BAR_DOWNLOADING) {
            if (mStateChangeListener != null) {
                mStateChangeListener.onProgressBarStateChange(STATUS_PROGRESS_BAR_DOWNLOADING);
            }
        } else if (progress >= 0 && progress < max && mCurrentState == STATUS_PROGRESS_BAR_PAUSE) {
            if (mStateChangeListener != null) {
                mStateChangeListener.onProgressBarStateChange(STATUS_PROGRESS_BAR_PAUSE);
            }
        } else if (progress == max && mCurrentState != STATUS_PROGRESS_BAR_OPEN) {
            mCurrentState = STATUS_PROGRESS_BAR_INSTALL;    //安装，状态还是安装
            if (mStateChangeListener != null) {
                mStateChangeListener.onProgressBarStateChange(STATUS_PROGRESS_BAR_INSTALL);
            }
        } else if (mCurrentState == STATUS_PROGRESS_BAR_WAIT) {
            if (mStateChangeListener != null) {
                mStateChangeListener.onProgressBarStateChange(STATUS_PROGRESS_BAR_WAIT);
            }
        } else if (mCurrentState == STATUS_PROGRESS_BAR_ERROR) {
            if (mStateChangeListener != null) {
                mStateChangeListener.onProgressBarStateChange(STATUS_PROGRESS_BAR_ERROR);
            }
        } else if (mCurrentState == STATUS_PROGRESS_BAR_OPEN) {
            if (mStateChangeListener != null) {
                mStateChangeListener.onProgressBarStateChange(STATUS_PROGRESS_BAR_OPEN);
            }
        }
        postInvalidateDelayed(40);
    }


    /*将dp转化为px*/
    private int convertDPtoPX(int dpValue) {
        float pxValue = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, getResources().getDisplayMetrics());
        return (int) (pxValue + 0.5f);
    }

    /*将sp转化为px*/
    private int convertSPtoPX(int spValue) {
        float pxValue = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spValue, getResources().getDisplayMetrics());
        return (int) (pxValue + 0.5f);
    }

}