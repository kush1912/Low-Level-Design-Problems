package lld.DesignProblems.bookmyshow.model;

import java.util.ArrayList;
import java.util.List;

public class City {
    private String name;
    List<Theatre> theatres;

    public City(String name) {
        this.name = name;
        this.theatres = new ArrayList<>();
    }

    public List<Theatre> getTheatres(){
        return this.theatres;
    }

    public String getCiyName() {
        return name;
    }

    public List<Theatre> addTheatres(Theatre theatre){
        this.theatres.add(theatre);
        return this.theatres;
    }
}
