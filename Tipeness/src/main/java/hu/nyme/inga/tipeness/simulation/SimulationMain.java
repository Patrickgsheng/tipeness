/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.nyme.inga.tipeness.simulation;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.Arrays;

/**
 *
 * @author András Molnár
 */
public class SimulationMain {

    public static PrintWriter out;
    public static FileWriter fileWriter;
    static String inputFilePath;

    public static void main(String[] args) {
        //inputFilePath = args[0];     
        inputFilePath= "holzteamBeta.xml";
        SimulationUnit simUnit = new SimulationUnit(inputFilePath);
        try {
            fileWriter = new FileWriter(simUnit.getConfigParser().getOutFileNamePath(), true);
            out = new PrintWriter(fileWriter);
            out.print(simUnit.getConfigParser().outPetrinet());
            simUnit.runSimulation();
            out.print(simUnit.getMeasureUnitStatistic().outResults());
            out.close();
        } catch (IOException ioe) {
            System.out.println("IOException thrown!");
            System.out.println(Arrays.toString(ioe.getStackTrace()));
            System.exit(-1);
        }
    }
}
