package lld.DesignProblems.Oracle;

import java.util.LinkedList;
import java.util.Queue;

public class Dsa {

    /*
        1,0,0,0,1
        1,1,0,0,1
        0,0,1,0,0
        0,0,0,0,0
        0,0,1,0,0

        a->b
        b->c
        a->d

        dfs = a->b->c
               a->d;
        bfs = a->b
              a->d
              b->c

    */


    public static void bfs(int[][] grid, int row, int col, boolean[][]visited){
        int [][] dir = {{-1,0},{1,0},{0,-1},{0,1}};
        Queue<int[]> queue = new LinkedList<>();
        queue.offer(new int[]{row, col});
        visited[row][col] = true;

        while (!queue.isEmpty()){
            int[] curr = queue.poll();
            int r1 = curr[0];
            int c1 = curr[1];
            for(int [] d:dir) {
                int r = r1 + d[0];
                int c = c1 + d[1];
                if (r >= 0 && r < grid.length && c >= 0 && c < grid[0].length && grid[r][c] == 1 && !visited[r][c]) {
                    queue.offer(new int[]{r, c});
                    visited[r][c] = true;
                }
            }
        }
    }


    public static void dfs(int[][] grid, int row, int col, boolean[][] visted){
        int [][] dir = {{-1,0},{1,0},{0,-1},{0,1}};
        visted[row][col]=true;
        for(int[] d: dir){
            int r= row+ d[0];
            int c =col+d[1];
            if(r>=0 && r<grid.length && c>=0 && c<grid[0].length && grid[r][c]==1 && !visted[r][c]){
                dfs(grid,r,c,visted);
            }
        }
    }

    public static int Solve(int[][] grid){
        int rows = grid.length;
        int cols = grid[0].length;

        boolean[][] visted = new boolean[rows][cols];
        int ans =0;
        for(int i=0;i<rows;i++){
            for(int j=0;j<cols;j++){
                if(grid[i][j]==1 && !visted[i][j]){
//                    dfs(grid, i, j, visted);
                    bfs(grid, i, j, visted);
                    ans++;
                }
            }
        }
        return ans;
    }

    public static void main(String[] args) {
        int[][] arr= {{1,0,0,0,1},
                    {1,1,0,0,1},
                    {0,0,1,0,0},
                    {0,0,0,0,0},
                    {0,0,1,0,0}
                    };
        System.out.println(Solve(arr));

    }
}
