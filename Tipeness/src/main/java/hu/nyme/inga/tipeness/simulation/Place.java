/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.nyme.inga.tipeness.simulation;

import hu.nyme.inga.tipeness.statdata.StatValues;
import java.io.Serializable;

/**
 *
 * @author András Molnár
 */
public class Place implements Serializable {

    private final String name;
    private int currentToken;    

    public Place(String name) {
        this.name = name;        
    }

    public Place(String name, int current) {
        this.name = name;
        this.currentToken = current;        
    }

    public Place(String name, int current, StatValues stat) {
        this.name = name;
        this.currentToken = current;        
    }

    public String getName() {
        return name;
    }

    public int getCurrent() {
        return currentToken;
    }

    public void decreaseToken(int delta) {
        this.currentToken -= delta;
    }

    public void increaseToken(int delta) {
        this.currentToken += delta;
    }

    @Override
    public String toString() {
        String nl = System.lineSeparator();
        StringBuilder sb = new StringBuilder();
        sb.append("Placename: ").append(this.name).append(nl); 
        sb.append("Tokennumber: ").append(this.currentToken).append(nl);   
        sb.append("---------------------------------").append(nl);
        return sb.toString();
    }

    public Place copy() {
        return new Place(name, currentToken);
    }
}
