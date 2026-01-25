package com.example.debugappproject.debugger;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.debugmaster.app.R;

import java.util.ArrayList;
import java.util.List;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║           DEBUGMASTER - STACK TRACE VISUALIZER                               ║
 * ║              Animated Call Stack Visualization                               ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 * 
 * Visual representation of the call stack with:
 * - Animated stack frames
 * - Color coding for different states
 * - Arrow connections between frames
 * - Current frame highlighting
 */
public class StackTraceView extends View {

    private static final String TAG = "StackTraceView";
    
    // Paints
    private Paint framePaint;
    private Paint frameStrokePaint;
    private Paint textPaint;
    private Paint lineTextPaint;
    private Paint arrowPaint;
    private Paint highlightPaint;
    private Paint errorPaint;
    
    // Dimensions
    private int frameWidth = 200;
    private int frameHeight = 60;
    private int frameSpacing = 20;
    private int padding = 40;
    private int arrowSize = 10;
    
    // Stack data
    private List<StackFrameData> stackFrames = new ArrayList<>();
    private int currentFrameIndex = -1;
    private int errorFrameIndex = -1;
    
    // Animation
    private float animationProgress = 1f;
    
    public static class StackFrameData {
        public String methodName;
        public int lineNumber;
        public String className;
        public boolean isActive;
        public boolean hasError;
        
        public StackFrameData(String methodName, int lineNumber, String className) {
            this.methodName = methodName;
            this.lineNumber = lineNumber;
            this.className = className;
            this.isActive = false;
            this.hasError = false;
        }
    }
    
    public StackTraceView(Context context) {
        super(context);
        init();
    }
    
    public StackTraceView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public StackTraceView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        // Frame background
        framePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        framePaint.setColor(Color.parseColor("#1E1E2E"));
        framePaint.setStyle(Paint.Style.FILL);
        
        // Frame border
        frameStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        frameStrokePaint.setColor(Color.parseColor("#6366F1"));
        frameStrokePaint.setStyle(Paint.Style.STROKE);
        frameStrokePaint.setStrokeWidth(2);
        
        // Method name text
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(32);
        textPaint.setTextAlign(Paint.Align.CENTER);
        
        // Line number text
        lineTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        lineTextPaint.setColor(Color.parseColor("#94A3B8"));
        lineTextPaint.setTextSize(24);
        lineTextPaint.setTextAlign(Paint.Align.CENTER);
        
        // Arrow
        arrowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        arrowPaint.setColor(Color.parseColor("#6366F1"));
        arrowPaint.setStyle(Paint.Style.STROKE);
        arrowPaint.setStrokeWidth(3);
        
        // Highlight (current frame)
        highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        highlightPaint.setColor(Color.parseColor("#22C55E"));
        highlightPaint.setStyle(Paint.Style.STROKE);
        highlightPaint.setStrokeWidth(4);
        
        // Error frame
        errorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        errorPaint.setColor(Color.parseColor("#EF4444"));
        errorPaint.setStyle(Paint.Style.STROKE);
        errorPaint.setStrokeWidth(4);
        
        // Scale based on density
        float density = getResources().getDisplayMetrics().density;
        frameWidth = (int) (180 * density);
        frameHeight = (int) (50 * density);
        frameSpacing = (int) (15 * density);
        padding = (int) (30 * density);
        arrowSize = (int) (8 * density);
        textPaint.setTextSize(14 * density);
        lineTextPaint.setTextSize(11 * density);
    }
    
    /**
     * Set the call stack to display
     */
    public void setCallStack(List<CodeDebugger.StackFrame> stack) {
        stackFrames.clear();
        for (CodeDebugger.StackFrame frame : stack) {
            StackFrameData data = new StackFrameData(
                    frame.methodName,
                    frame.lineNumber,
                    "DebugMaster"
            );
            stackFrames.add(data);
        }
        
        // Mark top frame as active
        if (!stackFrames.isEmpty()) {
            currentFrameIndex = stackFrames.size() - 1;
            stackFrames.get(currentFrameIndex).isActive = true;
        }
        
        animateStackUpdate();
        invalidate();
    }
    
    /**
     * Highlight current frame
     */
    public void setCurrentFrame(int index) {
        // Clear previous active
        for (StackFrameData frame : stackFrames) {
            frame.isActive = false;
        }
        
        if (index >= 0 && index < stackFrames.size()) {
            currentFrameIndex = index;
            stackFrames.get(index).isActive = true;
        }
        
        invalidate();
    }
    
    /**
     * Mark error frame
     */
    public void setErrorFrame(int index) {
        // Clear previous errors
        for (StackFrameData frame : stackFrames) {
            frame.hasError = false;
        }
        
        if (index >= 0 && index < stackFrames.size()) {
            errorFrameIndex = index;
            stackFrames.get(index).hasError = true;
        }
        
        invalidate();
    }
    
    /**
     * Push a new frame
     */
    public void pushFrame(String methodName, int lineNumber) {
        StackFrameData data = new StackFrameData(methodName, lineNumber, "DebugMaster");
        stackFrames.add(data);
        currentFrameIndex = stackFrames.size() - 1;
        
        // Clear previous active
        for (int i = 0; i < stackFrames.size() - 1; i++) {
            stackFrames.get(i).isActive = false;
        }
        data.isActive = true;
        
        animatePush();
        invalidate();
    }
    
    /**
     * Pop the top frame
     */
    public void popFrame() {
        if (!stackFrames.isEmpty()) {
            stackFrames.remove(stackFrames.size() - 1);
            
            if (!stackFrames.isEmpty()) {
                currentFrameIndex = stackFrames.size() - 1;
                stackFrames.get(currentFrameIndex).isActive = true;
            } else {
                currentFrameIndex = -1;
            }
            
            animatePop();
            invalidate();
        }
    }
    
    /**
     * Clear the stack
     */
    public void clear() {
        stackFrames.clear();
        currentFrameIndex = -1;
        errorFrameIndex = -1;
        invalidate();
    }
    
    private void animateStackUpdate() {
        animationProgress = 0f;
        ObjectAnimator animator = ObjectAnimator.ofFloat(this, "animationProgress", 0f, 1f);
        animator.setDuration(300);
        animator.start();
    }
    
    private void animatePush() {
        animationProgress = 0f;
        ObjectAnimator animator = ObjectAnimator.ofFloat(this, "animationProgress", 0f, 1f);
        animator.setDuration(250);
        animator.start();
    }
    
    private void animatePop() {
        // Animate out
        animationProgress = 0f;
        ObjectAnimator animator = ObjectAnimator.ofFloat(this, "animationProgress", 0f, 1f);
        animator.setDuration(200);
        animator.start();
    }
    
    public void setAnimationProgress(float progress) {
        this.animationProgress = progress;
        invalidate();
    }
    
    public float getAnimationProgress() {
        return animationProgress;
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = padding * 2 + stackFrames.size() * (frameHeight + frameSpacing);
        
        if (height < 200) height = 200;
        
        setMeasuredDimension(width, height);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (stackFrames.isEmpty()) {
            // Draw empty state
            textPaint.setColor(Color.parseColor("#64748B"));
            canvas.drawText("Call Stack Empty", getWidth() / 2f, getHeight() / 2f, textPaint);
            textPaint.setColor(Color.WHITE);
            return;
        }
        
        int centerX = getWidth() / 2;
        int startY = padding;
        
        // Draw frames from bottom to top (first = bottom of stack)
        for (int i = 0; i < stackFrames.size(); i++) {
            StackFrameData frame = stackFrames.get(i);
            
            // Calculate position (stack grows upward visually)
            int frameY = getHeight() - padding - (i + 1) * (frameHeight + frameSpacing);
            
            // Apply animation for the latest frame
            float alpha = 1f;
            float scale = 1f;
            if (i == stackFrames.size() - 1) {
                alpha = animationProgress;
                scale = 0.8f + 0.2f * animationProgress;
            }
            
            // Frame background
            RectF frameRect = new RectF(
                    centerX - frameWidth / 2f * scale,
                    frameY + (frameHeight * (1 - scale) / 2),
                    centerX + frameWidth / 2f * scale,
                    frameY + frameHeight - (frameHeight * (1 - scale) / 2)
            );
            
            // Draw shadow
            Paint shadowPaint = new Paint(framePaint);
            shadowPaint.setColor(Color.parseColor("#0D0D0D"));
            shadowPaint.setAlpha((int) (100 * alpha));
            canvas.drawRoundRect(
                    new RectF(frameRect.left + 4, frameRect.top + 4, 
                             frameRect.right + 4, frameRect.bottom + 4),
                    10, 10, shadowPaint);
            
            // Draw frame
            framePaint.setAlpha((int) (255 * alpha));
            canvas.drawRoundRect(frameRect, 10, 10, framePaint);
            
            // Draw border based on state
            Paint borderPaint = frameStrokePaint;
            if (frame.hasError) {
                borderPaint = errorPaint;
            } else if (frame.isActive) {
                borderPaint = highlightPaint;
            }
            borderPaint.setAlpha((int) (255 * alpha));
            canvas.drawRoundRect(frameRect, 10, 10, borderPaint);
            
            // Draw method name
            textPaint.setAlpha((int) (255 * alpha));
            String methodText = frame.methodName + "()";
            if (methodText.length() > 20) {
                methodText = methodText.substring(0, 17) + "...";
            }
            canvas.drawText(methodText, centerX, 
                    frameY + frameHeight / 2f - 5, textPaint);
            
            // Draw line number
            lineTextPaint.setAlpha((int) (255 * alpha));
            String lineText = "Line " + frame.lineNumber;
            canvas.drawText(lineText, centerX, 
                    frameY + frameHeight / 2f + 20, lineTextPaint);
            
            // Draw arrow to next frame
            if (i < stackFrames.size() - 1) {
                int arrowY = frameY - frameSpacing / 2;
                drawArrow(canvas, centerX, arrowY + 10, centerX, arrowY - 10, alpha);
            }
            
            // Draw "calls" label
            if (i < stackFrames.size() - 1) {
                lineTextPaint.setAlpha((int) (150 * alpha));
                canvas.drawText("↓ calls", centerX + frameWidth / 2f + 30, 
                        frameY + frameHeight + frameSpacing / 2f, lineTextPaint);
            }
        }
        
        // Draw stack labels
        if (!stackFrames.isEmpty()) {
            lineTextPaint.setAlpha(180);
            
            // "Top" label at current frame
            int topY = getHeight() - padding - stackFrames.size() * (frameHeight + frameSpacing);
            canvas.drawText("TOP ▶", 
                    centerX - frameWidth / 2f - 50, 
                    topY + frameHeight / 2f + 5, lineTextPaint);
            
            // "Bottom" label at first frame
            int bottomY = getHeight() - padding - (frameHeight + frameSpacing);
            canvas.drawText("BOTTOM", 
                    centerX - frameWidth / 2f - 50, 
                    bottomY + frameHeight / 2f + 5, lineTextPaint);
        }
    }
    
    private void drawArrow(Canvas canvas, float startX, float startY, 
                           float endX, float endY, float alpha) {
        arrowPaint.setAlpha((int) (255 * alpha));
        
        // Draw line
        canvas.drawLine(startX, startY, endX, endY, arrowPaint);
        
        // Draw arrowhead
        float angle = (float) Math.atan2(endY - startY, endX - startX);
        
        Path arrowHead = new Path();
        arrowHead.moveTo(endX, endY);
        arrowHead.lineTo(
                endX - arrowSize * (float) Math.cos(angle - Math.PI / 6),
                endY - arrowSize * (float) Math.sin(angle - Math.PI / 6)
        );
        arrowHead.moveTo(endX, endY);
        arrowHead.lineTo(
                endX - arrowSize * (float) Math.cos(angle + Math.PI / 6),
                endY - arrowSize * (float) Math.sin(angle + Math.PI / 6)
        );
        
        canvas.drawPath(arrowHead, arrowPaint);
    }
}
