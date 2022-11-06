import java.util.ArrayList;
import java.util.Collections;
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
        forwardCheck(state);
        long tEnd = System.nanoTime();
        System.out.println("Forward Check -- Total time: " + (tEnd - tStart)/1000000000.000000000);

        tStart = System.nanoTime();
        backtrack(state);
        tEnd = System.nanoTime();
        System.out.println("Back Track -- Total time: " + (tEnd - tStart)/1000000000.000000000);
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

        if (allAssigned(state))
            return false;

        int[] mrvRes = MRV(state);
        ArrayList<String> lcv = LCV(state, mrvRes);
        for (String s : lcv) {
            State newState = state.copy();
            newState.setIndexBoard(mrvRes[0], mrvRes[1], s);
            newState.removeIndexDomain(mrvRes[0], mrvRes[1], s);
            updateBoardAndDomain(newState, mrvRes[0], mrvRes[1], s);

            if (!isConsistent(newState) || emptyDomain(newState))
                continue;

            if (forwardCheck(newState))
                return true;
        }
        return false;
    }

    private boolean emptyDomain(State state) {
        ArrayList<ArrayList<ArrayList<String>>> domain = state.getDomain();
        for (int i = 0; i < domain.size(); i++)
            for (int j = 0; j < domain.get(i).size(); j++)
                if (domain.get(i).get(j).size() == 0 && state.getBoard().get(i).get(j).equals("E"))
                    return true;
        return false;
    }

    private void updateBoardAndDomain(State state, int row, int column, String value) {
        if (value.equals("F"))
        {
            updateFullRowCheck(state, row);
            updateFullColumnCheck(state, column);
            /*updateSingleConstraintRowCheck(state, row, column);
            updateSingleConstraintColumnCheck(state, row, column);*/
        }
       /* else
            updateUsingX(state, row, column);*/
    }

    private void updateFullRowCheck(State state, int row) {
        ArrayList<ArrayList<String>> board = state.getBoard();
        ArrayList<Integer> constraints = new ArrayList<>();
        int count = 0;
        for (int j = 0; j < n; j++)
            if (board.get(row).get(j).equals("F"))
                count++;
            else if (count != 0) {
                constraints.add(count);
                count = 0;
            }

            if (count != 0)
                constraints.add(count);

            if (constraints.equals(row_constraints.get(row)))
                for (int j = 0; j < n; j++)
                    if (board.get(row).get(j).equals("E")) {
                        state.setIndexBoard(row, j, "X");
                        state.removeIndexDomain(row, j, "X");
                        state.removeIndexDomain(row, j, "F");
                    }
    }

    private void updateFullColumnCheck(State state, int column) {
        ArrayList<ArrayList<String>> board = state.getBoard();
        ArrayList<Integer> constraints = new ArrayList<>();
        int count = 0;
            for (int i = 0; i < n; i++) {
                if (board.get(i).get(column).equals("F"))
                    count++;
                else if (count != 0) {
                    constraints.add(count);
                    count = 0;
                }
            }
            if (count != 0)
                constraints.add(count);

            if (constraints.equals(col_constraints.get(column)))
                for (int i = 0; i < n; i++)
                    if (board.get(i).get(column).equals("E")) {
                        state.setIndexBoard(i, column, "X");
                        state.removeIndexDomain(i, column, "X");
                        state.removeIndexDomain(i, column, "F");
                    }
    }

    private void updateSingleConstraintRowCheck(State state, int row, int column) {
        ArrayList<ArrayList<String>> board = state.getBoard();
        for (int i = 0; i < n; i++) {
            if (row_constraints.get(i).size() == 1) {
                if (board.get(i).get(0).equals("F")) {
                    for (int j = 0; j != Integer.parseInt(row_constraints.get(i).get(0).toString()) - 1; j++) {
                        if (board.get(j).get(i).equals("E"))
                        {
                            state.setIndexBoard(i, j, "F");
                            state.removeIndexDomain(i, j, "F");
                            state.removeIndexDomain(i, j, "X");
                        }
                    }
                }
                if (board.get(i).get(n - 1).equals("F")) {
                    int count = 0;
                    for (int j = n - 1; count != Integer.parseInt(row_constraints.get(i).get(0).toString()) - 1; j--){
                        if (board.get(j).get(i).equals("E"))
                        {
                            state.setIndexBoard(i, j, "F");
                            state.removeIndexDomain(i, j, "F");
                            state.removeIndexDomain(i, j, "X");
                        }
                        count++;
                    }
                }
                for (int j = 0; j < n; j++) {
                    if (board.get(j).get(i).equals("E")) {
                        state.setIndexBoard(i, j, "X");
                        state.removeIndexDomain(i, j, "X");
                        state.removeIndexDomain(i, j, "F");
                    }
                }
            }
        }
    }

    private void updateSingleConstraintColumnCheck(State state, int row, int column) {
        ArrayList<ArrayList<String>> board = state.getBoard();
        for (int i = 0; i < n; i++) {
            if (col_constraints.get(i).size() == 1) {
                int count = 0;
                if (board.get(0).get(i).equals("F")) {
                    for (int j = 0; count != Integer.parseInt(col_constraints.get(i).get(0).toString()) - 1; j++) {
                        if (board.get(i).get(j).equals("E"))
                        {
                            state.setIndexBoard(j, i, "F");
                            state.removeIndexDomain(j, i, "F");
                            state.removeIndexDomain(j, i, "X");
                        }
                        count++;
                    }
                }
                if (board.get(n-1).get(i).equals("F")){
                    for (int j = n - 1; count != Integer.parseInt(col_constraints.get(i).get(0).toString()) - 1; j--){
                        if (board.get(i).get(j).equals("E"))
                        {
                            state.setIndexBoard(j, i, "F");
                            state.removeIndexDomain(j, i, "F");
                            state.removeIndexDomain(j, i, "X");
                        }
                        count++;
                    }
                }
                for (int j = 0; j < n; j++) {
                    if (board.get(i).get(j).equals("E")) {
                            state.setIndexBoard(j, i, "X");
                            state.removeIndexDomain(j, i, "X");
                            state.removeIndexDomain(j, i, "F");
                    }
                }
            }
        }
    }


    private void updateUsingX(State state , int row, int column) {
        ArrayList<ArrayList<String>> board = state.getBoard();

        if (row < Collections.min(col_constraints.get(column)))
            for(int j = 0; j <= row; j++)
            {
                if (board.get(j).get(column).equals("E")) {
                    state.setIndexBoard(column, j, "X");
                    state.removeIndexDomain(column, j, "X");
                    state.removeIndexDomain(column, j, "F");
                }

            }


        if (column < Collections.min(row_constraints.get(row)))
            for(int i = 0; i <= column; i++)
            {
                if (board.get(row).get(i).equals("E")) {
                    state.setIndexBoard(i, row, "X");
                    state.removeIndexDomain(i, row, "X");
                    state.removeIndexDomain(i, row, "F");
                }

            }


        if (n - row < Collections.min(col_constraints.get(column)))
            for(int j = n - 1; j >= row; j--)
            {
                if (board.get(j).get(column).equals("E")) {
                    state.setIndexBoard(column, j, "X");
                    state.removeIndexDomain(column, j, "X");
                    state.removeIndexDomain(column, j, "F");
                }

            }


        if (n - column < Collections.min(row_constraints.get(row)))
            for(int i = n - 1; i >= column; i--)
            {
                if (board.get(row).get(i).equals("E")) {
                    state.setIndexBoard(i, row, "X");
                    state.removeIndexDomain(i, row, "X");
                    state.removeIndexDomain(i, row, "F");
                }
            }
    }

    private ArrayList<String> LCV (State state, int[] var) {
        ArrayList<String> strings = new ArrayList<>();
        ArrayList<ArrayList<ArrayList<String>>> cDomain;
        strings.add("X");
        strings.add("F");
        State newState = state.copy();
        newState.setIndexBoard(var[0], var[1], "F");
        newState.removeIndexDomain(var[0], var[1], "F");
        newState.removeIndexDomain(var[0], var[1], "X");
        updateBoardAndDomain(newState, var[0], var[1], "F");
        cDomain = newState.getDomain();
        int sum = 0;
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                sum += cDomain.get(i).get(j).size();                //if (cDomain.get(i).get(j).size() == 0)
        newState = state.copy();                                    //sum += 10;
        newState.setIndexBoard(var[0], var[1], "X");        //else if (cDomain.get(i).get(j).size() == 1)
        newState.removeIndexDomain(var[0], var[1], "X");    //sum++;
        newState.removeIndexDomain(var[0], var[1], "F");
        updateBoardAndDomain(newState, var[0], var[1], "X");
        cDomain = newState.getDomain();
        int sum_2 = 0;
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)                             //if (cDomain.get(i).get(j).size() == 0)
                sum_2 += cDomain.get(i).get(j).size();              //    sum_2 += 10;                                  /could be better
        if (sum > sum_2)                                              // else if (cDomain.get(i).get(j).size() == 1)
            Collections.sort(strings);                                 // sum_2++;

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
            label1:
            for (int j = 0; j < n; j++) {
                switch (cBoard.get(i).get(j)) {
                    case "F":
                        flag = true;
                        count++;
                        break;
                    case "E":
                        break label1;
                    case "X":
                        if (flag) {
                            flag = false;
                            if (!constraints.isEmpty()) {
                                if (count != constraints.peek()) {
                                    return false;
                                }
                                constraints.remove();
                            }
                            count = 0;
                        }
                        break;
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
            if (count_x > n - sum) {
                return false;
            }
            if (count_f != sum && count_e == 0) {
                return false;
            }

            Queue<Integer> constraints = new LinkedList<>(col_constraints.get(j));
            int count = 0;
            boolean flag = false;
            label:
            for (int i = 0; i < n; i++) {
                switch (cBoard.get(i).get(j)) {
                    case "F":
                        flag = true;
                        count++;
                        break;
                    case "E":
                        break label;
                    case "X":
                        if (flag) {
                            flag = false;
                            if (!constraints.isEmpty()) {
                                if (count != constraints.peek()) {
                                    return false;
                                }
                                constraints.remove();
                            }
                            count = 0;
                        }
                        break;
                }
            }
        }
        return true;
    }

    private boolean isFinished(State state) {
        return allAssigned(state) && isConsistent(state);
    }

}



/*if(j >= constraints.get(j))
                        for(int k = 0; k < j; k++)
                            if(board.get(k).get(j).equals("X"))
                                updateUsingX(state,k,j);

                    if(i >= constraints.get(j))
                        for(int k = 0; k < i; k++)
                            if(board.get(i).get(k).equals("X"))
                                updateUsingX(state,i,k);

                    if(n - j >= constraints.get(j))
                        for(int k = n - j; k < n;k++)
                            if(board.get(i).get(n-j).equals("X"))
                                updateUsingX(state,i,n - j);

                    if(n - i >= constraints.get(j))
                        for(int k = n - i; k < n; k++)
                            if(board.get(n - i).get(j).equals("X"))
                                updateUsingX(state,n - i, j);*/