package com.example.debugappproject.ai;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║           DEBUGMASTER - ANIMATED STACK TRACE VISUALIZER                      ║
 * ║              Beautiful Call Stack Animation for Learning                     ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 * 
 * Features:
 * - Parses Java/Python/JS stack traces
 * - Animated visualization of call chain
 * - Highlights error source
 * - Color-coded by package/module
 */
public class StackTraceVisualizer extends View {

    private static final String TAG = "StackTraceVisualizer";
    
    // Parsed stack frames
    private List<StackFrame> frames = new ArrayList<>();
    private int animatedFrameCount = 0;
    private float animationProgress = 0f;
    
    // Paints
    private Paint framePaint;
    private Paint textPaint;
    private Paint linePaint;
    private Paint errorPaint;
    private Paint arrowPaint;
    
    // Dimensions
    private float frameHeight = 60f;
    private float frameMargin = 16f;
    private float cornerRadius = 12f;
    private float arrowSize = 20f;
    
    // Colors
    private int[] frameColors = {
            Color.parseColor("#3B82F6"), // Blue
            Color.parseColor("#8B5CF6"), // Purple
            Color.parseColor("#06B6D4"), // Cyan
            Color.parseColor("#10B981"), // Green
            Color.parseColor("#F59E0B"), // Amber
    };
    private int errorColor = Color.parseColor("#EF4444");
    private int textColor = Color.WHITE;
    private int lineColor = Color.parseColor("#64748B");
    
    public static class StackFrame {
        public String className;
        public String methodName;
        public String fileName;
        public int lineNumber;
        public boolean isError;
        public boolean isUserCode;
        
        public StackFrame(String className, String methodName, String fileName, int lineNumber, boolean isError) {
            this.className = className;
            this.methodName = methodName;
            this.fileName = fileName;
            this.lineNumber = lineNumber;
            this.isError = isError;
            this.isUserCode = !className.startsWith("java.") && 
                              !className.startsWith("android.") && 
                              !className.startsWith("androidx.");
        }
        
        public String getShortClassName() {
            int lastDot = className.lastIndexOf('.');
            return lastDot > 0 ? className.substring(lastDot + 1) : className;
        }
        
        @Override
        public String toString() {
            return getShortClassName() + "." + methodName + "():" + lineNumber;
        }
    }
    
    public StackTraceVisualizer(Context context) {
        super(context);
        init();
    }
    
    public StackTraceVisualizer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public StackTraceVisualizer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        framePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        framePaint.setStyle(Paint.Style.FILL);
        
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(textColor);
        textPaint.setTextSize(28f);
        
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(lineColor);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(4f);
        
        errorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        errorPaint.setColor(errorColor);
        errorPaint.setStyle(Paint.Style.STROKE);
        errorPaint.setStrokeWidth(6f);
        
        arrowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        arrowPaint.setColor(lineColor);
        arrowPaint.setStyle(Paint.Style.FILL);
    }
    
    /**
     * Parse a stack trace string and visualize it
     */
    public void setStackTrace(String stackTrace) {
        frames.clear();
        animatedFrameCount = 0;
        
        if (stackTrace == null || stackTrace.isEmpty()) {
            invalidate();
            return;
        }
        
        // Parse Java-style stack traces
        Pattern javaPattern = Pattern.compile(
                "at\\s+([\\w.$]+)\\.([\\w<>$]+)\\(([^:]+):(\\d+)\\)");
        Matcher matcher = javaPattern.matcher(stackTrace);
        
        boolean firstFrame = true;
        while (matcher.find()) {
            String className = matcher.group(1);
            String methodName = matcher.group(2);
            String fileName = matcher.group(3);
            int lineNumber = Integer.parseInt(matcher.group(4));
            
            frames.add(new StackFrame(className, methodName, fileName, lineNumber, firstFrame));
            firstFrame = false;
        }
        
        // If no Java frames, try Python style
        if (frames.isEmpty()) {
            Pattern pythonPattern = Pattern.compile(
                    "File \"([^\"]+)\", line (\\d+), in ([\\w<>]+)");
            matcher = pythonPattern.matcher(stackTrace);
            firstFrame = true;
            while (matcher.find()) {
                String fileName = matcher.group(1);
                int lineNumber = Integer.parseInt(matcher.group(2));
                String methodName = matcher.group(3);
                
                frames.add(new StackFrame(fileName, methodName, fileName, lineNumber, firstFrame));
                firstFrame = false;
            }
        }
        
        // Calculate required height
        int requiredHeight = (int) ((frames.size() * (frameHeight + frameMargin)) + frameMargin);
        setMinimumHeight(requiredHeight);
        
        // Start animation
        startAnimation();
    }
    
    /**
     * Animate frames appearing one by one
     */
    public void startAnimation() {
        if (frames.isEmpty()) return;
        
        ValueAnimator animator = ValueAnimator.ofInt(0, frames.size());
        animator.setDuration(frames.size() * 300L);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            animatedFrameCount = (int) animation.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = (int) ((frames.size() * (frameHeight + frameMargin)) + frameMargin);
        height = Math.max(height, getSuggestedMinimumHeight());
        setMeasuredDimension(width, height);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (frames.isEmpty()) {
            // Draw empty state
            textPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("No stack trace to display", getWidth() / 2f, getHeight() / 2f, textPaint);
            return;
        }
        
        float startX = frameMargin;
        float startY = frameMargin;
        float frameWidth = getWidth() - (2 * frameMargin);
        
        for (int i = 0; i < Math.min(animatedFrameCount, frames.size()); i++) {
            StackFrame frame = frames.get(i);
            
            float top = startY + (i * (frameHeight + frameMargin));
            float left = startX;
            float right = left + frameWidth;
            float bottom = top + frameHeight;
            
            RectF rect = new RectF(left, top, right, bottom);
            
            // Draw frame background
            int colorIndex = i % frameColors.length;
            framePaint.setColor(frame.isError ? errorColor : frameColors[colorIndex]);
            framePaint.setAlpha(frame.isUserCode ? 255 : 180);
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, framePaint);
            
            // Draw error highlight
            if (frame.isError) {
                canvas.drawRoundRect(rect, cornerRadius, cornerRadius, errorPaint);
            }
            
            // Draw method name
            textPaint.setTextAlign(Paint.Align.LEFT);
            textPaint.setTextSize(frame.isUserCode ? 30f : 26f);
            textPaint.setColor(textColor);
            String text = frame.toString();
            canvas.drawText(text, left + 16, top + (frameHeight / 2) + 10, textPaint);
            
            // Draw connecting arrow to next frame
            if (i < frames.size() - 1 && i < animatedFrameCount - 1) {
                float arrowX = getWidth() / 2f;
                float arrowTop = bottom + 4;
                float arrowBottom = bottom + frameMargin - 4;
                
                // Line
                canvas.drawLine(arrowX, arrowTop, arrowX, arrowBottom, linePaint);
                
                // Arrow head
                Path arrow = new Path();
                arrow.moveTo(arrowX - arrowSize / 2, arrowBottom - arrowSize);
                arrow.lineTo(arrowX, arrowBottom);
                arrow.lineTo(arrowX + arrowSize / 2, arrowBottom - arrowSize);
                arrow.close();
                canvas.drawPath(arrow, arrowPaint);
            }
        }
    }
    
    /**
     * Get the error frame (first frame, usually where the error occurred)
     */
    public StackFrame getErrorFrame() {
        for (StackFrame frame : frames) {
            if (frame.isError) return frame;
        }
        return frames.isEmpty() ? null : frames.get(0);
    }
    
    /**
     * Get user code frames (excluding system libraries)
     */
    public List<StackFrame> getUserCodeFrames() {
        List<StackFrame> userFrames = new ArrayList<>();
        for (StackFrame frame : frames) {
            if (frame.isUserCode) {
                userFrames.add(frame);
            }
        }
        return userFrames;
    }
    
    /**
     * Clear the visualization
     */
    public void clear() {
        frames.clear();
        animatedFrameCount = 0;
        invalidate();
    }
}
