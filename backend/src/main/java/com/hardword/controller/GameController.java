package com.hardword.controller;

import com.hardword.model.GuessRequest;
import com.hardword.model.GuessResponse;
import com.hardword.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/game")
public class GameController {

    private final GameService gameService;

    @Autowired
    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping("/start")
    public ResponseEntity<?> startGame() {
        String sessionId = gameService.startNewSession();
        return ResponseEntity.ok(Map.of("sessionId", sessionId));
    }

    @PostMapping("/guess")
    public ResponseEntity<?> makeGuess(@RequestBody GuessRequest request) {
        try {
            GuessResponse response = gameService.processGuess(request.getSessionId(), request.getGuess());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
