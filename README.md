# CSE535-Tic-Tac-Toe

MISERE TIK TAC TOE - The concept of the game is that if a user makes 3 in a row, they **lose**

Our app supports:
- 👤 **Single-player vs AI** (Easy / Medium / Hard)
- 👥 **Two-player** (on the same device)
- 📡 **Peer-to-peer gameplay** (Bluetooth / Wi-Fi Direct) – cross-device matches without internet
- 🕓 **Game history** – local JSON-style storage of past matches
- ⚙️ **Optimized AI** – Minimax (negamax style) with memoization + early cut-off

**Authors**
Chao-Shiang Chen
Dhruv Jain 
Manya Mehta
Nishtha Chaudhary
Sandarsh Murthy

## 1. What is Misère Tic-Tac-Toe?
In the Traditional Tic-Tac-Toe, the player needs to make 3 in a row to win the game.
**Misère variant** = The rule flips, if you make 3 in a row you lose!

---

## 2. Features
- 🧩 **Misère rules**<br>
  Unlike traditional Tic-Tac-Toe, this variant flips the objective. f you complete three in a row, you lose! This inversion adds depth and strategy, requiring players to anticipate traps instead of rushing to win.

- 🤖 **On-device AI with 3 difficulty levels**<br>
  Play against a smart AI that adapts to your skill level:
  1. Easy mode: AI makes random moves for a relaxed experience and basic gameplay validation.
  2. Medium mode: AI avoids self-defeating moves and mixes logic with randomness for a balanced challenge.
  3. Hard mode (actually very hard!): AI uses an optimized Minimax algorithm with negamax variation and alpha–beta–like pruning including memoization, and early cut-off for fast, optimal play even on mobile hardware.

- 🧍‍♂️🧍‍♀️ **Local Two-Player Mode**<br>
  Challenge a friend on the same device with smooth turn-switching, perfect for quick offline matches.

- 📡 **Peer-to-Peer Gameplay with Bluetooth Connectivity**<br>
  Play with your friend on separate devices without internet using a lightweight Bluetooth connection. The real-time synchronization ensures fairness and low latency in the game. And the automatic state recovery ensures reliability in case a packet is delayed or lost.

- 🕓 **Game History and Data Persistence**<br>
  Every completed game, whether it's against the AI or peer-to-peer play, is stored locally in a JSON-style format, including timestamp, difficulty, and the outcome. Players can view, scroll, and manage past matches via the **Past Games** screen, with async operations for a smooth UI experience.

- 🎨 **User Experience**<br>
  Clean, modern interface with gradient themes, responsive layouts, and consistent visual feedback across all screens which ensurs intuitive and enjoyable gameplay on all device sizes.
