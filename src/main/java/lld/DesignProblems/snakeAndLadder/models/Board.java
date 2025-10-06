package lld.DesignProblems.snakeAndLadder.models;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
public class Board {
    private Integer size;
//    private List<Integer> board = new ArrayList<>();
    private int[] board= new int[size];
    Board(Integer size){
        this.size=size;
        for (int i = 0; i < size; i++) {
            board[i]=i;
        }
    }
}
