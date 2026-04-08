import { useState, useEffect, useCallback } from "react";
import "./App.css";
import Keyboard from "./components/Keyboard";

const API_BASE = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api/game";
function App() {
  const [sessionId, setSessionId] = useState(null);
  const [guesses, setGuesses] = useState([]);
  const [currentGuess, setCurrentGuess] = useState("");
  const [gameState, setGameState] = useState("PLAYING");
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  // Track disabled letters that are confirmed not in the word (0 green 0 yellow)
  const [disabledLetters, setDisabledLetters] = useState(new Set());
  const [showRules, setShowRules] = useState(false);

  const startNewGame = useCallback(async () => {
    setLoading(true);
    try {
      const res = await fetch(`${API_BASE}/start`, { method: "POST" });
      const data = await res.json();
      setSessionId(data.sessionId);
      setGuesses([]);
      setCurrentGuess("");
      setGameState("PLAYING");
      setMessage("");
      setError("");
      setDisabledLetters(new Set());
    } catch (err) {
      setError("Failed to connect to the server.");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    startNewGame();
  }, [startNewGame]);

  const onKeyPress = useCallback(
    async (key) => {
      if (gameState !== "PLAYING") return;

      if (key === "ENTER") {
        if (currentGuess.length !== 4) {
          setError("Not enough letters");
          setTimeout(() => setError(""), 2000);
          return;
        }

        try {
          const res = await fetch(`${API_BASE}/guess`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ sessionId, guess: currentGuess }),
          });

          if (!res.ok) {
            const errData = await res.json();
            setError(errData.error || "Invalid guess");
            setTimeout(() => setError(""), 2000);
            return;
          }

          const data = await res.json();
          const newGuesses = [
            ...guesses,
            { word: currentGuess, green: data.green, yellow: data.yellow },
          ];
          setGuesses(newGuesses);

          // If 0 green and 0 yellow, all letters in this guess are definitely not in the word
          if (data.green === 0 && data.yellow === 0) {
            setDisabledLetters((prev) => {
              const newSet = new Set(prev);
              for (let i = 0; i < currentGuess.length; i++) {
                newSet.add(currentGuess[i]);
              }
              return newSet;
            });
          }

          setCurrentGuess("");

          if (data.state !== "PLAYING") {
            setGameState(data.state);
            setMessage(data.message);
          }
        } catch (err) {
          setError("Failed to submit guess");
        }
      } else if (key === "BACKSPACE") {
        setCurrentGuess((prev) => prev.slice(0, -1));
      } else if (/^[A-Z]$/.test(key)) {
        if (currentGuess.length < 4) {
          // Validation: Unique letters and disabled letters
          if (currentGuess.includes(key)) {
            setError("Letters must be distinct");
            setTimeout(() => setError(""), 1500);
            return;
          }

          if (disabledLetters.has(key)) {
            setError(`Letter ${key} is not in the word`);
            setTimeout(() => setError(""), 1500);
            return;
          }

          setCurrentGuess((prev) => prev + key);
        }
      }
    },
    [currentGuess, gameState, guesses, sessionId, disabledLetters],
  );

  useEffect(() => {
    const handleKeyDown = (e) => {
      const key = e.key.toUpperCase();
      if (key === "ENTER" || key === "BACKSPACE" || /^[A-Z]$/.test(key)) {
        onKeyPress(key);
      }
    };
    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [onKeyPress]);

  return (
    <div className="app-container">
      <header>
        <div className="title-row">
          <h1>Hardword</h1>
          <button className="rules-btn" onClick={() => setShowRules(true)}>
            ?
          </button>
        </div>
        <div className="subtitle">Find the hidden 4-letter isogram.</div>
      </header>

      {showRules && (
        <div className="modal-overlay" onClick={() => setShowRules(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h2>How to Play</h2>
            <ul>
              <li>
                Guess the <b>4-letter word</b>.
              </li>
              <li>
                Every letter must be <b>distinct</b> (no repeated letters).
              </li>
              <li>After each guess, you will receive two numbers:</li>
            </ul>
            <div className="rules-example">
              <div>
                <span className="score green mini">2</span> : Two letters are
                correct AND in the correct position.
              </div>
              <div>
                <span className="score yellow mini">1</span> : One letter is
                correct BUT in the wrong position.
              </div>
            </div>
            <p>
              If you get a 0 and 0, none of those letters are in the secret
              word! You will be blocked from using them again.
            </p>
            <button className="close-btn" onClick={() => setShowRules(false)}>
              Got It
            </button>
          </div>
        </div>
      )}

      <div className="game-container">
        {error && <div className="toast toast-error">{error}</div>}
        {!error && loading && <div className="toast">Loading game...</div>}

        <div className="board">
          {Array.from({ length: 8 }).map((_, i) => {
            const guessObj = guesses[i];
            const isCurrentRow = i === guesses.length;
            const rowClass = `row ${isCurrentRow ? "current" : ""}`;

            let rowWord = "";
            if (guessObj) rowWord = guessObj.word;
            else if (isCurrentRow) rowWord = currentGuess;

            return (
              <div key={i} className="row-wrapper">
                <div className={rowClass}>
                  {Array.from({ length: 4 }).map((_, j) => (
                    <div
                      key={j}
                      className={`tile ${guessObj ? "filled" : ""} ${isCurrentRow && rowWord[j] ? "active" : ""}`}
                    >
                      {rowWord[j] || ""}
                    </div>
                  ))}
                </div>
                {/* Score Indicators next to row */}
                <div className="scores">
                  {guessObj ? (
                    <>
                      <div className="score green">{guessObj.green}</div>
                      <div className="score yellow">{guessObj.yellow}</div>
                    </>
                  ) : (
                    <>
                      <div className="score empty"></div>
                      <div className="score empty"></div>
                    </>
                  )}
                </div>
              </div>
            );
          })}
        </div>

        {gameState !== "PLAYING" && (
          <div className="game-over">
            <h2>{gameState === "WON" ? "🎉" : "💀"}</h2>
            <h3 className="final-message">{message}</h3>
            <button onClick={startNewGame}>Play Again</button>
          </div>
        )}

        <Keyboard onKeyPress={onKeyPress} />
      </div>
    </div>
  );
}

export default App;
