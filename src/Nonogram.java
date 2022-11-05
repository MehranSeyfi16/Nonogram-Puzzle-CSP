import java.util.ArrayList;
import java.util.Queue;
import java.util.LinkedList;

public class Nonogram {

    private final State state;
    private final int n;
    ArrayList<ArrayList<Integer>> row_constraints;
    ArrayList<ArrayList<Integer>> col_constraints;

    public Nonogram(State state, ArrayList<ArrayList<Integer>> row_constraints, ArrayList<ArrayList<Integer>> col_constraints) {
        this.state = state;
        this.n = state.getN();
        this.row_constraints = row_constraints;
        this.col_constraints = col_constraints;
    }


    public void start() {
        long tStart = System.nanoTime();
        backtrack(state);
        //forwardCheck(state);
        long tEnd = System.nanoTime();
        System.out.println("Total time: " + (tEnd - tStart)/1000000000.000000000);
    }

    private boolean backtrack(State state) {
        if (isFinished(state)) {
            System.out.println("Result Board: \n");
            state.printBoard();
            return true;
        }
        if (allAssigned(state)) {
            return false;
        }

        int[] mrvRes = MRV(state);
        for (String s : LCV(state, mrvRes)) {
            State newState = state.copy();
            newState.setIndexBoard(mrvRes[0], mrvRes[1], s);
            newState.removeIndexDomain(mrvRes[0], mrvRes[1], s);
            if (!isConsistent(newState)) {
                continue;
            }

            if (backtrack(newState)) {
                return true;
            }
        }

        return false;
    }

    public boolean forwardCheck(State state) {

        if (isFinished(state)) {
            System.out.println("Result Board: \n");
            state.printBoard();
            return true;
        }
        if (allAssigned(state)) {
            return false;
        }

        int[] mrvRes = MRV(state);
        ArrayList<String> lcv = LCV(state, mrvRes);
        for (String s : lcv) {
            State newState = state.copy();
            newState.setIndexBoard(mrvRes[0], mrvRes[1], s);
            newState.removeIndexDomain(mrvRes[0], mrvRes[1], s);
            updateDomain(newState);

            if (!isConsistent(newState)) {
                continue;
            }

            if (forwardCheck(newState)) {
                return true;
            }
        }
        return false;
    }

    public void updateDomain(State state) {
        updateDomainFullRowCheck(state);
        updateDomainFullColumnCheck(state);
        updateSingleValueRowPositions(state);
//        updateSingleValueColumnPositions(state);
    }

    public void updateDomainFullRowCheck(State state) {
        ArrayList<ArrayList<String>> board = state.getBoard();
        ArrayList<Integer> constraints = new ArrayList<>();
        ArrayList<Integer> goodRows = new ArrayList<>();
        int count = 0;

        for (int i = 0; i < n; i++) { //row constraints
            for (int j = 0; j < n; j++)
                if (board.get(i).get(j).equals("F"))
                    count++;
                else if (count != 0) {
                    constraints.add(count);
                    count = 0;
                }

            if (constraints.equals(row_constraints.get(i)))
                goodRows.add(i);
            constraints.clear();
        }

        for (Integer goodRow : goodRows)
            for (int j = 0; j < n; j++)
                if (board.get(goodRow).get(j).equals("E")) {
                    board.get(goodRow).set(j, "X");
                    state.setIndexBoard(goodRow, j, "X");
                    state.removeIndexDomain(goodRow, j, "X");
                }
    }

    public void updateDomainFullColumnCheck(State state) {
        ArrayList<ArrayList<String>> board = state.getBoard();
        ArrayList<Integer> constraints = new ArrayList<>();
        ArrayList<Integer> goodColumns = new ArrayList<>();
        int count = 0;
        for (int i = 0; i < n; i++) { //column constraints
            for (int j = 0; j < n; j++) {
                if (board.get(i).get(j).equals("F"))
                    count++;
                else if (count != 0) {
                    constraints.add(count);
                    count = 0;
                }
            }
            if (constraints.equals(col_constraints.get(i))) {
                goodColumns.add(i);
            }
            constraints.clear();
        }

        for (Integer goodColumn : goodColumns)
            for (int i = 0; i < n; i++)
                if (board.get(goodColumn).get(i).equals("E")) {
                    state.setIndexBoard(i, goodColumn, "X");
                    state.removeIndexDomain(i, goodColumn, "X");
                }
    }

    public void updateSingleValueRowPositions(State state) {
        ArrayList<ArrayList<String>> board = state.getBoard();
        for (int i = 0; i < n; i++) {
            if (row_constraints.get(i).size() == 1) {
                if (board.get(i).get(0).equals("F")) {
                    for (int j=1; j!=Integer.parseInt(row_constraints.get(i).get(0).toString())-1; j++){
                        state.setIndexBoard(i, j, "F");
                        state.removeIndexDomain(i, j, "F");
                    }
                }
                if (board.get(i).get(n-1).equals("F")) {
                    int count = 0;
                    for (int j=n-2; count!=Integer.parseInt(row_constraints.get(i).get(0).toString())-1; j--){
                        state.setIndexBoard(i, j, "F");
                        state.removeIndexDomain(i, j, "F");
                        count++;
                    }
                }
                for (int j = 0; j < n; j++) {
                    if (!board.get(i).get(j).equals("F")) {
                        state.setIndexBoard(i, j, "X");
                        state.removeIndexDomain(i, j, "X");
                    }
                }
            }
        }
    }

    public void updateSingleValueColumnPositions(State state) {
        ArrayList<ArrayList<String>> board = state.getBoard();
        for (int i = 0; i < n; i++) {
            if (col_constraints.get(i).size() == 1){
                int count = 0;
                if (board.get(0).get(i).equals("F")){
                    System.out.println(col_constraints.get(i));
                    for (int j=0; count!=Integer.parseInt(col_constraints.get(i).get(0).toString())-1; j++){
                        state.setIndexBoard(j, i, "F");
                        state.removeIndexDomain(j, i, "F");
                        count++;
                    }
                }
                if (board.get(n-1).get(i).equals("F")){
                    for (int j=n-2; count!=Integer.parseInt(col_constraints.get(i).get(0).toString())-1; j--){
                        state.setIndexBoard(j, i, "F");
                        state.removeIndexDomain(j, i, "F");
                        count++;
                    }
                }
                for (int j = 0; j < n; j++) {
                    if (!board.get(j).get(i).equals("F")){
                        state.setIndexBoard(j, i, "X");
                        state.removeIndexDomain(j, i, "X");
                    }
                }
            }
        }
    }

    private ArrayList<String> LCV (State state, int[] var) {
        ArrayList<String> strings = new ArrayList<>();
        strings.add("F");
        strings.add("X");
        State newState = state.copy();
        newState.setIndexBoard(var[0], var[1], "F");
        newState.removeIndexDomain(var[0], var[1], "F");
        updateDomain(newState);
        ArrayList<ArrayList<ArrayList<String>>> cDomain = newState.getDomain();
        int sum = 0;
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                sum += cDomain.get(i).get(j).size();
        newState = state.copy();
        newState.setIndexBoard(var[0], var[1], "X");
        newState.removeIndexDomain(var[0], var[1], "X");
        updateDomain(newState);
        cDomain = newState.getDomain();
        int sum_2 = 0;
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                sum_2 += cDomain.get(i).get(j).size();
        if (sum > sum_2)
            strings.remove(1);

        return strings;
    }

    private int[] MRV (State state) {
        ArrayList<ArrayList<String>> cBoard = state.getBoard();
        ArrayList<ArrayList<ArrayList<String>>> cDomain = state.getDomain();

        int min = Integer.MAX_VALUE;
        int[] result = new int[2];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (cBoard.get(i).get(j).equals("E")) {
                    int val = cDomain.get(i).get(j).size();
                    if (val < min) {
                        min = val;
                        result[0] = i;
                        result[1] = j;
                    }
                }
            }
        }
        return result;
    }

    private boolean allAssigned(State state) {
        ArrayList<ArrayList<String>> cBoard = state.getBoard();

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                String s = cBoard.get(i).get(j);
                if (s.equals("E"))
                    return false;
            }
        }
        return true;
    }

    private boolean isConsistent(State state) {

        ArrayList<ArrayList<String>> cBoard = state.getBoard();
        //check row constraints
        for (int i = 0; i < n; i++) {
            int sum = 0;
            for (int x : row_constraints.get(i)) {
                sum += x;
            }
            int count_f = 0;
            int count_e = 0;
            int count_x = 0;
            for (int j = 0; j < n; j++) {
                switch (cBoard.get(i).get(j)) {
                    case "F" -> count_f++;
                    case "E" -> count_e++;
                    case "X" -> count_x++;
                }
            }

            if (count_x > n - sum) {
                return false;
            }
            if (count_f != sum && count_e == 0) {
                return false;
            }

            Queue<Integer> constraints = new LinkedList<>(row_constraints.get(i));
            int count = 0;
            boolean flag = false;
            for (int j = 0; j < n; j++) {
                if (cBoard.get(i).get(j).equals("F")) {
                    flag = true;
                    count++;
                } else if (cBoard.get(i).get(j).equals("X")) {
                    if (flag) {
                        flag = false;
                        if (!constraints.isEmpty()){
                            if (count != constraints.peek()) {
                                return false;
                            }
                            constraints.remove();
                        }
                        count = 0;
                    }
                }
            }

        }

        //check col constraints

        for (int j = 0; j < n; j++) {
            int sum = 0;
            for (int x : col_constraints.get(j)) {
                sum += x;
            }
            int count_f = 0;
            int count_e = 0;
            int count_x = 0;
            for (int i = 0; i < n; i++) {
                switch (cBoard.get(i).get(j)) {
                    case "F" -> count_f++;
                    case "E" -> count_e++;
                    case "X" -> count_x++;
                }
            }
            if (count_x > n -sum) {
                return false;
            }
            if (count_f != sum && count_e == 0) {
                return false;
            }

            Queue<Integer> constraints = new LinkedList<>(col_constraints.get(j));
            int count = 0;
            boolean flag = false;
            for (int i = 0; i < n; i++) {
                if (cBoard.get(i).get(j).equals("F")) {
                    flag = true;
                    count++;
                } else if (cBoard.get(i).get(j).equals("X")) {
                    if (flag) {
                        flag = false;
                        if (!constraints.isEmpty()){
                            if (count != constraints.peek()) {
                                return false;
                            }
                            constraints.remove();
                        }
                        count = 0;
                    }
                }
            }
        }
        return true;
    }

    private boolean isFinished(State state) {
        return allAssigned(state) && isConsistent(state);
    }

}
