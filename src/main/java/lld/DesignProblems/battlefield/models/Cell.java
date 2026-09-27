package lld.DesignProblems.battlefield.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Cell {
    public int x, y;
    public Ship ship;
    public int territory;
    Cell(int x, int y, int p){
        this.x = x;
        this.y = y;
        this.territory = p;
    }
}
