package com.example.debugappproject.ai;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Environment;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║           DEBUGMASTER - CERTIFICATE GENERATOR                                ║
 * ║         Beautiful PDF/Image Certificates for Achievements                    ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 *
 * Features:
 * - Learning path completion certificates
 * - Achievement certificates
 * - Battle arena champion certificates
 * - Shareable to social media
 * - Professional design
 */
public class CertificateGenerator {

    private static final String TAG = "CertificateGenerator";
    
    // Certificate dimensions (A4 landscape at 150 DPI)
    private static final int WIDTH = 1754;
    private static final int HEIGHT = 1240;
    
    // Colors
    private static final int COLOR_GOLD = Color.parseColor("#FFD700");
    private static final int COLOR_GOLD_DARK = Color.parseColor("#DAA520");
    private static final int COLOR_PURPLE = Color.parseColor("#8B5CF6");
    private static final int COLOR_PURPLE_DARK = Color.parseColor("#6D28D9");
    private static final int COLOR_BACKGROUND = Color.parseColor("#0F0F23");
    private static final int COLOR_TEXT = Color.WHITE;
    private static final int COLOR_TEXT_SECONDARY = Color.parseColor("#B0BEC5");
    
    // Certificate types
    public enum CertificateType {
        PATH_COMPLETION,
        ACHIEVEMENT,
        BATTLE_CHAMPION,
        BUG_HUNTER,
        SPEED_DEMON,
        PERFECT_SCORE
    }
    
    private final Context context;
    
    public CertificateGenerator(Context context) {
        this.context = context;
    }
    
    /**
     * Generate a learning path completion certificate
     */
    public Bitmap generatePathCertificate(String userName, String pathName, int bugsFixed, int totalXP) {
        Bitmap bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        
        // Draw background
        drawBackground(canvas);
        
        // Draw border
        drawBorder(canvas, COLOR_PURPLE, COLOR_PURPLE_DARK);
        
        // Draw decorations
        drawCornerDecorations(canvas);
        
        // Draw logo/badge
        drawBadge(canvas, "🎓");
        
        // Draw title
        Paint titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        titlePaint.setColor(COLOR_GOLD);
        titlePaint.setTextSize(72);
        titlePaint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.BOLD));
        titlePaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("CERTIFICATE OF COMPLETION", WIDTH / 2f, 280, titlePaint);
        
        // Draw subtitle
        Paint subtitlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        subtitlePaint.setColor(COLOR_TEXT_SECONDARY);
        subtitlePaint.setTextSize(36);
        subtitlePaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("This is to certify that", WIDTH / 2f, 380, subtitlePaint);
        
        // Draw name
        Paint namePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        namePaint.setColor(COLOR_TEXT);
        namePaint.setTextSize(80);
        namePaint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.BOLD_ITALIC));
        namePaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(userName, WIDTH / 2f, 500, namePaint);
        
        // Draw underline for name
        Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(COLOR_GOLD);
        linePaint.setStrokeWidth(3);
        float nameWidth = namePaint.measureText(userName);
        canvas.drawLine(WIDTH / 2f - nameWidth / 2, 520, WIDTH / 2f + nameWidth / 2, 520, linePaint);
        
        // Draw achievement text
        Paint achievePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        achievePaint.setColor(COLOR_TEXT_SECONDARY);
        achievePaint.setTextSize(36);
        achievePaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("has successfully completed the", WIDTH / 2f, 600, achievePaint);
        
        // Draw path name
        Paint pathPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pathPaint.setColor(COLOR_PURPLE);
        pathPaint.setTextSize(56);
        pathPaint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.BOLD));
        pathPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(pathName, WIDTH / 2f, 680, pathPaint);
        
        // Draw learning path label
        canvas.drawText("Learning Path", WIDTH / 2f, 740, achievePaint);
        
        // Draw stats
        Paint statsPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        statsPaint.setColor(COLOR_TEXT);
        statsPaint.setTextSize(32);
        statsPaint.setTextAlign(Paint.Align.CENTER);
        
        String statsText = String.format("🐛 %d Bugs Fixed  •  ⭐ %d XP Earned", bugsFixed, totalXP);
        canvas.drawText(statsText, WIDTH / 2f, 820, statsPaint);
        
        // Draw date
        String date = new SimpleDateFormat("MMMM dd, yyyy", Locale.US).format(new Date());
        Paint datePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        datePaint.setColor(COLOR_TEXT_SECONDARY);
        datePaint.setTextSize(28);
        datePaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Issued on " + date, WIDTH / 2f, 900, datePaint);
        
        // Draw certificate ID
        String certId = "CERT-" + System.currentTimeMillis() % 100000;
        canvas.drawText("Certificate ID: " + certId, WIDTH / 2f, 940, datePaint);
        
        // Draw signature area
        drawSignatureArea(canvas);
        
        // Draw DebugMaster branding
        drawBranding(canvas);
        
        return bitmap;
    }
    
    /**
     * Generate an achievement certificate
     */
    public Bitmap generateAchievementCertificate(String userName, String achievementName, 
                                                   String achievementDesc, String emoji) {
        Bitmap bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        
        drawBackground(canvas);
        drawBorder(canvas, COLOR_GOLD, COLOR_GOLD_DARK);
        drawCornerDecorations(canvas);
        drawBadge(canvas, emoji);
        
        // Title
        Paint titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        titlePaint.setColor(COLOR_GOLD);
        titlePaint.setTextSize(68);
        titlePaint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.BOLD));
        titlePaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("ACHIEVEMENT UNLOCKED", WIDTH / 2f, 280, titlePaint);
        
        // Subtitle
        Paint subtitlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        subtitlePaint.setColor(COLOR_TEXT_SECONDARY);
        subtitlePaint.setTextSize(36);
        subtitlePaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Presented to", WIDTH / 2f, 380, subtitlePaint);
        
        // Name
        Paint namePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        namePaint.setColor(COLOR_TEXT);
        namePaint.setTextSize(80);
        namePaint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.BOLD_ITALIC));
        namePaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(userName, WIDTH / 2f, 500, namePaint);
        
        // Achievement name
        Paint achievePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        achievePaint.setColor(COLOR_PURPLE);
        achievePaint.setTextSize(52);
        achievePaint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.BOLD));
        achievePaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("\"" + achievementName + "\"", WIDTH / 2f, 640, achievePaint);
        
        // Description
        Paint descPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        descPaint.setColor(COLOR_TEXT_SECONDARY);
        descPaint.setTextSize(32);
        descPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(achievementDesc, WIDTH / 2f, 720, descPaint);
        
        // Date
        String date = new SimpleDateFormat("MMMM dd, yyyy", Locale.US).format(new Date());
        Paint datePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        datePaint.setColor(COLOR_TEXT_SECONDARY);
        datePaint.setTextSize(28);
        datePaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Achieved on " + date, WIDTH / 2f, 820, datePaint);
        
        drawSignatureArea(canvas);
        drawBranding(canvas);
        
        return bitmap;
    }
    
    /**
     * Generate a battle champion certificate
     */
    public Bitmap generateBattleCertificate(String userName, int wins, int trophies, String rank) {
        Bitmap bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        
        drawBackground(canvas);
        drawBorder(canvas, Color.parseColor("#FF6B6B"), Color.parseColor("#C0392B"));
        drawCornerDecorations(canvas);
        drawBadge(canvas, "⚔️");
        
        // Title
        Paint titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        titlePaint.setColor(COLOR_GOLD);
        titlePaint.setTextSize(68);
        titlePaint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.BOLD));
        titlePaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("BATTLE ARENA CHAMPION", WIDTH / 2f, 280, titlePaint);
        
        // Subtitle
        Paint subtitlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        subtitlePaint.setColor(COLOR_TEXT_SECONDARY);
        subtitlePaint.setTextSize(36);
        subtitlePaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("This honor is bestowed upon", WIDTH / 2f, 380, subtitlePaint);
        
        // Name
        Paint namePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        namePaint.setColor(COLOR_TEXT);
        namePaint.setTextSize(80);
        namePaint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.BOLD_ITALIC));
        namePaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(userName, WIDTH / 2f, 500, namePaint);
        
        // Rank
        Paint rankPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rankPaint.setColor(getRankColor(rank));
        rankPaint.setTextSize(60);
        rankPaint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.BOLD));
        rankPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(rank + " League Champion", WIDTH / 2f, 620, rankPaint);
        
        // Stats
        Paint statsPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        statsPaint.setColor(COLOR_TEXT);
        statsPaint.setTextSize(36);
        statsPaint.setTextAlign(Paint.Align.CENTER);
        
        String statsText = String.format("🏆 %d Victories  •  🎖️ %d Trophies", wins, trophies);
        canvas.drawText(statsText, WIDTH / 2f, 720, statsPaint);
        
        // Date
        String date = new SimpleDateFormat("MMMM dd, yyyy", Locale.US).format(new Date());
        Paint datePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        datePaint.setColor(COLOR_TEXT_SECONDARY);
        datePaint.setTextSize(28);
        datePaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Season " + getCurrentSeason() + " • " + date, WIDTH / 2f, 820, datePaint);
        
        drawSignatureArea(canvas);
        drawBranding(canvas);
        
        return bitmap;
    }
    
    /**
     * Draw certificate background
     */
    private void drawBackground(Canvas canvas) {
        // Gradient background
        Paint bgPaint = new Paint();
        LinearGradient gradient = new LinearGradient(
                0, 0, WIDTH, HEIGHT,
                Color.parseColor("#0F0F23"),
                Color.parseColor("#1A1A2E"),
                Shader.TileMode.CLAMP
        );
        bgPaint.setShader(gradient);
        canvas.drawRect(0, 0, WIDTH, HEIGHT, bgPaint);
        
        // Subtle pattern
        Paint patternPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        patternPaint.setColor(Color.parseColor("#20FFFFFF"));
        patternPaint.setStrokeWidth(1);
        
        // Draw diagonal lines
        for (int i = -HEIGHT; i < WIDTH + HEIGHT; i += 40) {
            canvas.drawLine(i, 0, i + HEIGHT, HEIGHT, patternPaint);
        }
    }
    
    /**
     * Draw decorative border
     */
    private void drawBorder(Canvas canvas, int color1, int color2) {
        Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(8);
        
        LinearGradient borderGradient = new LinearGradient(
                0, 0, WIDTH, HEIGHT, color1, color2, Shader.TileMode.CLAMP);
        borderPaint.setShader(borderGradient);
        
        // Outer border
        RectF outerRect = new RectF(30, 30, WIDTH - 30, HEIGHT - 30);
        canvas.drawRoundRect(outerRect, 20, 20, borderPaint);
        
        // Inner border
        borderPaint.setStrokeWidth(3);
        RectF innerRect = new RectF(50, 50, WIDTH - 50, HEIGHT - 50);
        canvas.drawRoundRect(innerRect, 15, 15, borderPaint);
    }
    
    /**
     * Draw corner decorations
     */
    private void drawCornerDecorations(Canvas canvas) {
        Paint decorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        decorPaint.setColor(COLOR_GOLD);
        decorPaint.setStyle(Paint.Style.FILL);
        
        // Top left corner decoration
        Path topLeft = new Path();
        topLeft.moveTo(60, 60);
        topLeft.lineTo(130, 60);
        topLeft.lineTo(60, 130);
        topLeft.close();
        canvas.drawPath(topLeft, decorPaint);
        
        // Top right
        Path topRight = new Path();
        topRight.moveTo(WIDTH - 60, 60);
        topRight.lineTo(WIDTH - 130, 60);
        topRight.lineTo(WIDTH - 60, 130);
        topRight.close();
        canvas.drawPath(topRight, decorPaint);
        
        // Bottom left
        Path bottomLeft = new Path();
        bottomLeft.moveTo(60, HEIGHT - 60);
        bottomLeft.lineTo(130, HEIGHT - 60);
        bottomLeft.lineTo(60, HEIGHT - 130);
        bottomLeft.close();
        canvas.drawPath(bottomLeft, decorPaint);
        
        // Bottom right
        Path bottomRight = new Path();
        bottomRight.moveTo(WIDTH - 60, HEIGHT - 60);
        bottomRight.lineTo(WIDTH - 130, HEIGHT - 60);
        bottomRight.lineTo(WIDTH - 60, HEIGHT - 130);
        bottomRight.close();
        canvas.drawPath(bottomRight, decorPaint);
    }
    
    /**
     * Draw central badge with emoji
     */
    private void drawBadge(Canvas canvas, String emoji) {
        Paint circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setColor(COLOR_PURPLE);
        circlePaint.setStyle(Paint.Style.FILL);
        
        // Draw circle background
        canvas.drawCircle(WIDTH / 2f, 150, 60, circlePaint);
        
        // Draw glow
        Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glowPaint.setColor(COLOR_PURPLE);
        glowPaint.setStyle(Paint.Style.STROKE);
        glowPaint.setStrokeWidth(4);
        glowPaint.setAlpha(100);
        canvas.drawCircle(WIDTH / 2f, 150, 70, glowPaint);
        
        // Draw emoji (as text, since we can't render actual emojis perfectly)
        Paint emojiPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        emojiPaint.setTextSize(60);
        emojiPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(emoji, WIDTH / 2f, 170, emojiPaint);
    }
    
    /**
     * Draw signature area
     */
    private void drawSignatureArea(Canvas canvas) {
        Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(COLOR_TEXT_SECONDARY);
        linePaint.setStrokeWidth(2);
        
        // Left signature line
        canvas.drawLine(300, HEIGHT - 180, 550, HEIGHT - 180, linePaint);
        
        // Right signature line
        canvas.drawLine(WIDTH - 550, HEIGHT - 180, WIDTH - 300, HEIGHT - 180, linePaint);
        
        // Labels
        Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelPaint.setColor(COLOR_TEXT_SECONDARY);
        labelPaint.setTextSize(22);
        labelPaint.setTextAlign(Paint.Align.CENTER);
        
        canvas.drawText("Course Director", 425, HEIGHT - 150, labelPaint);
        canvas.drawText("DebugMaster Team", WIDTH - 425, HEIGHT - 150, labelPaint);
        
        // Signature text
        Paint sigPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        sigPaint.setColor(COLOR_TEXT);
        sigPaint.setTextSize(28);
        sigPaint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.ITALIC));
        sigPaint.setTextAlign(Paint.Align.CENTER);
        
        canvas.drawText("Prof. Debug", 425, HEIGHT - 195, sigPaint);
        canvas.drawText("Byte & Team", WIDTH - 425, HEIGHT - 195, sigPaint);
    }
    
    /**
     * Draw DebugMaster branding
     */
    private void drawBranding(Canvas canvas) {
        Paint brandPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        brandPaint.setColor(COLOR_TEXT_SECONDARY);
        brandPaint.setTextSize(24);
        brandPaint.setTextAlign(Paint.Align.CENTER);
        brandPaint.setAlpha(150);
        
        canvas.drawText("🐛 DebugMaster - Learn by Fixing Real Code", WIDTH / 2f, HEIGHT - 80, brandPaint);
        canvas.drawText("debugmaster.app", WIDTH / 2f, HEIGHT - 50, brandPaint);
    }
    
    /**
     * Get color for rank
     */
    private int getRankColor(String rank) {
        switch (rank.toLowerCase()) {
            case "diamond": return Color.parseColor("#B9F2FF");
            case "platinum": return Color.parseColor("#E5E4E2");
            case "gold": return COLOR_GOLD;
            case "silver": return Color.parseColor("#C0C0C0");
            default: return Color.parseColor("#CD7F32"); // Bronze
        }
    }
    
    /**
     * Get current season
     */
    private String getCurrentSeason() {
        int month = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH);
        int year = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
        int quarter = (month / 3) + 1;
        return year + " Q" + quarter;
    }
    
    /**
     * Save certificate to file and return URI
     */
    public Uri saveCertificate(Bitmap bitmap, String fileName) {
        try {
            File dir = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "certificates");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            File file = new File(dir, fileName + ".png");
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
            
            return FileProvider.getUriForFile(context, 
                    context.getPackageName() + ".fileprovider", file);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Share certificate
     */
    public void shareCertificate(Bitmap bitmap, String title) {
        Uri uri = saveCertificate(bitmap, "certificate_" + System.currentTimeMillis());
        
        if (uri != null) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/png");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.putExtra(Intent.EXTRA_TEXT, 
                    "🎉 I just earned a certificate in DebugMaster! " + title + " #DebugMaster #CodingAchievement");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            context.startActivity(Intent.createChooser(shareIntent, "Share Certificate")
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
    }
}
