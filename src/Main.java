import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Main {
    private static ArrayList<ArrayList<Integer>> row_constraints;
    private static ArrayList<ArrayList<Integer>> col_constraints;

    private static ArrayList<ArrayList<String>> board;
    private static ArrayList<ArrayList<ArrayList<String>>> domain;

    public static void main(String[] args) {
        File file = new File("E:\\University Semester\\5\\Basics and Application of Artificial Intelligence\\Projects\\Search Project\\Nonogram-Puzzle-CSP\\inputs\\input5.txt");
        Scanner scanner;

        try {
            scanner = new Scanner(file);
            int n = scanner.nextInt();
            scanner.nextLine();
            row_constraints = new ArrayList<>(n);
            col_constraints = new ArrayList<>(n);

            for (int i = 0; i < n; i++) {
                String[] numbers = scanner.nextLine().split(" ");
                ArrayList<Integer> row = new ArrayList<>();
                for (String s : numbers) {
                    row.add(Integer.parseInt(s));
                }
                row_constraints.add(i, row);
            }

            for (int i = 0; i < n; i++) {
                String[] numbers = scanner.nextLine().split(" ");
                ArrayList<Integer> col = new ArrayList<>();
                for (String s : numbers) {
                    col.add(Integer.parseInt(s));
                }
                col_constraints.add(i, col);
            }

            scanner.close();

            board = new ArrayList<>(n);
            domain = new ArrayList<>(n);

            for (int i = 0; i < n; i++) {
                board.add(new ArrayList<>());
                domain.add(new ArrayList<>());

                for (int j = 0; j < n; j++) {
                    board.get(i).add("E");
                    domain.get(i).add(new ArrayList<>(Arrays.asList(
                        "F",
                        "X"
                    )));
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        State state = new State(board, domain);
        Nonogram nonogram = new Nonogram(state, row_constraints, col_constraints);
        nonogram.start();

    }
}


