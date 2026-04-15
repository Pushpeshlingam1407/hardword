package com.hardword.controller;

import com.hardword.model.GuessRequest;
import com.hardword.model.GuessResponse;
import com.hardword.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class GameController {

    private final GameService gameService;

    @Autowired
    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping("/")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Hardword backend is running. Use POST /api/game/start or POST /api/start to begin.");
    }

    @GetMapping(path = {"/start", "/game/start"})
    public ResponseEntity<String> startInfo() {
        return ResponseEntity.ok("Send a POST request to /api/game/start or /api/start to create a game session.");
    }

    @GetMapping(path = {"/guess", "/game/guess"})
    public ResponseEntity<String> guessInfo() {
        return ResponseEntity.ok("Send a POST request to /api/game/guess or /api/guess with JSON body {\"sessionId\":\"...\",\"guess\":\"WORD\"}.");
    }

    @PostMapping(path = {"/game/start", "/start"})
    public ResponseEntity<?> startGame() {
        String sessionId = gameService.startNewSession();
        return ResponseEntity.ok(Map.of("sessionId", sessionId));
    }

    @PostMapping(path = {"/game/guess", "/guess"})
    public ResponseEntity<?> makeGuess(@RequestBody GuessRequest request) {
        try {
            GuessResponse response = gameService.processGuess(request.getSessionId(), request.getGuess());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
