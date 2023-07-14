package com.github.kr328.clash.design.view;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.MaskFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.github.kr328.clash.design.BuildConfig;
import com.github.kr328.clash.design.R.styleable;

//import com.github.kr328.clash.design.view.R.styleable;

public class CircleProgressBar extends View {
    public static final int LINE = 0;
    public static final int SOLID = 1;
    public static final int SOLID_LINE = 2;
    public static final int LINEAR = 0;
    public static final int RADIAL = 1;
    public static final int SWEEP = 2;
    private static final int DEFAULT_MAX = 100;
    private static final float MAX_DEGREE = 360.0F;
    private static final float LINEAR_START_DEGREE = 90.0F;
    private static final int DEFAULT_START_DEGREE = -90;
    private static final int DEFAULT_LINE_COUNT = 45;
    private static final float DEFAULT_LINE_WIDTH = 4.0F;
    private static final float DEFAULT_PROGRESS_TEXT_SIZE = 11.0F;
    private static final float DEFAULT_PROGRESS_STROKE_WIDTH = 1.0F;
    private static final String COLOR_FFF2A670 = "#fff2a670";
    private static final String COLOR_FFD3D3D5 = "#ffe3e3e5";
    private final RectF mProgressRectF;
    private final RectF mInnerProgressRectF;
    private final RectF mBoundsRectF;
    private final Rect mProgressTextRect;
    private final Paint mProgressPaint;
    private final Paint mProgressBackgroundPaint;
    private final Paint mProgressTextPaint;
    private float mRadius;
    private float mCenterX;
    private float mCenterY;
    private int mProgress;
    private int mMax;
    private int mLineCount;
    private float mLineWidth;
    private float mProgressStrokeWidth;
    private float mProgressTextSize;
    private int mProgressStartColor;
    private int mProgressEndColor;
    private int mProgressTextColor;
    private int mProgressBackgroundColor;
    private int mStartDegree;
    private boolean mDrawBackgroundOutsideProgress;
    private ProgressFormatter mProgressFormatter;
    private int mStyle;
    private int mShader;
    private Paint.Cap mCap;
    private int mBlurRadius;
    private BlurMaskFilter.Blur mBlurStyle;

    public CircleProgressBar(Context context) {
        this(context, (AttributeSet) null);
    }

    public CircleProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mProgressRectF = new RectF();
        this.mInnerProgressRectF = new RectF();
        this.mBoundsRectF = new RectF();
        this.mProgressTextRect = new Rect();
        this.mProgressPaint = new Paint(1);
        this.mProgressBackgroundPaint = new Paint(1);
        this.mProgressTextPaint = new TextPaint(1);
        this.mMax = 100;
        this.mProgressFormatter = new DefaultProgressFormatter();
        if (BuildConfig.DEBUG) {
            this.mProgress = 52;
        }
        this.initFromAttributes(context, attrs);
        this.initPaint();
    }

    private static int dip2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5F);
    }

    private void initFromAttributes(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, styleable.CircleProgressBar);
        this.mLineCount = a.getInt(styleable.CircleProgressBar_line_count, 45);
        this.mStyle = a.getInt(styleable.CircleProgressBar_progress_style, 0);
        this.mShader = a.getInt(styleable.CircleProgressBar_progress_shader, 0);
        this.mCap = a.hasValue(styleable.CircleProgressBar_progress_stroke_cap) ? Paint.Cap.values()[a.getInt(styleable.CircleProgressBar_progress_stroke_cap, 0)] : Paint.Cap.BUTT;
        this.mLineWidth = (float) a.getDimensionPixelSize(styleable.CircleProgressBar_line_width, dip2px(this.getContext(), 4.0F));
        this.mProgressTextSize = (float) a.getDimensionPixelSize(styleable.CircleProgressBar_progress_text_size, dip2px(this.getContext(), 11.0F));
        this.mProgressStrokeWidth = (float) a.getDimensionPixelSize(styleable.CircleProgressBar_progress_stroke_width, dip2px(this.getContext(), 1.0F));
        this.mProgressStartColor = a.getColor(styleable.CircleProgressBar_progress_start_color, Color.parseColor("#fff2a670"));
        this.mProgressEndColor = a.getColor(styleable.CircleProgressBar_progress_end_color, Color.parseColor("#fff2a670"));
        this.mProgressTextColor = a.getColor(styleable.CircleProgressBar_progress_text_color, Color.parseColor("#fff2a670"));
        this.mProgressBackgroundColor = a.getColor(styleable.CircleProgressBar_progress_background_color, Color.parseColor("#ffe3e3e5"));
        this.mStartDegree = a.getInt(styleable.CircleProgressBar_progress_start_degree, -90);
        this.mDrawBackgroundOutsideProgress = a.getBoolean(styleable.CircleProgressBar_drawBackgroundOutsideProgress, false);
        this.mBlurRadius = a.getDimensionPixelSize(styleable.CircleProgressBar_progress_blur_radius, 0);
        int blurStyle = a.getInt(styleable.CircleProgressBar_progress_blur_style, 0);
        switch (blurStyle) {
            case 1:
                this.mBlurStyle = BlurMaskFilter.Blur.SOLID;
                break;
            case 2:
                this.mBlurStyle = BlurMaskFilter.Blur.OUTER;
                break;
            case 3:
                this.mBlurStyle = BlurMaskFilter.Blur.INNER;
                break;
            default:
                this.mBlurStyle = BlurMaskFilter.Blur.NORMAL;
        }

        a.recycle();
    }

    private void initPaint() {
        this.mProgressTextPaint.setTextAlign(Paint.Align.CENTER);
        this.mProgressTextPaint.setTextSize(this.mProgressTextSize);
        this.mProgressPaint.setStyle(this.mStyle == 1 ? Paint.Style.FILL : Paint.Style.STROKE);
        this.mProgressPaint.setStrokeWidth(this.mProgressStrokeWidth);
        this.mProgressPaint.setColor(this.mProgressStartColor);
        this.mProgressPaint.setStrokeCap(this.mCap);
        this.updateMaskBlurFilter();
        this.mProgressBackgroundPaint.setStyle(this.mStyle == 1 ? Paint.Style.FILL : Paint.Style.STROKE);
        this.mProgressBackgroundPaint.setStrokeWidth(this.mProgressStrokeWidth);
        this.mProgressBackgroundPaint.setColor(this.mProgressBackgroundColor);
        this.mProgressBackgroundPaint.setStrokeCap(this.mCap);
    }

    private void updateMaskBlurFilter() {
        if (this.mBlurStyle != null && this.mBlurRadius > 0) {
            this.setLayerType(View.LAYER_TYPE_SOFTWARE, this.mProgressPaint);
            this.mProgressPaint.setMaskFilter(new BlurMaskFilter((float) this.mBlurRadius, this.mBlurStyle));
        } else {
            this.mProgressPaint.setMaskFilter((MaskFilter) null);
        }

    }

    private void updateProgressShader() {
        if (this.mProgressStartColor != this.mProgressEndColor) {
            Shader shader = null;
            switch (this.mShader) {
                case 0:
                    shader = new LinearGradient(this.mProgressRectF.left, this.mProgressRectF.top, this.mProgressRectF.left, this.mProgressRectF.bottom, this.mProgressStartColor, this.mProgressEndColor, Shader.TileMode.CLAMP);
                    Matrix matrix = new Matrix();
                    matrix.setRotate(90.0F, this.mCenterX, this.mCenterY);
                    ((Shader) shader).setLocalMatrix(matrix);
                    break;
                case 1:
                    shader = new RadialGradient(this.mCenterX, this.mCenterY, this.mRadius, this.mProgressStartColor, this.mProgressEndColor, Shader.TileMode.CLAMP);
                    break;
                case 2:
                    float radian = (float) ((double) this.mProgressStrokeWidth / Math.PI * 2.0 / (double) this.mRadius);
                    float rotateDegrees = (float) (-(this.mCap == Paint.Cap.BUTT && this.mStyle == 2 ? 0.0 : Math.toDegrees((double) radian)));
                    shader = new SweepGradient(this.mCenterX, this.mCenterY, new int[]{this.mProgressStartColor, this.mProgressEndColor}, new float[]{0.0F, 1.0F});
                    Matrix matrix_ = new Matrix();
                    matrix_.setRotate(rotateDegrees, this.mCenterX, this.mCenterY);
                    ((Shader) shader).setLocalMatrix(matrix_);
            }

            this.mProgressPaint.setShader((Shader) shader);
        } else {
            this.mProgressPaint.setShader((Shader) null);
            this.mProgressPaint.setColor(this.mProgressStartColor);
        }

    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        canvas.rotate((float) this.mStartDegree, this.mCenterX, this.mCenterY);
        this.drawProgress(canvas);
        canvas.restore();
        this.drawProgressText(canvas);
    }

    private void drawProgressText(Canvas canvas) {
        if (this.mProgressFormatter != null) {
            CharSequence progressText = this.mProgressFormatter.format(this.mProgress, this.mMax);
            if (!TextUtils.isEmpty(progressText)) {
                this.mProgressTextPaint.setTextSize(this.mProgressTextSize);
                this.mProgressTextPaint.setColor(this.mProgressTextColor);
                this.mProgressTextPaint.getTextBounds(String.valueOf(progressText), 0, progressText.length(), this.mProgressTextRect);
                canvas.drawText(progressText, 0, progressText.length(), this.mCenterX, this.mCenterY + (float) (this.mProgressTextRect.height() / 2), this.mProgressTextPaint);
            }
        }
    }

    private void drawProgress(Canvas canvas) {
        switch (this.mStyle) {
            case 0:
            default:
                this.drawLineProgress(canvas);
                break;
            case 1:
                this.drawSolidProgress(canvas);
                break;
            case 2:
                this.drawSolidLineProgress(canvas);
        }

    }

    private void drawLineProgress(Canvas canvas) {
        float unitDegrees = (float) (6.283185307179586 / (double) this.mLineCount);
        float outerCircleRadius = this.mRadius;
        float interCircleRadius = this.mRadius - this.mLineWidth;
        int progressLineCount = (int) ((float) this.mProgress / (float) this.mMax * (float) this.mLineCount);

        for (int i = 0; i < this.mLineCount; ++i) {
            float rotateDegrees = (float) i * -unitDegrees;
            float startX = this.mCenterX + (float) Math.cos((double) rotateDegrees) * interCircleRadius;
            float startY = this.mCenterY - (float) Math.sin((double) rotateDegrees) * interCircleRadius;
            float stopX = this.mCenterX + (float) Math.cos((double) rotateDegrees) * outerCircleRadius;
            float stopY = this.mCenterY - (float) Math.sin((double) rotateDegrees) * outerCircleRadius;
            if (this.mDrawBackgroundOutsideProgress) {
                if (i >= progressLineCount) {
                    canvas.drawLine(startX, startY, stopX, stopY, this.mProgressBackgroundPaint);
                }
            } else {
                canvas.drawLine(startX, startY, stopX, stopY, this.mProgressBackgroundPaint);
            }

            if (i < progressLineCount) {
                canvas.drawLine(startX, startY, stopX, stopY, this.mProgressPaint);
            }
        }
    }

    private void drawSolidProgress(Canvas canvas) {
        if (this.mDrawBackgroundOutsideProgress) {
            float startAngle = 360.0F * (float) this.mProgress / (float) this.mMax;
            float sweepAngle = 360.0F - startAngle;
            canvas.drawArc(this.mProgressRectF, startAngle, sweepAngle, true, this.mProgressBackgroundPaint);
        } else {
            canvas.drawArc(this.mProgressRectF, 0.0F, 360.0F, true, this.mProgressBackgroundPaint);
        }

        canvas.drawArc(this.mProgressRectF, 0.0F, 360.0F * (float) this.mProgress / (float) this.mMax, true, this.mProgressPaint);
    }

    private void drawSolidLineProgress(Canvas canvas) {
//        if (this.mDrawBackgroundOutsideProgress) {
//            float startAngle = 360.0F * (float) this.mProgress / (float) this.mMax;
//            float sweepAngle = 360.0F - startAngle;
//            canvas.drawArc(this.mProgressRectF, startAngle, sweepAngle, false, this.mProgressBackgroundPaint);
//        } else {
//            canvas.drawArc(this.mProgressRectF, 0.0F, 360.0F, false, this.mProgressBackgroundPaint);
//        }
        this.mProgressBackgroundPaint.setColor(Color.parseColor("#77e0e0e0"));
        canvas.drawArc(this.mProgressRectF, 0.0F, 360.0F, false, this.mProgressBackgroundPaint);
        this.mProgressBackgroundPaint.setColor(Color.parseColor("#FFe0e0e0"));
        canvas.drawArc(this.mInnerProgressRectF, 0.0F, 360.0F, false, this.mProgressBackgroundPaint);

        this.mProgressPaint.setColor(this.mProgressStartColor);
        float angle = 360.F * (float) this.mProgress / (float) this.mMax;
        if (mProgress>=0){
            canvas.drawArc(this.mProgressRectF, angle, angle,
                    false, this.mProgressPaint);
            mProgressPaint.setColor(Color.parseColor("#44FF0000"));
            canvas.drawArc(this.mInnerProgressRectF, angle, angle,
                    false, this.mProgressPaint);

        }else{
            canvas.drawArc(this.mProgressRectF, - angle, angle,
                    false, this.mProgressPaint);
            mProgressPaint.setColor(Color.parseColor("#44FF0000"));
            canvas.drawArc(this.mInnerProgressRectF, -angle, angle,
                    false, this.mProgressPaint);
        }

        this.mProgressPaint.setColor(this.mProgressStartColor);

    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.mBoundsRectF.left = (float) this.getPaddingLeft();
        this.mBoundsRectF.top = (float) this.getPaddingTop();
        this.mBoundsRectF.right = (float) (w - this.getPaddingRight());
        this.mBoundsRectF.bottom = (float) (h - this.getPaddingBottom());
        this.mCenterX = this.mBoundsRectF.centerX();
        this.mCenterY = this.mBoundsRectF.centerY();
        this.mRadius = Math.min(this.mBoundsRectF.width(), this.mBoundsRectF.height()) / 2.0F;
        this.mProgressRectF.set(this.mBoundsRectF);
        this.updateProgressShader();
        this.mProgressRectF.inset(this.mProgressStrokeWidth / 2.0F, this.mProgressStrokeWidth / 2.0F);
        this.mInnerProgressRectF.set(this.mProgressRectF);
        this.mInnerProgressRectF.inset(this.mProgressStrokeWidth , this.mProgressStrokeWidth);
    }

    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.progress = this.mProgress;
        return ss;
    }

    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        this.setProgress(ss.progress);
    }

    public void setProgressFormatter(ProgressFormatter progressFormatter) {
        this.mProgressFormatter = progressFormatter;
        this.invalidate();
    }

    public void setProgressStrokeWidth(float progressStrokeWidth) {
        this.mProgressStrokeWidth = progressStrokeWidth;
        this.mProgressRectF.set(this.mBoundsRectF);
        this.updateProgressShader();
        this.mProgressRectF.inset(this.mProgressStrokeWidth / 2.0F, this.mProgressStrokeWidth / 2.0F);
        this.mInnerProgressRectF.set(this.mProgressRectF);
        this.mInnerProgressRectF.inset(this.mProgressStrokeWidth, this.mProgressStrokeWidth);
        this.invalidate();
    }

    public void setProgressTextSize(float progressTextSize) {
        this.mProgressTextSize = progressTextSize;
        this.invalidate();
    }

    public void setProgressStartColor(int progressStartColor) {
        this.mProgressStartColor = progressStartColor;
        this.updateProgressShader();
        this.invalidate();
    }

    public void setProgressEndColor(int progressEndColor) {
        this.mProgressEndColor = progressEndColor;
        this.updateProgressShader();
        this.invalidate();
    }

    public void setProgressTextColor(int progressTextColor) {
        this.mProgressTextColor = progressTextColor;
        this.invalidate();
    }

    public void setProgressBackgroundColor(int progressBackgroundColor) {
        this.mProgressBackgroundColor = progressBackgroundColor;
        this.mProgressBackgroundPaint.setColor(this.mProgressBackgroundColor);
        this.invalidate();
    }

    public void setLineCount(int lineCount) {
        this.mLineCount = lineCount;
        this.invalidate();
    }

    public void setLineWidth(float lineWidth) {
        this.mLineWidth = lineWidth;
        this.invalidate();
    }

    public void setStyle(int style) {
        this.mStyle = style;
        this.mProgressPaint.setStyle(this.mStyle == 1 ? Paint.Style.FILL : Paint.Style.STROKE);
        this.mProgressBackgroundPaint.setStyle(this.mStyle == 1 ? Paint.Style.FILL : Paint.Style.STROKE);
        this.invalidate();
    }

    public void setBlurRadius(int blurRadius) {
        this.mBlurRadius = blurRadius;
        this.updateMaskBlurFilter();
        this.invalidate();
    }

    public void setBlurStyle(BlurMaskFilter.Blur blurStyle) {
        this.mBlurStyle = blurStyle;
        this.updateMaskBlurFilter();
        this.invalidate();
    }

    public void setShader(int shader) {
        this.mShader = shader;
        this.updateProgressShader();
        this.invalidate();
    }

    public void setCap(Paint.Cap cap) {
        this.mCap = cap;
        this.mProgressPaint.setStrokeCap(cap);
        this.mProgressBackgroundPaint.setStrokeCap(cap);
        this.invalidate();
    }

    public void setStartDegree(int startDegree) {
        this.mStartDegree = startDegree;
        this.invalidate();
    }

    public void setDrawBackgroundOutsideProgress(boolean drawBackgroundOutsideProgress) {
        this.mDrawBackgroundOutsideProgress = drawBackgroundOutsideProgress;
        this.invalidate();
    }

    public int getProgress() {
        return this.mProgress;
    }

    public void setProgress(int progress) {
        this.mProgress = progress;
        this.invalidate();
    }

    public int getMax() {
        return this.mMax;
    }

    public void setMax(int max) {
        this.mMax = max;
        this.invalidate();
    }

    private static final class SavedState extends View.BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        int progress;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            this.progress = in.readInt();
        }

        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(this.progress);
        }
    }

    private static final class DefaultProgressFormatter implements ProgressFormatter {
        private static final String DEFAULT_PATTERN = "%d%%";

        private DefaultProgressFormatter() {
        }

        public CharSequence format(int progress, int max) {
            return String.format("%d%%", (int) ((float) progress / (float) max * 100.0F));
        }
    }

    public interface ProgressFormatter {
        CharSequence format(int var1, int var2);
    }
}
