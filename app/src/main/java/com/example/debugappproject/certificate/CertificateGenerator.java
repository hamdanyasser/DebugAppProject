package com.example.debugappproject.certificate;

import android.content.ContentValues;
import android.content.Context;
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
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║           DEBUGMASTER - CERTIFICATE GENERATOR                                ║
 * ║              Professional Completion Certificates                            ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class CertificateGenerator {

    private static final int WIDTH = 1920;
    private static final int HEIGHT = 1080;
    
    public enum CertificateType {
        PATH_COMPLETION("Learning Path Completion"),
        BUG_MASTER("Bug Master Achievement"),
        STREAK_CHAMPION("Streak Champion"),
        BATTLE_WINNER("Battle Arena Champion"),
        SPEED_DEMON("Speed Run Record"),
        DEBUGGING_EXPERT("Debugging Expert");
        
        public final String title;
        CertificateType(String title) { this.title = title; }
    }
    
    public static class CertificateData {
        public String userName;
        public CertificateType type;
        public String achievementTitle;
        public String description;
        public int bugsFixed;
        public int totalXP;
        public String pathName;
        public Date completionDate;
        
        public CertificateData(String userName, CertificateType type) {
            this.userName = userName;
            this.type = type;
            this.completionDate = new Date();
        }
    }
    
    /**
     * Generate a professional certificate bitmap
     */
    public static Bitmap generateCertificate(CertificateData data) {
        Bitmap bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        
        // Background gradient
        drawBackground(canvas);
        
        // Border design
        drawBorder(canvas);
        
        // Logo/Icon
        drawLogo(canvas);
        
        // Title
        drawTitle(canvas, data.type.title);
        
        // Certificate text
        drawCertificateText(canvas, data);
        
        // User name (large, prominent)
        drawUserName(canvas, data.userName);
        
        // Achievement details
        drawAchievementDetails(canvas, data);
        
        // Date and signature
        drawFooter(canvas, data);
        
        // Decorative elements
        drawDecorations(canvas);
        
        return bitmap;
    }
    
    private static void drawBackground(Canvas canvas) {
        // Dark gradient background
        Paint bgPaint = new Paint();
        LinearGradient gradient = new LinearGradient(
                0, 0, WIDTH, HEIGHT,
                new int[]{Color.parseColor("#0F0F1A"), Color.parseColor("#1A1A2E"), Color.parseColor("#16213E")},
                null, Shader.TileMode.CLAMP
        );
        bgPaint.setShader(gradient);
        canvas.drawRect(0, 0, WIDTH, HEIGHT, bgPaint);
    }
    
    private static void drawBorder(Canvas canvas) {
        Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(8);
        
        // Outer border - gold gradient
        LinearGradient goldGradient = new LinearGradient(
                0, 0, WIDTH, 0,
                new int[]{Color.parseColor("#D4AF37"), Color.parseColor("#FFD700"), Color.parseColor("#D4AF37")},
                null, Shader.TileMode.CLAMP
        );
        borderPaint.setShader(goldGradient);
        canvas.drawRoundRect(new RectF(30, 30, WIDTH - 30, HEIGHT - 30), 20, 20, borderPaint);
        
        // Inner border
        borderPaint.setStrokeWidth(3);
        borderPaint.setShader(null);
        borderPaint.setColor(Color.parseColor("#6366F1"));
        canvas.drawRoundRect(new RectF(50, 50, WIDTH - 50, HEIGHT - 50), 15, 15, borderPaint);
        
        // Corner decorations
        drawCornerDecorations(canvas);
    }
    
    private static void drawCornerDecorations(Canvas canvas) {
        Paint cornerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cornerPaint.setColor(Color.parseColor("#FFD700"));
        cornerPaint.setStyle(Paint.Style.FILL);
        
        // Top-left
        drawCornerFlourish(canvas, 60, 60, false, false);
        // Top-right
        drawCornerFlourish(canvas, WIDTH - 60, 60, true, false);
        // Bottom-left
        drawCornerFlourish(canvas, 60, HEIGHT - 60, false, true);
        // Bottom-right
        drawCornerFlourish(canvas, WIDTH - 60, HEIGHT - 60, true, true);
    }
    
    private static void drawCornerFlourish(Canvas canvas, float x, float y, boolean flipX, boolean flipY) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.parseColor("#D4AF37"));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        
        Path path = new Path();
        float scaleX = flipX ? -1 : 1;
        float scaleY = flipY ? -1 : 1;
        
        path.moveTo(x, y);
        path.lineTo(x + 40 * scaleX, y);
        path.moveTo(x, y);
        path.lineTo(x, y + 40 * scaleY);
        
        // Decorative curl
        path.moveTo(x + 15 * scaleX, y + 15 * scaleY);
        path.quadTo(x + 25 * scaleX, y + 5 * scaleY, x + 35 * scaleX, y + 15 * scaleY);
        
        canvas.drawPath(path, paint);
    }
    
    private static void drawLogo(Canvas canvas) {
        Paint logoPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        logoPaint.setTextSize(60);
        logoPaint.setColor(Color.parseColor("#6366F1"));
        logoPaint.setTextAlign(Paint.Align.CENTER);
        logoPaint.setTypeface(Typeface.DEFAULT_BOLD);
        
        canvas.drawText("🔍 DEBUGMASTER", WIDTH / 2f, 120, logoPaint);
        
        // Subtitle
        logoPaint.setTextSize(24);
        logoPaint.setColor(Color.parseColor("#94A3B8"));
        canvas.drawText("Learn Debugging. Master Code.", WIDTH / 2f, 155, logoPaint);
    }
    
    private static void drawTitle(Canvas canvas, String title) {
        Paint titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        titlePaint.setTextSize(72);
        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.BOLD));
        
        // Gold gradient text
        LinearGradient goldText = new LinearGradient(
                WIDTH / 2f - 300, 0, WIDTH / 2f + 300, 0,
                Color.parseColor("#FFD700"), Color.parseColor("#D4AF37"),
                Shader.TileMode.CLAMP
        );
        titlePaint.setShader(goldText);
        
        canvas.drawText("CERTIFICATE", WIDTH / 2f, 240, titlePaint);
        
        // Subtitle
        titlePaint.setShader(null);
        titlePaint.setTextSize(36);
        titlePaint.setColor(Color.WHITE);
        canvas.drawText("of " + title, WIDTH / 2f, 290, titlePaint);
    }
    
    private static void drawCertificateText(Canvas canvas, CertificateData data) {
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(28);
        textPaint.setColor(Color.parseColor("#E2E8F0"));
        textPaint.setTextAlign(Paint.Align.CENTER);
        
        canvas.drawText("This is to certify that", WIDTH / 2f, 380, textPaint);
    }
    
    private static void drawUserName(Canvas canvas, String userName) {
        Paint namePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        namePaint.setTextSize(80);
        namePaint.setColor(Color.WHITE);
        namePaint.setTextAlign(Paint.Align.CENTER);
        namePaint.setTypeface(Typeface.create("cursive", Typeface.BOLD_ITALIC));
        
        canvas.drawText(userName, WIDTH / 2f, 480, namePaint);
        
        // Underline
        Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(Color.parseColor("#6366F1"));
        linePaint.setStrokeWidth(2);
        
        float nameWidth = namePaint.measureText(userName);
        float startX = (WIDTH - nameWidth) / 2 - 20;
        float endX = (WIDTH + nameWidth) / 2 + 20;
        canvas.drawLine(startX, 500, endX, 500, linePaint);
    }
    
    private static void drawAchievementDetails(Canvas canvas, CertificateData data) {
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(28);
        textPaint.setColor(Color.parseColor("#E2E8F0"));
        textPaint.setTextAlign(Paint.Align.CENTER);
        
        String line1 = "has successfully demonstrated exceptional debugging skills";
        String line2 = "";
        String line3 = "";
        
        switch (data.type) {
            case PATH_COMPLETION:
                line2 = "by completing the \"" + (data.pathName != null ? data.pathName : "Learning Path") + "\"";
                line3 = "with " + data.bugsFixed + " bugs fixed and " + data.totalXP + " XP earned";
                break;
            case BUG_MASTER:
                line2 = "achieving the prestigious Bug Master status";
                line3 = "Total bugs squashed: " + data.bugsFixed;
                break;
            case BATTLE_WINNER:
                line2 = "emerging victorious in the Battle Arena";
                line3 = "proving superior debugging speed and accuracy";
                break;
            case STREAK_CHAMPION:
                line2 = "maintaining an incredible learning streak";
                line3 = "demonstrating dedication and consistency";
                break;
            case SPEED_DEMON:
                line2 = "setting a new speed record";
                line3 = "completing challenges with lightning speed";
                break;
            case DEBUGGING_EXPERT:
                line2 = "mastering the art of debugging";
                line3 = "across multiple programming languages";
                break;
        }
        
        canvas.drawText(line1, WIDTH / 2f, 570, textPaint);
        canvas.drawText(line2, WIDTH / 2f, 610, textPaint);
        
        textPaint.setColor(Color.parseColor("#22C55E"));
        canvas.drawText(line3, WIDTH / 2f, 660, textPaint);
    }
    
    private static void drawFooter(Canvas canvas, CertificateData data) {
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(24);
        textPaint.setColor(Color.parseColor("#94A3B8"));
        textPaint.setTextAlign(Paint.Align.CENTER);
        
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy", Locale.US);
        String dateStr = sdf.format(data.completionDate);
        
        canvas.drawText("Awarded on " + dateStr, WIDTH / 2f, 800, textPaint);
        
        // Signature line
        Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(Color.parseColor("#64748B"));
        linePaint.setStrokeWidth(1);
        canvas.drawLine(WIDTH / 2f - 150, 900, WIDTH / 2f + 150, 900, linePaint);
        
        textPaint.setTextSize(20);
        canvas.drawText("DebugMaster Team", WIDTH / 2f, 930, textPaint);
        
        // Certificate ID
        textPaint.setTextSize(16);
        textPaint.setColor(Color.parseColor("#475569"));
        String certId = "CERT-" + System.currentTimeMillis() % 100000;
        canvas.drawText("Certificate ID: " + certId, WIDTH / 2f, 1000, textPaint);
        
        // Verification URL
        canvas.drawText("Verify at: debugmaster.app/verify/" + certId, WIDTH / 2f, 1030, textPaint);
    }
    
    private static void drawDecorations(Canvas canvas) {
        Paint starPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        starPaint.setTextSize(40);
        starPaint.setColor(Color.parseColor("#FFD700"));
        starPaint.setTextAlign(Paint.Align.CENTER);
        
        // Stars
        canvas.drawText("⭐", 150, 500, starPaint);
        canvas.drawText("⭐", WIDTH - 150, 500, starPaint);
        
        // Achievement badges
        starPaint.setTextSize(60);
        canvas.drawText("🏆", WIDTH / 2f, 750, starPaint);
    }
    
    /**
     * Save certificate to device storage
     */
    public static Uri saveCertificate(Context context, Bitmap bitmap, String fileName) throws Exception {
        OutputStream outputStream;
        Uri uri;
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/DebugMaster");
            
            uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (uri == null) throw new Exception("Failed to create file");
            outputStream = context.getContentResolver().openOutputStream(uri);
        } else {
            File dir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES), "DebugMaster");
            if (!dir.exists()) dir.mkdirs();
            
            File file = new File(dir, fileName);
            outputStream = new FileOutputStream(file);
            uri = Uri.fromFile(file);
        }
        
        if (outputStream != null) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.close();
        }
        
        return uri;
    }
}
