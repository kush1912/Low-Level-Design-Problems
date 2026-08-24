package lld.DesignProblems.bookmyshow.model;

import java.util.HashSet;
import java.util.Set;

public class Theatre {
    private final String name;
    private Set<Screen> screens;

    public Theatre(String name){
        this.name = name;
        this.screens = new HashSet<>();
    }

    public Set<Screen> getScreens(){
        return screens;
    }

    public Set<Screen> addScreen(Screen screen){
        screens.add(screen);
        return screens;
    }
}
