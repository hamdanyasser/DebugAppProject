package com.example.debugappproject.ui.battle;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.debugappproject.R;
import com.example.debugappproject.billing.BillingManager;
import com.example.debugappproject.databinding.FragmentBattleArenaBinding;
import com.example.debugappproject.util.AnimationUtil;
import com.google.android.material.snackbar.Snackbar;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * BattleArenaFragment - Premium PvP debugging battles.
 * Challenge friends or random opponents in timed debugging challenges.
 * 
 * Features:
 * - Quick Match: Find random opponents
 * - Challenge Friend: Battle with friends
 * - Create Room: Private battle rooms
 * - Join Room: Join via room code
 * - Battle History: Track wins/losses
 */
@AndroidEntryPoint
public class BattleArenaFragment extends Fragment {

    private static final String TAG = "BattleArenaFragment";
    private FragmentBattleArenaBinding binding;
    private BillingManager billingManager;
    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean isMatchmaking = false;

    // Sample stats (would come from server in production)
    private int wins = 47;
    private int losses = 12;
    private int trophies = 1250;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentBattleArenaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        billingManager = new BillingManager(requireContext());
        
        setupUI();
        loadStats();
        observeProStatus();
    }

    private void setupUI() {
        // Back button
        if (binding.buttonBack != null) {
            binding.buttonBack.setOnClickListener(v -> {
                if (isMatchmaking) {
                    cancelMatchmaking();
                } else {
                    Navigation.findNavController(v).navigateUp();
                }
            });
        }

        // Quick Match button
        if (binding.buttonQuickMatch != null) {
            binding.buttonQuickMatch.setOnClickListener(v -> {
                AnimationUtil.animatePress(v, this::startMatchmaking);
            });
        }

        // Challenge Friend button
        if (binding.buttonChallengeFriend != null) {
            binding.buttonChallengeFriend.setOnClickListener(v -> {
                AnimationUtil.animatePress(v, this::showFriendsList);
            });
        }

        // Create Room button
        if (binding.buttonCreateRoom != null) {
            binding.buttonCreateRoom.setOnClickListener(v -> {
                AnimationUtil.animatePress(v, this::createBattleRoom);
            });
        }

        // Join Room button
        if (binding.buttonJoinRoom != null) {
            binding.buttonJoinRoom.setOnClickListener(v -> {
                AnimationUtil.animatePress(v, this::showJoinRoomDialog);
            });
        }

        // Cancel matchmaking button
        if (binding.buttonCancelMatchmaking != null) {
            binding.buttonCancelMatchmaking.setOnClickListener(v -> {
                cancelMatchmaking();
            });
        }

        // View all battles
        if (binding.textViewAllBattles != null) {
            binding.textViewAllBattles.setOnClickListener(v -> {
                Toast.makeText(requireContext(), "Battle history coming soon!", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void loadStats() {
        // Update UI with stats
        if (binding.textWins != null) {
            binding.textWins.setText(String.valueOf(wins));
        }
        if (binding.textLosses != null) {
            binding.textLosses.setText(String.valueOf(losses));
        }
        if (binding.textWinRate != null) {
            int total = wins + losses;
            int winRate = total > 0 ? (wins * 100) / total : 0;
            binding.textWinRate.setText(winRate + "%");
        }
        if (binding.textTrophies != null) {
            binding.textTrophies.setText(String.format("%,d", trophies));
        }
    }

    private void observeProStatus() {
        billingManager.getIsProUser().observe(getViewLifecycleOwner(), isPro -> {
            // Battle Arena could be Pro-only feature
            // For now, it's available to all users with some restrictions
        });
    }

    private void startMatchmaking() {
        isMatchmaking = true;
        
        // Show matchmaking overlay with animation
        if (binding.layoutMatchmaking != null) {
            binding.layoutMatchmaking.setVisibility(View.VISIBLE);
            AnimationUtil.fadeIn(binding.layoutMatchmaking);
        }
        if (binding.layoutMainMenu != null) {
            AnimationUtil.fadeOut(binding.layoutMainMenu);
        }
        
        // Simulate matchmaking process
        simulateMatchmaking();
    }

    private void simulateMatchmaking() {
        String[] statusMessages = {
            "Searching for players at your level...",
            "Found 3 potential matches...",
            "Checking connection quality...",
            "Almost there...",
            "Opponent found!"
        };

        for (int i = 0; i < statusMessages.length; i++) {
            final int index = i;
            handler.postDelayed(() -> {
                if (isMatchmaking && binding != null && binding.textMatchmakingStatus != null) {
                    binding.textMatchmakingStatus.setText(statusMessages[index]);
                    
                    // On the last message, show found opponent
                    if (index == statusMessages.length - 1) {
                        handler.postDelayed(() -> {
                            if (isMatchmaking) {
                                showOpponentFound();
                            }
                        }, 1000);
                    }
                }
            }, (i + 1) * 1500L);
        }
    }

    private void showOpponentFound() {
        if (getContext() == null || !isMatchmaking) return;

        // Generate random opponent
        String[] opponents = {"CodeNinja42", "BugSlayer99", "DebugQueen", "JavaMaster", "ByteHunter"};
        String opponent = opponents[(int) (Math.random() * opponents.length)];
        int opponentLevel = 10 + (int) (Math.random() * 25);

        new AlertDialog.Builder(requireContext())
            .setTitle("⚔️ Opponent Found!")
            .setMessage("Ready to battle?\n\n" +
                "👤 " + opponent + "\n" +
                "📊 Level " + opponentLevel + "\n" +
                "🏆 Win Rate: " + (60 + (int)(Math.random() * 30)) + "%\n\n" +
                "Bug Type: Memory Leak\n" +
                "Time Limit: 5 minutes")
            .setPositiveButton("Start Battle!", (dialog, which) -> {
                startBattle(opponent);
            })
            .setNegativeButton("Cancel", (dialog, which) -> {
                cancelMatchmaking();
            })
            .setCancelable(false)
            .show();
    }

    private void startBattle(String opponent) {
        cancelMatchmaking();
        
        // In production, this would navigate to a live battle screen
        Toast.makeText(requireContext(), 
            "⚔️ Battle starting vs " + opponent + "!", 
            Toast.LENGTH_LONG).show();
        
        // Simulate a quick battle result for demo
        handler.postDelayed(() -> {
            if (getContext() != null) {
                boolean won = Math.random() > 0.4; // 60% chance to win
                showBattleResult(won, opponent);
            }
        }, 2000);
    }

    private void showBattleResult(boolean won, String opponent) {
        if (getContext() == null) return;

        int trophyChange = won ? 35 + (int)(Math.random() * 20) : -(15 + (int)(Math.random() * 10));
        int xpEarned = won ? 75 + (int)(Math.random() * 50) : 25;

        String title = won ? "🎉 Victory!" : "😢 Defeat";
        String message = won ? 
            "You defeated " + opponent + "!\n\n" +
            "🏆 +" + trophyChange + " Trophies\n" +
            "⭐ +" + xpEarned + " XP" :
            "You lost to " + opponent + ".\n\n" +
            "🏆 " + trophyChange + " Trophies\n" +
            "⭐ +" + xpEarned + " XP (participation)";

        // Update stats
        if (won) {
            wins++;
            trophies += trophyChange;
        } else {
            losses++;
            trophies += trophyChange; // trophyChange is negative for loss
        }
        loadStats();

        new AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Play Again", (dialog, which) -> {
                startMatchmaking();
            })
            .setNegativeButton("Done", null)
            .show();
    }

    private void cancelMatchmaking() {
        isMatchmaking = false;
        handler.removeCallbacksAndMessages(null);
        
        // Hide matchmaking overlay
        if (binding.layoutMatchmaking != null) {
            AnimationUtil.fadeOut(binding.layoutMatchmaking);
            handler.postDelayed(() -> {
                if (binding != null && binding.layoutMatchmaking != null) {
                    binding.layoutMatchmaking.setVisibility(View.GONE);
                }
            }, 300);
        }
        if (binding.layoutMainMenu != null) {
            binding.layoutMainMenu.setVisibility(View.VISIBLE);
            AnimationUtil.fadeIn(binding.layoutMainMenu);
        }
    }

    private void showFriendsList() {
        if (getContext() == null) return;

        // Sample friends list
        String[] friends = {"Alex_Dev", "Sam_Coder", "Jordan_JS", "Casey_Python", "Morgan_Swift"};
        
        new AlertDialog.Builder(requireContext())
            .setTitle("👥 Challenge a Friend")
            .setItems(friends, (dialog, which) -> {
                String friend = friends[which];
                Toast.makeText(requireContext(), 
                    "📤 Challenge sent to " + friend + "!", 
                    Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void createBattleRoom() {
        // Create a private room with a code
        String roomCode = generateRoomCode();
        
        if (getContext() == null) return;

        new AlertDialog.Builder(requireContext())
            .setTitle("🏟️ Room Created!")
            .setMessage("Share this code with your friend:\n\n" +
                "📋 " + roomCode + "\n\n" +
                "Waiting for opponent to join...")
            .setPositiveButton("Copy Code", (dialog, which) -> {
                // Copy to clipboard
                android.content.ClipboardManager clipboard = 
                    (android.content.ClipboardManager) requireContext()
                        .getSystemService(android.content.Context.CLIPBOARD_SERVICE);
                android.content.ClipData clip = 
                    android.content.ClipData.newPlainText("Room Code", roomCode);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(requireContext(), "Code copied!", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Cancel Room", null)
            .show();
    }

    private void showJoinRoomDialog() {
        if (getContext() == null) return;

        EditText input = new EditText(requireContext());
        input.setHint("Enter room code");
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT | 
                          android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        input.setFilters(new android.text.InputFilter[] { 
            new android.text.InputFilter.LengthFilter(6),
            new android.text.InputFilter.AllCaps()
        });

        int padding = (int) (20 * getResources().getDisplayMetrics().density);
        input.setPadding(padding, padding, padding, padding);

        new AlertDialog.Builder(requireContext())
            .setTitle("🔑 Join Room")
            .setMessage("Enter the 6-character room code:")
            .setView(input)
            .setPositiveButton("Join", (dialog, which) -> {
                String code = input.getText().toString().trim().toUpperCase();
                if (code.length() == 6) {
                    joinRoom(code);
                } else {
                    Toast.makeText(requireContext(), 
                        "Invalid code. Must be 6 characters.", 
                        Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void joinRoom(String code) {
        // Simulate joining a room
        Toast.makeText(requireContext(), 
            "Joining room " + code + "...", 
            Toast.LENGTH_SHORT).show();
        
        handler.postDelayed(() -> {
            if (getContext() != null) {
                // 50% chance to find the room (for demo)
                if (Math.random() > 0.5) {
                    Toast.makeText(requireContext(), 
                        "✅ Joined room! Battle starting...", 
                        Toast.LENGTH_LONG).show();
                    startBattle("RoomHost");
                } else {
                    Toast.makeText(requireContext(), 
                        "❌ Room not found or expired.", 
                        Toast.LENGTH_SHORT).show();
                }
            }
        }, 1500);
    }

    private String generateRoomCode() {
        // Generate a 6-character room code
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < 6; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        return code.toString();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacksAndMessages(null);
        if (billingManager != null) {
            billingManager.destroy();
        }
        binding = null;
    }
}
