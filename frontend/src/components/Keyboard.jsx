import { useState } from 'react';
import './Keyboard.css';

const ROWS = [
  ['Q', 'W', 'E', 'R', 'T', 'Y', 'U', 'I', 'O', 'P'],
  ['A', 'S', 'D', 'F', 'G', 'H', 'J', 'K', 'L'],
  ['ENTER', 'Z', 'X', 'C', 'V', 'B', 'N', 'M', 'BACKSPACE']
];

function Keyboard({ onKeyPress }) {
  // Store the marking state of keys. States: 'default' -> 'dim' -> 'default'
  const [keyStates, setKeyStates] = useState({});

  const handleKeyClick = (key) => {
    onKeyPress(key);
  };

  const handleRightClick = (e, key) => {
    e.preventDefault(); // Prevent context menu
    if (key === 'ENTER' || key === 'BACKSPACE') return;
    
    setKeyStates(prev => {
       const current = prev[key] || 'default';
       return {
         ...prev,
         [key]: current === 'default' ? 'dim' : 'default'
       };
    });
  };

  return (
    <div className="keyboard">
        <div className="keyboard-instructions">
          (Right-click a key to dim it out)
        </div>
      {ROWS.map((row, i) => (
        <div key={i} className="keyboard-row">
          {row.map((key) => {
             const stateClass = keyStates[key] === 'dim' ? 'dimmed' : '';
             return (
              <button
                key={key}
                className={`key ${key === 'ENTER' || key === 'BACKSPACE' ? 'large' : ''} ${stateClass}`}
                onClick={() => handleKeyClick(key)}
                onContextMenu={(e) => handleRightClick(e, key)}
              >
                {key === 'BACKSPACE' ? '⌫' : key}
              </button>
             );
          })}
        </div>
      ))}
    </div>
  );
}

export default Keyboard;
