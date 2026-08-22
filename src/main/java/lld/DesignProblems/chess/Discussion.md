# Chess - Interview Requirements and Scope

## Time Constraint

- The solution should be designed and implemented in 45-60 minutes.
- The priority is a small, working chess engine with correct core behavior.
- Advanced rules should be supported by sensible extension points and discussed
  after the working implementation is complete.
- Avoid unnecessary framework, persistence, networking, or concurrency code.


## 1. Core Domain

- The game is played on a standard 8x8 board.
- There are two players:
  - White
  - Black
- White takes the first turn.
- Only one player can make a move at a time.
- A player can move only a piece belonging to their color.
- Turns alternate after every successful move.
- An invalid move must not change the board or the current turn.


## 2. Pieces and Movement

### Starting Pieces

Each player starts with:
- 1 King
  - Moves one square in any direction.
  - Cannot move onto a square attacked by an opponent.
- 1 Queen
  - Moves any number of unobstructed squares horizontally, vertically, or
    diagonally.
- 2 Rooks
  - Move any number of unobstructed squares horizontally or vertically.
- 2 Bishops
  - Move any number of unobstructed squares diagonally.
- 2 Knights
  - Move in an L shape.
  - Can jump over other pieces.
- 8 Pawns
  - Move one square forward into an empty square.
  - May move two squares forward from their initial position if both squares
    are empty.
  - Capture one square diagonally forward.
  - Movement direction depends on the pawn's color.

### General Movement Rules

- Source and destination positions must be valid board positions and must differ.
- A piece cannot move onto a square occupied by a friendly piece.
- Rooks, bishops, and queens cannot jump over pieces.
- A piece may capture an opponent piece by legally moving to its square.
- The king is never captured; checkmate ends the game before king capture.
- A move is illegal if it leaves the moving player's king in check.


## 3. Game States and End Conditions

### States

The game should represent the following states:
- Active
- Check
- Checkmate
- Stalemate
- Draw

### Definitions

- Check:
  - The current player's king is under attack.
  - Check is a game condition, not a winning condition.
- Checkmate:
  - The current player's king is in check and the player has no legal move.
  - The opposing player wins.
- Stalemate:
  - The current player's king is not in check and the player has no legal move.
  - The game ends in a draw.
- Draw:
  - For the interview implementation, stalemate is the required draw condition.
  - Other draw rules can be discussed as extensions.

- No move is accepted after the game reaches a terminal state.


## 4. Required Working Functionality

The interview implementation should demonstrate:
- Standard board initialization.
- Movement rules for all six piece types.
- Path-obstruction validation for sliding pieces.
- Turn validation and alternating turns.
- Legal captures.
- Rejection of invalid moves without state mutation.
- Check detection.
- Prevention of moves that expose the moving player's king.
- Checkmate and stalemate detection if time permits.
- A driver program or focused tests covering representative legal and illegal
  moves.

If time is constrained, check detection and self-check prevention take priority
over checkmate and stalemate detection. The remaining behavior should be
explained accurately rather than implemented using an incorrect shortcut.


## 5. Explicitly Deferred Requirements

The following are outside the first working implementation and will be
discussed afterward as extensions:
- Castling.
- En passant.
- Pawn promotion and selection of the promoted piece.
- Threefold repetition.
- Fifty-move draw rule.
- Draw by insufficient material.
- Draw agreement and resignation.
- Undo and redo.
- Algebraic move notation.
- Chess clocks.
- Persistence and restoration of a game.
- Online multiplayer and concurrency.
- Computer-controlled players.


## 6. Core Invariants

- Exactly one player owns the current turn.
- A player can move only their own piece.
- Every accepted move follows the selected piece's movement rules.
- Sliding pieces have an unobstructed path.
- A destination occupied by a friendly piece is invalid.
- A player cannot make a move that leaves their own king in check.
- A king cannot move onto an attacked square.
- The two kings cannot occupy adjacent squares.
- An invalid move leaves all game state unchanged.
- Checkmate requires both check and the absence of every legal response.
- Stalemate requires no check and the absence of every legal move.
- A terminal game cannot accept another move.


## 7. Minimal Interview Design

### `ChessGame`

- Coordinates the game.
- Owns the board, current turn, game status, and move history.
- Accepts move requests and preserves game-level invariants.

### `Board`

- Owns the current placement of pieces.
- Provides safe access to positions and pieces.
- Supports movement and board inspection.

### `Position`

- Immutable value object representing a row and column.
- Validates or exposes whether coordinates are within the board.

### `Player`

- Represents player identity and color.
- Does not own chess-rule logic.

### `Piece`

- Represents a piece's color and type.
- Delegates to, or implements, its movement behavior.

### `MovementRule`

- Encapsulates how a particular piece can move.
- Uses the board when movement depends on occupancy or path obstruction.
- Can be implemented using Strategy, or equivalent piece polymorphism, without
  a large conditional statement.

### `Move`

- Immutable record of source, destination, moved piece, captured piece, and
  information required by move history.

### `CheckDetector`

- Determines whether a color's king is attacked in a given board position.
- Keeps attack detection separate from turn orchestration.

### `GameStatus`

- Represents Active, Check, Checkmate, Stalemate, or Draw.


## 8. Design Priorities for the Interview

- Keep the model small and behavior-focused.
- Prefer composition and cohesive responsibilities over unnecessary layers.
- Introduce interfaces only where behavior genuinely varies.
- Avoid forcing design patterns merely to name them.
- Make invalid states and partial state changes difficult to create.
- Optimize first for correctness, readability, and explainability.
- Be ready to explain important alternatives and trade-offs.
- Complete and demonstrate the core flow before discussing advanced extensions.
