package lld.DesignProblems.parkingLot;

import lld.DesignProblems.parkingLot.service.CommandService;

public class ParkingDriver {
    public void run(String[] args){
        CommandService commandService = new CommandService();

        //Handle input from file
        if(args.length>0) {
            commandService.parseFileInput(args[0]);
        }

        //Handle input from CLI
        commandService.parseCliInput();
    }
}
