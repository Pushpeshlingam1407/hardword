package com.hardword.service;

import com.hardword.model.GameSession;
import com.hardword.model.GuessResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameService {
    private final Map<String, GameSession> sessions = new ConcurrentHashMap<>();
    private final Set<String> validDictionaryWords = new HashSet<>();
    private final List<String> targetDictionary = new ArrayList<>();
    private final Random random = new Random();

    @PostConstruct
    public void loadDictionary() {
        // 1. Load TARGET Dictionary from the Top 10,000 common English words
        try {
            ClassPathResource targetResource = new ClassPathResource("target-words.txt");
            if (targetResource.exists()) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(targetResource.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String word = line.trim().toUpperCase();
                        // Target words must be exactly 4 letters, and all 4 letters must be distinct!
                        if (word.length() == 4) {
                            long distinctLetters = word.chars().distinct().count();
                            if (distinctLetters == 4) {
                                targetDictionary.add(word);
                            }
                        }
                    }
                }
                System.out.println("Loaded " + targetDictionary.size() + " common words for TARGET dictionary.");
            } else {
                System.out.println("Warning: target-words.txt not found. Using fallback.");
                targetDictionary.addAll(List.of("WORD", "GAME", "PLAY", "CODE", "USER", "BIRD", "FISH"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            targetDictionary.addAll(List.of("WORD", "GAME", "PLAY", "CODE", "USER", "BIRD", "FISH"));
        }

        // 2. Load the HUGE english dictionary for GUESS VALIDATION (allow any valid 4
        // letter word)
        try {
            ClassPathResource resource = new ClassPathResource("words.txt");
            if (resource.exists()) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String word = line.trim().toUpperCase();
                        if (word.length() == 4) {
                            validDictionaryWords.add(word);
                        }
                    }
                }
                System.out.println("Loaded " + validDictionaryWords.size() + " total valid 4-letter words.");
            } else {
                System.out.println("Warning: words.txt not found. Using TARGET dictionary as fallback.");
                validDictionaryWords.addAll(targetDictionary);
            }
        } catch (Exception e) {
            e.printStackTrace();
            validDictionaryWords.addAll(targetDictionary);
        }
    }

    public String startNewSession() {
        String sessionId = UUID.randomUUID().toString();
        String targetWord = targetDictionary.get(random.nextInt(targetDictionary.size()));
        sessions.put(sessionId, new GameSession(targetWord));
        return sessionId;
    }

    public GuessResponse processGuess(String sessionId, String guess) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("Invalid session ID");
        }

        if (guess == null || guess.length() != 4) {
            throw new IllegalArgumentException("Guess must be exactly 4 letters");
        }

        guess = guess.toUpperCase();

        // 1. Check for distinct letters (no duplicates allowed in guess)
        long distinctLetters = guess.chars().distinct().count();
        if (distinctLetters < 4) {
            throw new IllegalArgumentException("The 4 letters must be distinct");
        }

        // 4. Check if the word is in the HUGE dictionary (don't waste a try if it's not
        // a real word!)
        if (!validDictionaryWords.contains(guess)) {
            throw new IllegalArgumentException("Word not in dictionary");
        }

        // Now we can count this as a valid attempt
        session.setAttempts(session.getAttempts() + 1);
        String target = session.getTargetWord();

        int green = 0;
        int yellow = 0;
        boolean[] targetUsed = new boolean[4];
        boolean[] guessUsed = new boolean[4];

        // First pass: find greens
        for (int i = 0; i < 4; i++) {
            if (guess.charAt(i) == target.charAt(i)) {
                green++;
                targetUsed[i] = true;
                guessUsed[i] = true;
            }
        }

        // Second pass: find yellows
        for (int i = 0; i < 4; i++) {
            if (!guessUsed[i]) {
                for (int j = 0; j < 4; j++) {
                    if (!targetUsed[j] && guess.charAt(i) == target.charAt(j)) {
                        yellow++;
                        targetUsed[j] = true;
                        break;
                    }
                }
            }
        }

        String state = "PLAYING";
        if (green == 4) {
            state = "WON";
        } else if (session.getAttempts() >= 8) {
            state = "LOST";
        }

        // 3. Display the final answer
        String message = "";
        if (state.equals("WON"))
            message = "You Won! The answer was " + target;
        if (state.equals("LOST"))
            message = "Game Over! The answer was " + target;

        return new GuessResponse(green, yellow, state, message);
    }
}
