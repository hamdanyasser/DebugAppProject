package com.example.debugappproject.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Environment;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║              DEBUGMASTER - SHARE RESULT MANAGER                              ║
 * ║           Generate & Share Beautiful Result Images 📸                        ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 *
 * Features:
 * - Generate shareable images with bug challenge info
 * - Include user's fix and XP earned
 * - Add app watermark/branding
 * - Share to social media
 */
public class ShareResultManager {

    private static final int IMAGE_WIDTH = 1080;
    private static final int IMAGE_HEIGHT = 1920;
    private static final int PADDING = 60;

    private final Context context;

    public ShareResultManager(Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * Generate a shareable result image.
     */
    public Bitmap generateResultImage(ShareData data) {
        Bitmap bitmap = Bitmap.createBitmap(IMAGE_WIDTH, IMAGE_HEIGHT, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // Draw gradient background
        drawBackground(canvas);

        // Draw content
        int y = PADDING + 100;

        // App logo/title
        y = drawAppTitle(canvas, y);

        // Challenge title
        y = drawChallengeTitle(canvas, y, data.bugTitle);

        // Category & Difficulty badges
        y = drawBadges(canvas, y, data.category, data.difficulty);

        // Bug description
        y = drawDescription(canvas, y, data.description);

        // The fix (code snippet)
        y = drawCodeSnippet(canvas, y, "Your Fix:", data.userFix);

        // XP earned
        y = drawXpSection(canvas, y, data.xpEarned, data.isWeekendBonus, data.comboMultiplier);

        // Stats bar
        y = drawStatsBar(canvas, y, data.streak, data.totalSolved, data.level);

        // Watermark
        drawWatermark(canvas);

        return bitmap;
    }

    private void drawBackground(Canvas canvas) {
        Paint paint = new Paint();
        LinearGradient gradient = new LinearGradient(
            0, 0, IMAGE_WIDTH, IMAGE_HEIGHT,
            new int[]{
                Color.parseColor("#0F172A"),
                Color.parseColor("#1E293B"),
                Color.parseColor("#0F172A")
            },
            null,
            Shader.TileMode.CLAMP
        );
        paint.setShader(gradient);
        canvas.drawRect(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT, paint);

        // Add subtle pattern
        paint.setShader(null);
        paint.setColor(Color.parseColor("#10FFFFFF"));
        for (int i = 0; i < IMAGE_HEIGHT; i += 30) {
            canvas.drawLine(0, i, IMAGE_WIDTH, i, paint);
        }
    }

    private int drawAppTitle(Canvas canvas, int y) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.parseColor("#3B82F6"));
        paint.setTextSize(72);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setTextAlign(Paint.Align.CENTER);

        canvas.drawText("🐛 DebugMaster", IMAGE_WIDTH / 2f, y, paint);

        // Subtitle
        paint.setColor(Color.parseColor("#94A3B8"));
        paint.setTextSize(36);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        canvas.drawText("I just crushed a bug!", IMAGE_WIDTH / 2f, y + 60, paint);

        return y + 140;
    }

    private int drawChallengeTitle(Canvas canvas, int y, String title) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.WHITE);
        paint.setTextSize(48);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setTextAlign(Paint.Align.CENTER);

        // Wrap text if needed
        String displayTitle = title.length() > 35 ? title.substring(0, 32) + "..." : title;
        canvas.drawText(displayTitle, IMAGE_WIDTH / 2f, y, paint);

        return y + 80;
    }

    private int drawBadges(Canvas canvas, int y, String category, String difficulty) {
        Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(32);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        textPaint.setTextAlign(Paint.Align.CENTER);

        // Category badge
        bgPaint.setColor(Color.parseColor("#3B82F6"));
        float catWidth = textPaint.measureText(category) + 40;
        float catX = IMAGE_WIDTH / 2f - catWidth / 2 - 80;
        canvas.drawRoundRect(new RectF(catX, y - 40, catX + catWidth, y + 10), 20, 20, bgPaint);
        textPaint.setColor(Color.WHITE);
        canvas.drawText(category, catX + catWidth / 2, y - 5, textPaint);

        // Difficulty badge
        int diffColor = getDifficultyColor(difficulty);
        bgPaint.setColor(diffColor);
        float diffWidth = textPaint.measureText(difficulty) + 40;
        float diffX = IMAGE_WIDTH / 2f + 80 - diffWidth / 2;
        canvas.drawRoundRect(new RectF(diffX, y - 40, diffX + diffWidth, y + 10), 20, 20, bgPaint);
        canvas.drawText(difficulty, diffX + diffWidth / 2, y - 5, textPaint);

        return y + 60;
    }

    private int getDifficultyColor(String difficulty) {
        if (difficulty == null) return Color.parseColor("#22C55E");
        switch (difficulty.toLowerCase()) {
            case "hard": return Color.parseColor("#EF4444");
            case "medium": return Color.parseColor("#F59E0B");
            case "expert": return Color.parseColor("#8B5CF6");
            default: return Color.parseColor("#22C55E");
        }
    }

    private int drawDescription(Canvas canvas, int y, String description) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.parseColor("#CBD5E1"));
        paint.setTextSize(32);
        paint.setTextAlign(Paint.Align.CENTER);

        // Truncate and wrap description
        String desc = description.length() > 100 ? description.substring(0, 97) + "..." : description;
        canvas.drawText(desc, IMAGE_WIDTH / 2f, y + 30, paint);

        return y + 100;
    }

    private int drawCodeSnippet(Canvas canvas, int y, String label, String code) {
        Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelPaint.setColor(Color.parseColor("#22C55E"));
        labelPaint.setTextSize(36);
        labelPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText(label, PADDING, y, labelPaint);

        y += 50;

        // Code background
        Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(Color.parseColor("#1E1E2E"));
        int codeHeight = Math.min(code.split("\n").length * 40 + 40, 400);
        canvas.drawRoundRect(new RectF(PADDING, y, IMAGE_WIDTH - PADDING, y + codeHeight), 20, 20, bgPaint);

        // Border
        Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setColor(Color.parseColor("#3B82F6"));
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(4);
        canvas.drawRoundRect(new RectF(PADDING, y, IMAGE_WIDTH - PADDING, y + codeHeight), 20, 20, borderPaint);

        // Code text
        Paint codePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        codePaint.setColor(Color.parseColor("#A6E3A1"));
        codePaint.setTextSize(28);
        codePaint.setTypeface(Typeface.MONOSPACE);

        String[] lines = code.split("\n");
        int lineY = y + 50;
        for (int i = 0; i < Math.min(lines.length, 8); i++) {
            String line = lines[i].length() > 45 ? lines[i].substring(0, 42) + "..." : lines[i];
            canvas.drawText(line, PADDING + 30, lineY, codePaint);
            lineY += 40;
        }

        return y + codeHeight + 40;
    }

    private int drawXpSection(Canvas canvas, int y, int xpEarned, boolean isWeekend, int combo) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        // XP earned box
        Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        LinearGradient gradient = new LinearGradient(
            PADDING, y, IMAGE_WIDTH - PADDING, y + 120,
            new int[]{
                Color.parseColor("#10B981"),
                Color.parseColor("#059669")
            },
            null,
            Shader.TileMode.CLAMP
        );
        bgPaint.setShader(gradient);
        canvas.drawRoundRect(new RectF(PADDING, y, IMAGE_WIDTH - PADDING, y + 120), 20, 20, bgPaint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(64);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("+" + xpEarned + " XP", IMAGE_WIDTH / 2f, y + 75, paint);

        // Bonus indicators
        y += 140;
        paint.setTextSize(32);

        if (isWeekend) {
            paint.setColor(Color.parseColor("#FBBF24"));
            canvas.drawText("🎉 WEEKEND 2X BONUS!", IMAGE_WIDTH / 2f, y, paint);
            y += 50;
        }

        if (combo > 1) {
            paint.setColor(Color.parseColor("#F472B6"));
            canvas.drawText("🔥 " + combo + "x COMBO!", IMAGE_WIDTH / 2f, y, paint);
            y += 50;
        }

        return y + 30;
    }

    private int drawStatsBar(Canvas canvas, int y, int streak, int totalSolved, int level) {
        Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(Color.parseColor("#1E293B"));
        canvas.drawRoundRect(new RectF(PADDING, y, IMAGE_WIDTH - PADDING, y + 100), 20, 20, bgPaint);

        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(36);
        textPaint.setTextAlign(Paint.Align.CENTER);

        int sectionWidth = (IMAGE_WIDTH - PADDING * 2) / 3;

        // Streak
        canvas.drawText("🔥 " + streak, PADDING + sectionWidth / 2f, y + 55, textPaint);

        // Bugs solved
        canvas.drawText("🐛 " + totalSolved, PADDING + sectionWidth * 1.5f, y + 55, textPaint);

        // Level
        canvas.drawText("⭐ Lv." + level, PADDING + sectionWidth * 2.5f, y + 55, textPaint);

        // Labels
        textPaint.setTextSize(24);
        textPaint.setColor(Color.parseColor("#94A3B8"));
        canvas.drawText("Streak", PADDING + sectionWidth / 2f, y + 85, textPaint);
        canvas.drawText("Bugs Fixed", PADDING + sectionWidth * 1.5f, y + 85, textPaint);
        canvas.drawText("Level", PADDING + sectionWidth * 2.5f, y + 85, textPaint);

        return y + 130;
    }

    private void drawWatermark(Canvas canvas) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.parseColor("#64748B"));
        paint.setTextSize(28);
        paint.setTextAlign(Paint.Align.CENTER);

        String date = new SimpleDateFormat("MMM dd, yyyy", Locale.US).format(new Date());
        canvas.drawText("debugmaster.app • " + date, IMAGE_WIDTH / 2f, IMAGE_HEIGHT - PADDING, paint);

        paint.setTextSize(24);
        canvas.drawText("Can you beat my score? 🎮", IMAGE_WIDTH / 2f, IMAGE_HEIGHT - PADDING - 40, paint);
    }

    /**
     * Save bitmap to file and return Uri for sharing.
     */
    public Uri saveAndGetUri(Bitmap bitmap) throws IOException {
        File imagesDir = new File(context.getCacheDir(), "share_images");
        if (!imagesDir.exists()) {
            imagesDir.mkdirs();
        }

        String fileName = "debugmaster_result_" + System.currentTimeMillis() + ".png";
        File imageFile = new File(imagesDir, fileName);

        try (FileOutputStream fos = new FileOutputStream(imageFile)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        }

        return FileProvider.getUriForFile(
            context,
            context.getPackageName() + ".fileprovider",
            imageFile
        );
    }

    /**
     * Share the result image.
     */
    public void shareResult(ShareData data) {
        try {
            Bitmap bitmap = generateResultImage(data);
            Uri imageUri = saveAndGetUri(bitmap);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/png");
            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
            shareIntent.putExtra(Intent.EXTRA_TEXT,
                "🐛 I just fixed a bug in DebugMaster!\n\n" +
                "Challenge: " + data.bugTitle + "\n" +
                "XP Earned: +" + data.xpEarned + "\n\n" +
                "Can you beat my score? 🎮\n" +
                "#DebugMaster #CodingChallenge"
            );
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            Intent chooser = Intent.createChooser(shareIntent, "Share your result!");
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(chooser);

        } catch (Exception e) {
            android.util.Log.e("ShareResultManager", "Failed to share result", e);
        }
    }

    /**
     * Data class for share content.
     */
    public static class ShareData {
        public String bugTitle = "";
        public String category = "";
        public String difficulty = "";
        public String description = "";
        public String userFix = "";
        public int xpEarned = 0;
        public int streak = 0;
        public int totalSolved = 0;
        public int level = 1;
        public int comboMultiplier = 1;
        public boolean isWeekendBonus = false;

        public ShareData() {}

        public ShareData setBugTitle(String title) { this.bugTitle = title; return this; }
        public ShareData setCategory(String cat) { this.category = cat; return this; }
        public ShareData setDifficulty(String diff) { this.difficulty = diff; return this; }
        public ShareData setDescription(String desc) { this.description = desc; return this; }
        public ShareData setUserFix(String fix) { this.userFix = fix; return this; }
        public ShareData setXpEarned(int xp) { this.xpEarned = xp; return this; }
        public ShareData setStreak(int s) { this.streak = s; return this; }
        public ShareData setTotalSolved(int t) { this.totalSolved = t; return this; }
        public ShareData setLevel(int l) { this.level = l; return this; }
        public ShareData setComboMultiplier(int c) { this.comboMultiplier = c; return this; }
        public ShareData setWeekendBonus(boolean b) { this.isWeekendBonus = b; return this; }
    }
}
