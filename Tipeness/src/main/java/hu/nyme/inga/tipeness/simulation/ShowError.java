package hu.nyme.inga.tipeness.simulation;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author András Molnár
 */
public class ShowError {

    public enum ErrorType {

        wrongInputFilePath, wrongTokenNum, wrongPlaceName, wrongTransName, wrongPriority,
        wrongArcWeight, wrongDelay, wrongIOPlaceName, wrongServerType, wrongSignPlace, nullSignPlace,
        wrongNumberOfN, wrongAlphaValue, wrongTransientAccuracy, wrongAccuracy, wrongBatchLength, wrongTokenPlace,
        wrongAvgTokenPlace, wrongDiffTokenPlace, wrongMemoryPolicy, wrongTransWeight, wrongMemoryType, wrongGammaParam,
        wrongDetParam, wrongNormDistParam, wrongTerminatingTime, wrongMethodType, wrongWarmupLength,
        noWatchAvgOrDiffPlaceAtBatch, noWatchTokenOrAvgPlaceAtRepDel, wrongFireCondition, wrongFirePolicyTransname
    };
    public ErrorType error;

    public static void showError(ErrorType error, boolean fatal) {
        switch (error) {
            case wrongTokenNum:
                System.out.println("The token number is not valid!");
                break;
            case wrongPlaceName:
                System.out.println("The 'placename' is not valid!");
                break;
            case wrongTransName:
                System.out.println("The 'transitionname' is not valid!");
                break;
            case wrongPriority:
                System.out.println("The given 'priority' is not valid!");
                break;
            case wrongArcWeight:
                System.out.println("The 'arcweight' is not valid!");
                break;
            case wrongDelay:
                System.out.println("The given 'delay' is not valid!");
                break;
            case wrongIOPlaceName:
                System.out.println("The name of an input/output place is not valid!");
                break;
            case wrongServerType:
                System.out.println("The 'servertype' is not valid!");
                break;
            case wrongSignPlace:
                System.out.println("The name of a significant place is not valid!");
                break;
            case wrongNumberOfN:
                System.out.println("The value of minimal number of batches/replications is not valid!");
                break;
            case wrongAlphaValue:
                System.out.println("The chosen confidence level is not valid!");
                break;
            case nullSignPlace:
                System.out.println("There is no significant place and no maximum runtime is given! This can result in infinite runtime!");
                break;
            case wrongTransientAccuracy:
                System.out.println("The value of transient accuracy is not valid!");
                break;
            case wrongAccuracy:
                System.out.println("The value of accuracy is not valid!");
                break;
            case wrongBatchLength:
                System.out.println("The given length of batches is not valid!");
                break;
            case wrongInputFilePath:
                System.out.println("The parameter file is not readable!");
                break;
            case wrongTokenPlace:
                System.out.println("The name of a place is not valid at tokenlist!");
                break;
            case wrongAvgTokenPlace:
                System.out.println("The name of a place is not valid at avgtokenlist!");
                break;
            case wrongDiffTokenPlace:
                System.out.println("The name of a place is not valid at difftokenlist!");
                break;
            case wrongMemoryPolicy:
                System.out.println("The memory policy can only be resampling, enabling memory or age memory!");
                break;
            case wrongFirePolicyTransname:
                System.out.println("A given transition name is not valid at the memory policy option!");
                break;
            case wrongTransWeight:
                System.out.println("The given transition weight must be a number greater than zero!");
                break;
            case wrongMemoryType:
                System.out.println("The given 'memorytype' is not valid!!");
                break;
            case wrongGammaParam:
                System.out.println("The given parameters for the gamma distribution are not valid!");
                break;
            case wrongDetParam:
                System.out.println("The given parameters for the deterministic distribution are not valid!");
                break;
            case wrongNormDistParam:
                System.out.println("The given parameters for the normal distribution are not valid!");
                break;
            case wrongTerminatingTime:
                System.out.println("The value of terminatingtime is not valid!");
                break;
            case wrongWarmupLength:
                System.out.println("The value of warmuplength is not valid!");
                break;
            case wrongMethodType:
                System.out.println("The desired method cannot be found!");
                break;
            case noWatchAvgOrDiffPlaceAtBatch:
                System.out.println("At least one place must be defined with 'avgtoken' or 'difftoken' tag if using batchMeans method!");
                break;
            case noWatchTokenOrAvgPlaceAtRepDel:
                System.out.println("At least one place must be defined with 'avgtoken' or 'token' tag if using batchMeans method!");
                break;
            case wrongFireCondition:
                System.out.println("The given condition can not be parsed!");
                break;
            default:
                System.out.println("Error during the filereading!");
                break;
        }
        if (fatal) {
            System.exit(-1);
        }
    }

    public static void showError(ErrorType error, boolean fatal, String where) {
        switch (error) {
            case wrongTokenNum:
                System.out.println("The token number is not valid at the " + where + " place!");
                break;
            case wrongPriority:
                System.out.println("The given priority is not valid at the " + where + " transition!");
                break;
            case wrongArcWeight:
                System.out.println("An arcweight is not valid at the " + where + " transition!");
                break;
            case wrongDelay:
                System.out.println("The given delay is not valid at the " + where + " transition!");
                break;
            case wrongIOPlaceName:
                System.out.println("The name of an input/output place is not valid at the " + where + " transition!");
                break;
            case wrongServerType:
                System.out.println("The servertype is not valid at the " + where + " transition!");
                break;
            case wrongTransName:
                System.out.println("The transitionname is not valid! (" + where + ")");
                break;
            case wrongTokenPlace:
                System.out.println("The name of the place is not valid at the watched tokenlist (" + where + ")!");
                break;
            case wrongAvgTokenPlace:
                System.out.println("The name of the place is not valid at the watched avgtokenlist (" + where + ")!");
                break;
            case wrongMemoryPolicy:
                System.out.println("Wrong memory policy is given at the " + where + " transition. The memory policy can only be resampling,"
                        + "enabling memory or age memory!");
                break;
            case wrongTransWeight:
                System.out.println("The given weight at the " + where + " transition must be a number greater than zero!");
                break;
            case wrongMemoryType:
                System.out.println("The given memorytype is not valid at the " + where + " transition!");
                break;
            case wrongGammaParam:
                System.out.println("The given parameters for the gamma distribution are not valid at the " + where + " transition!");
                break;
            case wrongDetParam:
                System.out.println("The given parameters for the deterministic distribution at the " + where + " transition are not valid!");
                break;
            case wrongNormDistParam:
                System.out.println("The given parameters for the normal distribution at the " + where + " transition are not valid!");
                break;
            case wrongFireCondition:
                System.out.println("The given condition can not be parsed at the " + where + " transition!");
                break;
            case wrongFirePolicyTransname:
                System.out.println("A given transition name at the " + where + " transition is not valid at the memory policy option!");
                break;
            default:
                System.out.println("Error during the filereading!");
                break;
        }
        if (fatal) {
            System.exit(-1);
        }
    }
}
