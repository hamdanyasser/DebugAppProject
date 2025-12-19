package com.example.debugappproject.certificate;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.debugappproject.R;
import com.example.debugappproject.util.AuthManager;
import com.example.debugappproject.util.SoundManager;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Certificate Preview and Share Fragment
 */
public class CertificateFragment extends Fragment {

    private static final String ARG_TYPE = "certificate_type";
    private static final String ARG_BUGS_FIXED = "bugs_fixed";
    private static final String ARG_XP = "xp";
    private static final String ARG_PATH_NAME = "path_name";
    
    private ImageView imageCertificate;
    private MaterialButton btnDownload, btnShare;
    private SoundManager soundManager;
    private Bitmap certificateBitmap;
    
    public static CertificateFragment newInstance(CertificateGenerator.CertificateType type,
                                                   int bugsFixed, int xp, String pathName) {
        CertificateFragment fragment = new CertificateFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, type.name());
        args.putInt(ARG_BUGS_FIXED, bugsFixed);
        args.putInt(ARG_XP, xp);
        args.putString(ARG_PATH_NAME, pathName);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_certificate, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        soundManager = SoundManager.getInstance(requireContext());
        
        imageCertificate = view.findViewById(R.id.image_certificate);
        btnDownload = view.findViewById(R.id.btn_download);
        btnShare = view.findViewById(R.id.btn_share);
        
        View backBtn = view.findViewById(R.id.button_back);
        if (backBtn != null) {
            backBtn.setOnClickListener(v -> Navigation.findNavController(requireView()).navigateUp());
        }
        
        generateCertificate();
        setupButtons();
        
        soundManager.playSound(SoundManager.Sound.ACHIEVEMENT_UNLOCK);
    }
    
    private void generateCertificate() {
        Bundle args = getArguments();
        if (args == null) return;
        
        CertificateGenerator.CertificateType type = CertificateGenerator.CertificateType.valueOf(
                args.getString(ARG_TYPE, CertificateGenerator.CertificateType.PATH_COMPLETION.name())
        );
        
        AuthManager auth = AuthManager.getInstance(requireContext());
        String userName = auth.getDisplayName();
        if (userName == null || userName.isEmpty()) userName = "Debug Champion";
        
        CertificateGenerator.CertificateData data = new CertificateGenerator.CertificateData(userName, type);
        data.bugsFixed = args.getInt(ARG_BUGS_FIXED, 0);
        data.totalXP = args.getInt(ARG_XP, 0);
        data.pathName = args.getString(ARG_PATH_NAME, "Debugging Fundamentals");
        
        certificateBitmap = CertificateGenerator.generateCertificate(data);
        imageCertificate.setImageBitmap(certificateBitmap);
    }
    
    private void setupButtons() {
        btnDownload.setOnClickListener(v -> {
            soundManager.playButtonClick();
            downloadCertificate();
        });
        
        btnShare.setOnClickListener(v -> {
            soundManager.playButtonClick();
            shareCertificate();
        });
    }
    
    private void downloadCertificate() {
        if (certificateBitmap == null) return;
        
        try {
            String fileName = "DebugMaster_Certificate_" + 
                    new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date()) + ".png";
            
            Uri uri = CertificateGenerator.saveCertificate(requireContext(), certificateBitmap, fileName);
            Toast.makeText(getContext(), "Certificate saved to Pictures/DebugMaster", Toast.LENGTH_LONG).show();
            soundManager.playSound(SoundManager.Sound.COIN_COLLECT);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Failed to save: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void shareCertificate() {
        if (certificateBitmap == null) return;
        
        try {
            String fileName = "DebugMaster_Certificate.png";
            Uri uri = CertificateGenerator.saveCertificate(requireContext(), certificateBitmap, fileName);
            
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/png");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.putExtra(Intent.EXTRA_TEXT, 
                    "I earned a certificate from DebugMaster! 🏆🔍 #DebugMaster #Coding #Achievement");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            startActivity(Intent.createChooser(shareIntent, "Share Certificate"));
        } catch (Exception e) {
            Toast.makeText(getContext(), "Failed to share: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
