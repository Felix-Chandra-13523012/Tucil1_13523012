import java.io.*;
import java.util.*;
import java.nio.file.*;

class Coordinate {
    int r, c;
    Coordinate(int r, int c) {
        this.r = r;
        this.c = c;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coordinate that = (Coordinate) o;
        return r == that.r && c == that.c;
    }

    @Override
    public int hashCode() {
        return Objects.hash(r, c);
    }
}

class Piece {
    char id;
    List<List<Coordinate>> transformations; 
    Piece(char id, List<String> shape) {
        this.id = id;
        this.transformations = generateTransformations(shape);
    }
    private List<List<Coordinate>> generateTransformations(List<String> shape) {
        List<List<Coordinate>> transformations = new ArrayList<>();
        List<Coordinate> original = parseShape(shape);
        for (int rot = 0; rot < 4; rot++) {
            List<Coordinate> rotated = rotate(original, rot);
            transformations.add(rotated);
            transformations.add(reflect(rotated, true));
            transformations.add(reflect(rotated, false));
        }

        return transformations;
    }

    private List<Coordinate> parseShape(List<String> shape) {
        List<Coordinate> coords = new ArrayList<>();
        for (int r = 0; r < shape.size(); r++) {
            String line = shape.get(r);
            for (int c = 0; c < line.length(); c++) {
                if (line.charAt(c) == id) {
                    coords.add(new Coordinate(r, c));
                }
            }
        }
        return coords;
    }

    private List<Coordinate> rotate(List<Coordinate> coords, int rotations) {
        List<Coordinate> rotated = new ArrayList<>();
        for (Coordinate coord : coords) {
            int r = coord.r;
            int c = coord.c;
            for (int i = 0; i < rotations; i++) {
                int newR = c;
                int newC = -r;
                r = newR;
                c = newC;
            }
            rotated.add(new Coordinate(r, c));
        }
        return normalize(rotated);
    }

    private List<Coordinate> reflect(List<Coordinate> coords, boolean horizontal) {
        List<Coordinate> reflected = new ArrayList<>();
        for (Coordinate coord : coords) {
            int r = coord.r;
            int c = coord.c;
            if (horizontal) {
                c = -c;
            } else {
                r = -r;
            }
            reflected.add(new Coordinate(r, c));
        }
        return normalize(reflected);
    }

    private List<Coordinate> normalize(List<Coordinate> coords) {
        int minR = coords.stream().mapToInt(c -> c.r).min().orElse(0);
        int minC = coords.stream().mapToInt(c -> c.c).min().orElse(0);
        List<Coordinate> normalized = new ArrayList<>();
        for (Coordinate coord : coords) {
            normalized.add(new Coordinate(coord.r - minR, coord.c - minC));
        }
        return normalized;
    }
}

class PuzzleSolver {
    private int N, M;
    private List<Piece> pieces;
    private int cases = 0;
    private char[][] solutionBoard;
    private boolean solved = false;

    PuzzleSolver(int N, int M, List<Piece> pieces) {
        this.N = N;
        this.M = M;
        this.pieces = pieces;
    }

    public boolean solve() {
        long start = System.currentTimeMillis();
        solved = backtrack(pieces, new char[N][M]);
        long end = System.currentTimeMillis();
        System.out.println("Waktu pencarian: " + (end - start) + " ms");
        System.out.println("Banyak kasus yang ditinjau: " + cases);
        return solved;
    }

    private boolean backtrack(List<Piece> remaining, char[][] board) {
        if (remaining.isEmpty()) {
            solutionBoard = board;
            return true;
        }

        int[] pos = findEmpty(board);
        if (pos == null) {
            return false;
        }
        int r = pos[0], c = pos[1];

        for (int i = 0; i < remaining.size(); i++) {
            Piece piece = remaining.get(i);
            List<Piece> newRemaining = new ArrayList<>(remaining);
            newRemaining.remove(i);

            for (List<Coordinate> transformation : piece.transformations) {
                cases++;
                if (canPlace(board, r, c, transformation)) {
                    char[][] newBoard = copy(board);
                    place(newBoard, r, c, transformation, piece.id);
                    if (backtrack(newRemaining, newBoard)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private int[] findEmpty(char[][] board) {
        for (int r = 0; r < N; r++) {
            for (int c = 0; c < M; c++) {
                if (board[r][c] == '\0') {
                    return new int[]{r, c};
                }
            }
        }
        return null;
    }

    private boolean canPlace(char[][] board, int r, int c, List<Coordinate> coordinates) {
        for (Coordinate coord : coordinates) {
            int nr = r + coord.r;
            int nc = c + coord.c;
            if (nr < 0 || nr >= N || nc < 0 || nc >= M || board[nr][nc] != '\0') {
                return false;
            }
        }
        return true;
    }

    private void place(char[][] board, int r, int c, List<Coordinate> coordinates, char id) {
        for (Coordinate coord : coordinates) {
            board[r + coord.r][c + coord.c] = id;
        }
    }

    private char[][] copy(char[][] board) {
        char[][] newBoard = new char[N][M];
        for (int i = 0; i < N; i++) {
            System.arraycopy(board[i], 0, newBoard[i], 0, M);
        }
        return newBoard;
    }

    public void printSolution() {
        if (!solved) {
            System.out.println("Puzzle tidak memiliki solusi.");
            return;
        }
        String[] colors = {"\033[31m", "\033[32m", "\033[33m", "\033[34m", "\033[35m", "\033[36m", "\033[37m"};
        Map<Character, String> colorMap = new HashMap<>();
        int colorIndex = 0;
        for (Piece piece : pieces) {
            if (!colorMap.containsKey(piece.id)) {
                colorMap.put(piece.id, colors[colorIndex % colors.length]);
                colorIndex++;
            }
        }
        for (char[] row : solutionBoard) {
            for (char c : row) {
                System.out.print(colorMap.get(c) + c + "\033[0m ");
            }
            System.out.println();
        }
    }

    public void saveSolution(String filename) throws IOException {
        try (PrintWriter out = new PrintWriter(filename)) {
            for (char[] row : solutionBoard) {
                out.println(new String(row));
            }
        }
    }
}

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Masukkan nama file input: ");
        String inputFile = br.readLine();
        List<String> lines = Files.readAllLines(Paths.get(inputFile));
        String[] firstLine = lines.get(0).split(" ");
        int N = Integer.parseInt(firstLine[0]);
        int M = Integer.parseInt(firstLine[1]);
        int P = Integer.parseInt(firstLine[2]);
        List<Piece> pieces = new ArrayList<>();
        List<String> currentShape = new ArrayList<>();
        char currentId = '\0';

        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) continue;
            if (currentShape.isEmpty()) {
                currentId = line.charAt(0);
                currentShape.add(line);
            } else {
                if (line.charAt(0) == currentId) {
                    currentShape.add(line);
                } else {
                    pieces.add(new Piece(currentId, new ArrayList<>(currentShape)));
                    currentShape.clear();
                    currentId = line.charAt(0);
                    currentShape.add(line);
                }
            }
        }
        if (!currentShape.isEmpty()) {
            pieces.add(new Piece(currentId, new ArrayList<>(currentShape)));
        }

        int totalArea = 0;
        for (Piece piece : pieces) {
            totalArea += piece.transformations.get(0).size();
        }
        if (totalArea != N * M) {
            System.out.println("Tidak ada solusi karena total luas blok puzzle tidak sama dengan luas papan.");
            return;
        }

        PuzzleSolver solver = new PuzzleSolver(N, M, pieces);
        if (solver.solve()) {
            solver.printSolution();
            System.out.print("Apakah anda ingin menyimpan solusi? (ya/tidak) ");
            String answer = br.readLine().trim().toLowerCase();
            if (answer.equals("ya")) {
                System.out.print("Masukkan nama file output: ");
                String outputFile = br.readLine();
                solver.saveSolution(outputFile);
            }
        } else {
            System.out.println("Puzzle tidak memiliki solusi.");
        }
    }
}
