package com.example.debugappproject.ui.animation;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * ConfettiAnimationView - Creates a confetti celebration animation.
 *
 * Features:
 * - Particle-based confetti system
 * - Multiple colors (Material 3 palette)
 * - Physics-based falling animation
 * - Rotation and drift effects
 * - Configurable particle count and duration
 *
 * Usage:
 * <pre>
 *     confettiView.startAnimation();
 * </pre>
 */
public class ConfettiAnimationView extends View {

    private static final int DEFAULT_PARTICLE_COUNT = 150;
    private static final long DEFAULT_DURATION = 3000; // 3 seconds

    private final List<ConfettiParticle> particles = new ArrayList<>();
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Random random = new Random();

    private ValueAnimator animator;
    private int particleCount = DEFAULT_PARTICLE_COUNT;
    private long duration = DEFAULT_DURATION;

    // Material 3 confetti colors
    private final int[] colors = {
        Color.parseColor("#FF6B6B"), // Red
        Color.parseColor("#4ECDC4"), // Cyan
        Color.parseColor("#FFE66D"), // Yellow
        Color.parseColor("#95E1D3"), // Mint
        Color.parseColor("#F38181"), // Pink
        Color.parseColor("#AA96DA"), // Purple
        Color.parseColor("#FCBAD3"), // Rose
        Color.parseColor("#A8D8EA")  // Sky Blue
    };

    public ConfettiAnimationView(Context context) {
        super(context);
        init();
    }

    public ConfettiAnimationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ConfettiAnimationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setVisibility(GONE);
    }

    /**
     * Starts the confetti animation.
     */
    public void startAnimation() {
        setVisibility(VISIBLE);
        createParticles();
        startAnimator();
    }

    /**
     * Sets the number of confetti particles.
     * @param count Number of particles (50-300 recommended)
     */
    public void setParticleCount(int count) {
        this.particleCount = Math.max(50, Math.min(300, count));
    }

    /**
     * Sets the animation duration.
     * @param durationMs Duration in milliseconds
     */
    public void setDuration(long durationMs) {
        this.duration = durationMs;
    }

    private void createParticles() {
        particles.clear();

        int width = getWidth();
        int height = getHeight();

        for (int i = 0; i < particleCount; i++) {
            ConfettiParticle particle = new ConfettiParticle();

            // Start from top, spread across width
            particle.x = random.nextFloat() * width;
            particle.y = -random.nextFloat() * 100; // Start above screen

            // Random size (4-12 dp)
            particle.size = 4 + random.nextFloat() * 8;

            // Random color
            particle.color = colors[random.nextInt(colors.length)];

            // Random velocities
            particle.velocityX = (random.nextFloat() - 0.5f) * 2; // Horizontal drift
            particle.velocityY = 2 + random.nextFloat() * 3; // Fall speed

            // Random rotation
            particle.rotation = random.nextFloat() * 360;
            particle.rotationSpeed = (random.nextFloat() - 0.5f) * 10;

            // Random shape (circle or rectangle)
            particle.isCircle = random.nextBoolean();

            particles.add(particle);
        }
    }

    private void startAnimator() {
        if (animator != null &amp;&amp; animator.isRunning()) {
            animator.cancel();
        }

        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(duration);
        animator.setInterpolator(new AccelerateInterpolator(0.5f));

        animator.addUpdateListener(animation -> {
            updateParticles();
            invalidate();
        });

        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                // Animation started
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                setVisibility(GONE);
                particles.clear();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                setVisibility(GONE);
                particles.clear();
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                // Not used
            }
        });

        animator.start();
    }

    private void updateParticles() {
        for (ConfettiParticle particle : particles) {
            // Update position
            particle.x += particle.velocityX;
            particle.y += particle.velocityY;

            // Update rotation
            particle.rotation += particle.rotationSpeed;

            // Apply gravity (acceleration)
            particle.velocityY += 0.1f;

            // Add some horizontal drift variation
            particle.velocityX += (random.nextFloat() - 0.5f) * 0.1f;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (ConfettiParticle particle : particles) {
            paint.setColor(particle.color);

            canvas.save();
            canvas.translate(particle.x, particle.y);
            canvas.rotate(particle.rotation);

            if (particle.isCircle) {
                canvas.drawCircle(0, 0, particle.size, paint);
            } else {
                float halfSize = particle.size / 2;
                canvas.drawRect(-halfSize, -halfSize * 2, halfSize, halfSize * 2, paint);
            }

            canvas.restore();
        }
    }

    /**
     * Stops the animation immediately.
     */
    public void stopAnimation() {
        if (animator != null &amp;&amp; animator.isRunning()) {
            animator.cancel();
        }
        setVisibility(GONE);
        particles.clear();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAnimation();
    }

    /**
     * Represents a single confetti particle.
     */
    private static class ConfettiParticle {
        float x, y;
        float velocityX, velocityY;
        float size;
        int color;
        float rotation;
        float rotationSpeed;
        boolean isCircle;
    }
}
