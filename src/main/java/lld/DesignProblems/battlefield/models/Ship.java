package lld.DesignProblems.battlefield.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Ship {
    public String shipId;
    public int start_x, end_x;
    public int start_y, end_y;

    public Ship(String shipId, int start_x, int end_x, int start_y, int end_y){
        this.shipId = shipId;
        this.start_x = start_x;
        this.end_x = end_x;
        this.start_y = start_y;
        this.end_y = end_y;
    }
}
