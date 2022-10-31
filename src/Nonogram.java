import java.util.ArrayList;
import java.util.Queue;
import java.util.LinkedList;

public class Nonogram {

    private State state;
    private int n;
    ArrayList<ArrayList<Integer>> row_constraints;
    ArrayList<ArrayList<Integer>> col_constraints;

    public Nonogram(State state, 
        ArrayList<ArrayList<Integer>> row_constraints, 
        ArrayList<ArrayList<Integer>> col_constraints) {
        this.state = state;
        this.n = state.getN();
        this.row_constraints = row_constraints;
        this.col_constraints = col_constraints;
    }


    public void start() {
        long tStart = System.nanoTime();
        backtrack(state);
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

    public void updateDomain(State state){ 
        updateDomainFullRowCheck(state);
        updateDomainFullColumnCheck(state);
    }

    public void updateDomainFullRowCheck(State state){
        ArrayList<ArrayList<String>> board = state.getBoard();
        ArrayList<Integer> constraints = new ArrayList<>();
        ArrayList<Integer> goodRows = new ArrayList<>();
        int count = 0;
        for (int i = 0; i < n; i++) { //row constraints
            for (int j = 0; j < n; j++) {
                if (board.get(i).get(j).equals("F")) {
                    count++;
                }
                if (board.get(i).get(j+1).equals("E")) {
                    constraints.add(count);
                    count = 0;
                }
            }
            if (constraints.equals(row_constraints.get(i))) {
                goodRows.add(i);
            }
        }

        for (int i = 0; i < goodRows.size(); i++) {
            for (int j = 0; j < n; j++) {
                if (board.get(goodRows.get(i)).get(j).equals("E")) {
                    board.get(goodRows.get(i)).set(j, "X");
                }
            }
        }
    }

    public void updateDomainFullColumnCheck(State state){
        ArrayList<ArrayList<String>> board = state.getBoard();
        ArrayList<Integer> constraints = new ArrayList<>();
        ArrayList<Integer> goodColumns = new ArrayList<>();
        int count = 0;
        for (int i = 0; i < n; i++) { //column constraints
            for (int j = 0; j < n; j++) {
                if (board.get(j).get(i).equals("F")) {
                    count++;
                }
                if (board.get(j+1).get(i).equals("E")) {
                    constraints.add(count);
                    count = 0;
                }
            }
            if (constraints.equals(col_constraints.get(i))) {
                goodColumns.add(i);
            }
        }

        for (int i = 0; i < goodColumns.size(); i++) {
            for (int j = 0; j < n; j++) {
                if (board.get(i).get(goodColumns.get(j)).equals("E")) {
                    board.get(goodColumns.get(j)).set(j, "X");
                }
            }
        }
    }



    private ArrayList<String> LCV (State state, int[] var) {
        ArrayList<ArrayList<String>> cBoard = state.getBoard();
        ArrayList<ArrayList<ArrayList<String>>> cDomain = state.getDomain();



        return state.getDomain().get(var[0]).get(var[1]);
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
                if (cBoard.get(i).get(j).equals("F")) {
                    count_f++;
                } else if (cBoard.get(i).get(j).equals("E")) {
                    count_e++;
                } else if (cBoard.get(i).get(j).equals("X")) {
                    count_x++;
                }
            }

            if (count_x > n - sum) {
                return false;
            }
            if (count_f != sum && count_e == 0) {
                return false;
            }
            
            Queue<Integer> constraints = new LinkedList<>();
            constraints.addAll(row_constraints.get(i));
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
                if (cBoard.get(i).get(j).equals("F")) {
                    count_f++;
                } else if (cBoard.get(i).get(j).equals("E")) {
                    count_e++;
                } else if (cBoard.get(i).get(j).equals("X")) {
                    count_x++;
                }
            }
            if (count_x > n -sum) {
                return false;
            }
            if (count_f != sum && count_e == 0) {
                return false;
            }
            
            Queue<Integer> constraints = new LinkedList<>();
            constraints.addAll(col_constraints.get(j));
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
