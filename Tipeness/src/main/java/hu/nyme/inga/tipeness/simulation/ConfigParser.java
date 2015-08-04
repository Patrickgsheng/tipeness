/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.nyme.inga.tipeness.simulation;

import hu.nyme.inga.tipeness.simulation.ConfigParser.MemoryPolicy;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import hu.nyme.inga.tipeness.statdata.MeasureUnitStatistic;
import hu.nyme.inga.tipeness.transitions.DelayedTransition;
import hu.nyme.inga.tipeness.transitions.DetTransition;
import hu.nyme.inga.tipeness.transitions.ExpTransition;
import hu.nyme.inga.tipeness.transitions.GammaTransition;
import hu.nyme.inga.tipeness.transitions.ImmedTransition;
import hu.nyme.inga.tipeness.transitions.TimedTransition;
import hu.nyme.inga.tipeness.transitions.Transition;
import hu.nyme.inga.tipeness.transitions.TruncNormalTransition;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.xml.sax.SAXException;

/**
 *
 * @author András Molnár
 */
public class ConfigParser {

    private HashMap<String, Place> places;
    private HashMap<String, ImmedTransition> immedTranstions;
    private HashMap<String, TimedTransition> memoryTransitions;

    public enum MemoryPolicy {
        resampling, enablingMemory, ageMemory
    };

    public static enum MethodType {
        repdel, batchmean, analysis
    };
    private MethodType mType;
    private double terminatingTime;
    private double warmupLength;
    private double alpha;
    private int minSampleSize;
    private double maxRelError;
    private int batch;
    private static String outFileNamePath;

    private HashMap<String, HashMap<String, MemoryPolicy>> memoryMatrix = new HashMap<>();

    private HashSet<String> watchTokenList = new HashSet<>();
    private HashSet<String> watchAvgTokenList = new HashSet<>();
    private HashSet<String> watchDiffTokenList = new HashSet<>();
    private HashSet<String> listTokenList = new HashSet<>();
    private HashSet<String> listAvgTokenList = new HashSet<>();
    private HashSet<String> listDiffTokenList = new HashSet<>();

    public ConfigParser(String paramFilePath) {
        places = new HashMap<>();
        immedTranstions = new HashMap<>();
        memoryTransitions = new HashMap<>();
        try {
            places = readPlaceParams(paramFilePath);
            immedTranstions = readImmediateTransitionParams(paramFilePath);
            memoryTransitions = readMemoryTransitions(paramFilePath);
            memoryMatrix = readMemoryPolicies(paramFilePath);
            readRunParams(paramFilePath);
        } catch (XPathExpressionException xe) {
            System.out.println("XPathExpressionExcetion thrown!");
            System.out.println(Arrays.toString(xe.getStackTrace()));
            System.exit(-1);
        } catch (SAXException se) {
            System.out.println("SAXException thrown!");
            System.out.println(Arrays.toString(se.getStackTrace()));
            System.exit(-1);
        } catch (ParserConfigurationException pe) {
            System.out.println("ParserConfigurationException thrown!");
            System.out.println(Arrays.toString(pe.getStackTrace()));
            System.exit(-1);
        } catch (IOException ie) {
            System.out.println("IOException thrown!");
            System.out.println(Arrays.toString(ie.getStackTrace()));
            System.exit(-1);
        }
    }

    private Transition readGeneralTransitionParams(Node file, XPath xpath) throws XPathExpressionException {        
        HashMap<String, Integer> inplaces = new HashMap<>();
        HashMap<String, Integer> inhibplaces = new HashMap<>();
        HashMap<String, Integer> outplaces = new HashMap<>();

        String transitionName= xpath.evaluate("name", file).toLowerCase();
        if (!isViableTransitionName(transitionName)) {
            ShowError.showError(ShowError.ErrorType.wrongTransName, true);
        }
        
        XPathExpression transitionInhibitorExpression = xpath.compile("inhibitor");
        NodeList inhibitorNodeList = (NodeList) transitionInhibitorExpression.evaluate(file, XPathConstants.NODESET);
        for (int j = 0; j < inhibitorNodeList.getLength(); j++) {
            Node inhibitorNode = inhibitorNodeList.item(j);

            String inhibitorName = xpath.evaluate("name", inhibitorNode).toLowerCase();
            int arcWeight;

            Place addPlace = getViablePlace(inhibplaces, inhibitorName);
            if (addPlace != null) {
                try {
                    arcWeight = Integer.parseInt(xpath.evaluate("arc", inhibitorNode));
                } catch (NumberFormatException ne) {
                    if (!xpath.evaluate("arc", inhibitorNode).equals("")) {
                        ShowError.showError(ShowError.ErrorType.wrongArcWeight, true, transitionName);
                    }
                    arcWeight = 1;
                }
                inhibplaces.put(addPlace.getName(), arcWeight);
            } else {
                ShowError.showError(ShowError.ErrorType.wrongIOPlaceName, true, transitionName);
            }
        }

        XPathExpression transitionInplaceExpression = xpath.compile("inplace");
        NodeList inplaceNodeList = (NodeList) transitionInplaceExpression.evaluate(file, XPathConstants.NODESET);
        for (int j = 0; j < inplaceNodeList.getLength(); j++) {
            Node inplaceNode = inplaceNodeList.item(j);
            int arcWeight;
            String inplaceName = xpath.evaluate("name", inplaceNode).toLowerCase();

            Place addPlace = getViablePlace(inplaces, inplaceName);
            if (addPlace != null) {
                try {
                    arcWeight = Integer.parseInt(xpath.evaluate("arc", inplaceNode));
                } catch (NumberFormatException ne) {
                    if (!xpath.evaluate("arc", inplaceNode).equals("")) {
                        ShowError.showError(ShowError.ErrorType.wrongArcWeight, true, transitionName);
                    }
                    arcWeight = 1;
                }
                inplaces.put(addPlace.getName(), arcWeight);
            } else {
                ShowError.showError(ShowError.ErrorType.wrongIOPlaceName, true, transitionName);
            }
        }

        XPathExpression transitionOutputPlaceExpression = xpath.compile("outplace");
        NodeList outputPlaceNodeList = (NodeList) transitionOutputPlaceExpression.evaluate(file, XPathConstants.NODESET);
        for (int j = 0; j < outputPlaceNodeList.getLength(); j++) {
            Node outputPlaceNode = outputPlaceNodeList.item(j);

            String outplaceName = xpath.evaluate("name", outputPlaceNode).toLowerCase();
            int arcWeight;

            Place addPlace = getViablePlace(outplaces, outplaceName);
            if (addPlace != null) {
                try {
                    arcWeight = Integer.parseInt(xpath.evaluate("arc", outputPlaceNode));
                } catch (NumberFormatException ne) {
                    if (!xpath.evaluate("arc", outputPlaceNode).equals("")) {
                        ShowError.showError(ShowError.ErrorType.wrongArcWeight, true, transitionName);
                    }
                    arcWeight = 1;
                }
                outplaces.put(addPlace.getName(), arcWeight);
            } else {
                ShowError.showError(ShowError.ErrorType.wrongIOPlaceName, true, transitionName);
            }
        }

        return new Transition(transitionName, inplaces, outplaces, inhibplaces);
    }

    private HashMap<String, ImmedTransition> readImmediateTransitionParams(String paramFilePath) throws XPathExpressionException,
            SAXException, IOException, ParserConfigurationException {
        HashMap<String, ImmedTransition> immediateTransitions = new HashMap<>();

        File parameterFile = new File(paramFilePath);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(parameterFile);
        XPath xpath = XPathFactory.newInstance().newXPath();
        XPathExpression readImmediateTransitionExpression = xpath.compile("//petrinet/immedtransition");
        NodeList immediateTransitionNodes = (NodeList) readImmediateTransitionExpression.evaluate(doc, XPathConstants.NODESET);

        for (int i = 0; i < immediateTransitionNodes.getLength(); i++) {
            String conString = "";
            double priority;
            double weight;
            Node immediateTransitionNode = immediateTransitionNodes.item(i);
            Transition generalTrans = readGeneralTransitionParams(immediateTransitionNode, xpath);

            try {
                priority = Double.parseDouble(xpath.evaluate("priority", immediateTransitionNode));
            } catch (Exception e) {
                if (!xpath.evaluate("priority", immediateTransitionNode).equals("")) {
                    ShowError.showError(ShowError.ErrorType.wrongPriority, false, generalTrans.getName());
                }
                priority = 1;
            }

            try {
                weight = Double.parseDouble(xpath.evaluate("weight", immediateTransitionNode));
            } catch (Exception e) {
                if (!xpath.evaluate("weight", immediateTransitionNode).equals("")) {
                    ShowError.showError(ShowError.ErrorType.wrongTransWeight, true, generalTrans.getName());
                }
                weight = 1;
            }
            try {
                conString = xpath.evaluate("condition", immediateTransitionNode).toLowerCase();
                if (conString.equals("")) {
                    immediateTransitions.put(generalTrans.getName(), new ImmedTransition(generalTrans.getName(), priority, weight, generalTrans.getInput(), generalTrans.getOutput(), generalTrans.getInhibitor()));

                } else {
                    immediateTransitions.put(generalTrans.getName(), new ImmedTransition(generalTrans.getName(), priority, weight, generalTrans.getInput(), generalTrans.getOutput(), generalTrans.getInhibitor(), conString));
                }
            } catch (Exception e) {
                if (!conString.equals("")) {
                    ShowError.showError(ShowError.ErrorType.wrongPriority, false, generalTrans.getName());
                }
            }
        }
        return immediateTransitions;
    }

    private HashMap<String, Place> readPlaceParams(String paramFilePath) throws XPathExpressionException,
            ParserConfigurationException, SAXException, IOException {

        HashMap<String, Place> readPlaces= new HashMap<>();
        File parameterFile = new File(paramFilePath);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(parameterFile);
        XPath xpath = XPathFactory.newInstance().newXPath();
        XPathExpression readPlaceExpression = xpath.compile("//petrinet/place");
        NodeList placeNodeList = (NodeList) readPlaceExpression.evaluate(doc, XPathConstants.NODESET);

        for (int i = 0; i < placeNodeList.getLength(); i++) {            
            Node placeNode = placeNodeList.item(i);
            int initialToken;

            String placeName=xpath.evaluate("name", placeNode).toLowerCase();
            if (!isViablePlaceName(placeName)) {
                ShowError.showError(ShowError.ErrorType.wrongPlaceName, true, placeName);
            }
            try {
                initialToken = Integer.parseInt(xpath.evaluate("token", placeNode));
            } catch (NumberFormatException ne) {
                if (!xpath.evaluate("token", placeNode).equals("")) {
                    ShowError.showError(ShowError.ErrorType.wrongTokenNum, false, placeName);
                }
                initialToken = 0;
            }
            readPlaces.put(placeName, new Place(placeName, initialToken));

        }
        return readPlaces;
    }

    private HashMap<String, TimedTransition> readMemoryTransitions(String paramFilePath) throws XPathExpressionException,
            ParserConfigurationException, SAXException, IOException {

        
        HashMap<String, TimedTransition> readMemoryTransitions= new HashMap<>();
        File parameterFile = new File(paramFilePath);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(parameterFile);
        XPath xpath = XPathFactory.newInstance().newXPath();

        XPathExpression readExponentialTransitionExpression = xpath.compile("//petrinet/exptransition");
        NodeList exponentialTransitionNodeList = (NodeList) readExponentialTransitionExpression.evaluate(doc, XPathConstants.NODESET);

        for (int i = 0; i < exponentialTransitionNodeList.getLength(); i++) {
            Node memoryTransitionNode = exponentialTransitionNodeList.item(i);
            ExpTransition exponentialTransition = readGeneralExpTransition(memoryTransitionNode, xpath);
            readMemoryTransitions.put(exponentialTransition.getName(), exponentialTransition);
        }

        XPathExpression readDeterministicTransitionExpression = xpath.compile("//petrinet/dettransition");
        NodeList deterministicTransitionNodeList = (NodeList) readDeterministicTransitionExpression.evaluate(doc, XPathConstants.NODESET);

        for (int i = 0; i < deterministicTransitionNodeList.getLength(); i++) {
            Node deterministicTransitionNode = deterministicTransitionNodeList.item(i);
            DelayedTransition deterministicTransition = readGeneralDelayedTransition(deterministicTransitionNode, xpath);
            readMemoryTransitions.put(deterministicTransition.getName(), new DetTransition(deterministicTransition.getName(), deterministicTransition.getDelay(),
                    deterministicTransition.getInput(), deterministicTransition.getOutput(), deterministicTransition.getInhibitor()));
        }

        XPathExpression readGammaTransitionExpression = xpath.compile("//petrinet/gammatransition");
        NodeList gammaTransitionNodeList = (NodeList) readGammaTransitionExpression.evaluate(doc, XPathConstants.NODESET);

        for (int i = 0; i < gammaTransitionNodeList.getLength(); i++) {

            Node gammaTransitionNode = gammaTransitionNodeList.item(i);
            GammaTransition gammaTransition = readGeneralGammaTransition(gammaTransitionNode, xpath);
            readMemoryTransitions.put(gammaTransition.getName(), gammaTransition);
        }

        XPathExpression readNormalTransitionExpression = xpath.compile("//petrinet/normaltransition");
        NodeList normalTransitionNodeList = (NodeList) readNormalTransitionExpression.evaluate(doc, XPathConstants.NODESET);

        for (int i = 0; i < normalTransitionNodeList.getLength(); i++) {
            Node normalTransitionNode = normalTransitionNodeList.item(i);
            TruncNormalTransition genTrans = readGeneralNormalTransition(normalTransitionNode, xpath);
            readMemoryTransitions.put(genTrans.getName(), genTrans);
        }

        return readMemoryTransitions;

    }

    private HashMap<String, HashMap<String, MemoryPolicy>> readMemoryPolicies(String paramFilePath) throws XPathExpressionException,
            SAXException, ParserConfigurationException, IOException {

        File parameterFile = new File(paramFilePath);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(parameterFile);

        memoryMatrix.putAll(readMemoryPoliciesByTransitionType("immedtransition", doc));
        memoryMatrix.putAll(readMemoryPoliciesByTransitionType("exptransition", doc));
        memoryMatrix.putAll(readMemoryPoliciesByTransitionType("dettransition", doc));
        memoryMatrix.putAll(readMemoryPoliciesByTransitionType("gammatransition", doc));
        memoryMatrix.putAll(readMemoryPoliciesByTransitionType("normaltransition", doc));

        return memoryMatrix;
    }

    private HashMap<String, HashMap<String, MemoryPolicy>> readMemoryPoliciesByTransitionType(String transType, Document doc) throws XPathExpressionException {
        HashMap<String, HashMap<String, MemoryPolicy>> transitionTypeMemoryMatrix = new HashMap<>();
        XPath xpath = XPathFactory.newInstance().newXPath();
        XPathExpression transitionTypeMemoryMatrixExpression = xpath.compile("//petrinet/" + transType);
        NodeList transitionNodeList = (NodeList) transitionTypeMemoryMatrixExpression.evaluate(doc, XPathConstants.NODESET);

        for (int i = 0; i < transitionNodeList.getLength(); i++) {
            Node transitionNode = transitionNodeList.item(i);
            String transName = xpath.evaluate("name", transitionNode).toLowerCase();
            transitionTypeMemoryMatrix.put(transName, readMemoryPolicyAtTransition(transitionNode, xpath));
        }
        return transitionTypeMemoryMatrix;
    }

    private HashMap<String, MemoryPolicy> readMemoryPolicyAtTransition(Node transitionNode, XPath xpath) throws XPathExpressionException {
        HashMap<String, MemoryPolicy> memoryPoliciesAtTransition = new HashMap<>();

        String transitionName = xpath.evaluate("name", transitionNode).toLowerCase();

        XPathExpression memoryTransitionsExpression = xpath.compile("memory");
        NodeList memoryTransitionNodeList = (NodeList) memoryTransitionsExpression.evaluate(transitionNode, XPathConstants.NODESET);
        for (int j = 0; j < memoryTransitionNodeList.getLength(); j++) {
            Node memoryTransitionNode = memoryTransitionNodeList.item(j);
            String memoryTransName = xpath.evaluate("name", memoryTransitionNode).toLowerCase();
            if (memoryTransitions.containsKey(memoryTransName)) {
                String serverTypeString = xpath.evaluate("policy", memoryTransitionNode);
                switch (serverTypeString) {
                    case "resampling":
                        memoryPoliciesAtTransition.put(memoryTransName, MemoryPolicy.resampling);
                        break;
                    case "enabling":
                        memoryPoliciesAtTransition.put(memoryTransName, MemoryPolicy.enablingMemory);
                        break;
                    case "age":
                        memoryPoliciesAtTransition.put(memoryTransName, MemoryPolicy.ageMemory);
                        break;
                    default:
                        ShowError.showError(ShowError.ErrorType.wrongMemoryPolicy, true, transitionName);
                        break;
                }
            } else {
                ShowError.showError(ShowError.ErrorType.wrongMemoryPolicy, true, transitionName);
            }

        }
        for (String delayedTransName : memoryTransitions.keySet()) {
            if (!memoryPoliciesAtTransition.containsKey(delayedTransName)) {
                memoryPoliciesAtTransition.put(delayedTransName, MemoryPolicy.enablingMemory);
            }
        }
        return memoryPoliciesAtTransition;
    }

    private GammaTransition readGeneralGammaTransition(Node file, XPath xpath) throws XPathExpressionException {
        Transition transition = readGeneralTransitionParams(file, xpath);
        double shape = 0.0;
        double rate = 0.0;
        try {
            shape = Double.parseDouble(xpath.evaluate("shape", file));
        } catch (Exception e) {
            ShowError.showError(ShowError.ErrorType.wrongGammaParam, true, transition.getName());
        }
        try {
            rate = Double.parseDouble(xpath.evaluate("rate", file));
        } catch (Exception e) {
            ShowError.showError(ShowError.ErrorType.wrongGammaParam, true, transition.getName());
        }
        return new GammaTransition(transition.getName(), shape, rate, transition.getInput(), transition.getOutput(), transition.getInhibitor());
    }

    private TruncNormalTransition readGeneralNormalTransition(Node file, XPath xpath) throws XPathExpressionException {
        double mean = 0.0;
        double variance = 0.0;

        Transition transition = readGeneralTransitionParams(file, xpath);
        String meanString = xpath.evaluate("mean", file);
        String varianceString = xpath.evaluate("variance", file);
        try {
            mean = Double.parseDouble(meanString);
            variance = Double.parseDouble(varianceString);

            if (mean <= 0) {
                ShowError.showError(ShowError.ErrorType.wrongNormDistParam, true, transition.getName());
            }
        } catch (Exception e) {
            ShowError.showError(ShowError.ErrorType.wrongNormDistParam, true, transition.getName());
        }
        return new TruncNormalTransition(transition.getName(), mean, variance, transition.getInput(), transition.getOutput(), transition.getInhibitor());
    }

    private DelayedTransition readGeneralDelayedTransition(Node file, XPath xpath) throws XPathExpressionException {

        double delay = 0.0;
        Transition transition = readGeneralTransitionParams(file, xpath);
        try {
            delay = Double.parseDouble(xpath.evaluate("delay", file));
            if (delay <= 0) {
                ShowError.showError(ShowError.ErrorType.wrongDelay, true, transition.getName());
            }
        } catch (Exception e) {
            ShowError.showError(ShowError.ErrorType.wrongDelay, true, transition.getName());
        }
        return new DelayedTransition(transition.getName(), delay, transition.getInput(), transition.getOutput(), transition.getInhibitor());
    }

    private ExpTransition readGeneralExpTransition(Node file, XPath xpath) throws XPathExpressionException {
        DelayedTransition delayedTransition = readGeneralDelayedTransition(file, xpath);
        ExpTransition.ServerType serverType = ExpTransition.ServerType.exclusive;
        String serverTypeString = xpath.evaluate("servertype", file);
        switch (serverTypeString) {
            case "infinite":
                serverType = ExpTransition.ServerType.infinite;
                break;
            case "exclusive":
                serverType = ExpTransition.ServerType.exclusive;
                break;
            case "":
                serverType = ExpTransition.ServerType.exclusive;
                break;
            default:
                ShowError.showError(ShowError.ErrorType.wrongServerType, false, delayedTransition.getName());
                break;
        }
        return new ExpTransition(delayedTransition.getName(), delayedTransition.getDelay(), serverType, delayedTransition.getInput(),
                delayedTransition.getOutput(), delayedTransition.getInhibitor());
    }

    private void readRunParams(String paramFilePath) throws XPathExpressionException, SAXException, IOException, ParserConfigurationException {
        File parameterFile = new File(paramFilePath);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(parameterFile);
        XPath xpath = XPathFactory.newInstance().newXPath();

        try {
            minSampleSize = Integer.parseInt(xpath.evaluate("//system/minsamplesize", doc));
        } catch (NumberFormatException ne) {
            if (!xpath.evaluate("//system/minsamplesize", doc).equals("")) {
                ShowError.showError(ShowError.ErrorType.wrongMinSampleSize, true);
            }
            minSampleSize = 30;
        }
        try {
            alpha = 1 - Double.parseDouble(xpath.evaluate("//system/confidencelevel", doc));
            if (alpha < 0 || alpha > 1) {
                ShowError.showError(ShowError.ErrorType.wrongAlphaValue, true);
            }
        } catch (Exception e) {
            if (!xpath.evaluate("//system/confidencelevel", doc).equals("")) {
                ShowError.showError(ShowError.ErrorType.wrongAlphaValue, true);
            }
        }
        watchAvgTokenList = readTokenList(doc, xpath, "avgtoken");
        listAvgTokenList = readTokenList(doc, xpath, "listavgtoken");
        listAvgTokenList.addAll(watchAvgTokenList);
        String methodTypeString = xpath.evaluate("//system/method", doc);
        if (methodTypeString.equals("") || methodTypeString.equals("batchmean") || methodTypeString.equals("batch")) {
            mType = MethodType.batchmean;
            watchDiffTokenList = readTokenList(doc, xpath, "difftoken");
            listDiffTokenList = readTokenList(doc, xpath, "listdifftoken");
            listDiffTokenList.addAll(watchDiffTokenList);
            listAvgTokenList.addAll(listDiffTokenList);

            try {
                batch = Integer.parseInt(xpath.evaluate("//system/batch", doc));
            } catch (NumberFormatException ne) {
                ShowError.showError(ShowError.ErrorType.wrongBatchLength, true);
            }
            if (watchAvgTokenList.isEmpty() && watchDiffTokenList.isEmpty()) {
                ShowError.showError(ShowError.ErrorType.noWatchAvgOrDiffPlaceAtBatch, true);
            }
        } else if (methodTypeString.equals("repdel")) {
            mType = MethodType.repdel;
            watchTokenList = readTokenList(doc, xpath, "token");
            listTokenList = readTokenList(doc, xpath, "listtoken");
            listTokenList.addAll(watchTokenList);

            try {
                terminatingTime = Double.parseDouble(xpath.evaluate("//system/terminatingtime", doc));
            } catch (NumberFormatException ne) {
                ShowError.showError(ShowError.ErrorType.wrongTerminatingTime, true);
            }
            if (watchAvgTokenList.isEmpty() && watchTokenList.isEmpty()) {
                ShowError.showError(ShowError.ErrorType.noWatchTokenOrAvgPlaceAtRepDel, true);
            }
        } else if (methodTypeString.equals("analysis")) {
            mType = MethodType.analysis;
            for (String placeName : places.keySet()) {
                watchDiffTokenList.add(placeName);
                watchAvgTokenList.add(placeName);
            }
            try {
                batch = Integer.parseInt(xpath.evaluate("//system/batch", doc));
            } catch (NumberFormatException ne) {
                ShowError.showError(ShowError.ErrorType.wrongBatchLength, true);
            }

            listAvgTokenList.addAll(watchAvgTokenList);
            listDiffTokenList.addAll(watchDiffTokenList);
        } else {
            ShowError.showError(ShowError.ErrorType.wrongMethodType, true);
        }
        try {
            warmupLength = Double.parseDouble(xpath.evaluate("//system/warmuplength", doc));
        } catch (NumberFormatException ne) {
            if (xpath.evaluate("//system/warmuplength", doc).equals("")) {
                warmupLength = 0;
            } else {
                ShowError.showError(ShowError.ErrorType.wrongWarmupLength, true);
            }
        }
        outFileNamePath = xpath.evaluate("//system/outfilepath", doc);
        if (outFileNamePath.equals("")) {
            outFileNamePath = System.getProperty("user.dir") + "/" + SimulationMain.inputFilePath.replace(".xml", ".txt");
        }
        try {
            maxRelError = Double.parseDouble(xpath.evaluate("//system/maxrelerror", doc));
            if (maxRelError < 0) {
                ShowError.showError(ShowError.ErrorType.wrongRelErrorValue, true);
            }

        } catch (NumberFormatException ne) {
            ShowError.showError(ShowError.ErrorType.wrongRelErrorValue, true);
        }
    }

    private HashSet<String> readTokenList(Node doc, XPath xpath, String watchType) throws XPathExpressionException {
        XPathExpression tokenNumExpression = xpath.compile("//system/" + watchType);
        NodeList tokenNumNodeList = (NodeList) tokenNumExpression.evaluate(doc, XPathConstants.NODESET);
        HashSet<String> tokenList = new HashSet<>();
        for (int i = 0; i < tokenNumNodeList.getLength(); i++) {
            Node file = tokenNumNodeList.item(i);
            String tokenNumName = file.getTextContent();
            if (places.containsKey(tokenNumName)) {
                tokenList.add(tokenNumName);
            } else {
                ShowError.showError(ShowError.ErrorType.wrongTokenPlace, false, tokenNumName);
            }
        }
        return tokenList;
    }

    private boolean isViablePlaceName(String placeName) {
        if (placeName == null || placeName.equals("")) {
            return false;
        }
        return !places.containsKey(placeName);
    }

    private boolean isViableTransitionName(String transitionName) {
        if (transitionName == null || transitionName.equals("")) {
            return false;
        }
        if (!immedTranstions.isEmpty() && immedTranstions.containsKey(transitionName)) {
            return false;
        }
        if (!memoryTransitions.isEmpty() && memoryTransitions.containsKey(transitionName)) {
            return false;
        }
        return true;
    }

    private boolean containsTransition(String transitionName) {
        if (immedTranstions.containsKey(transitionName)) {
            return true;
        } else if (memoryTransitions.containsKey(transitionName)) {
            return true;
        }
        return false;
    }

    private Place getViablePlace(HashMap<String, Integer> ioList, String placeName) {
        if (ioList.containsKey(placeName)) {
            return null;
        }
        if (!places.containsKey(placeName)) {
            return null;
        } else {
            return places.get(placeName);
        }
    }

    public String outPetrinet() {
        String nl = System.lineSeparator();
        StringBuilder sb = new StringBuilder();
        sb.append("PETRI NET:").append(nl).append(nl);
        sb.append("Places:").append(nl);
        sb.append(nl);
        for (String placeName : getPlaces().keySet()) {
            sb.append(getPlaces().get(placeName).toString());            
        }
        sb.append(nl);
        sb.append("Transitions:").append(nl);
        sb.append(nl);
        for (String immediateTransitionName : getImmedTranstions().keySet()) {
            sb.append(getImmedTranstions().get(immediateTransitionName).toString());
        }
        sb.append(nl);
        for (String memoryTransitionName : getMemoryTransitions().keySet()) {
            sb.append(getMemoryTransitions().get(memoryTransitionName).toString());
        }
        sb.append(nl);
        sb.append("MEMORY POLICIES:").append(nl).append(nl);
        sb.append("The default memory policy(enabling memory) is not listed!").append(nl).append(nl);
        for (String transitionName : memoryMatrix.keySet()) {
            ArrayList<String> notEnablingMemoryPolicies = new ArrayList<>();
            HashMap<String, MemoryPolicy> memoryPoliciesAtTransition = memoryMatrix.get(transitionName);

            for (String affectedTransitionName : memoryPoliciesAtTransition.keySet()) {
                if (memoryPoliciesAtTransition.get(affectedTransitionName) != MemoryPolicy.enablingMemory) {
                    notEnablingMemoryPolicies.add(affectedTransitionName);
                }
            }

            if (!notEnablingMemoryPolicies.isEmpty()) {
                sb.append("Defined memory policies at ").append(transitionName).append(nl);
                for (String affectedTransitionName : notEnablingMemoryPolicies) {
                    sb.append(affectedTransitionName).append(": ").append(memoryPoliciesAtTransition.get(affectedTransitionName))
                            .append(nl);
                }
                sb.append("----------------------------------").append(nl);
            }             
            
        }        
        sb.append("----------------------------------").append(nl).append(nl);
        return sb.toString();
    }

    public String outSystemParams(MeasureUnitStatistic mUnitStat) {
        String nl = System.lineSeparator();
        StringBuilder sb = new StringBuilder();
        sb.append("SIMULATION PARAMETERS:").append(nl).append(nl);
        sb.append("Method: ");
        double sumTime = 0.0;
        String numberOfNString = new String();
        if (mType == MethodType.repdel) {
            sb.append("Replication/deletion").append(nl);
            sb.append("Terminating time: ").append(terminatingTime).append(nl);
            numberOfNString = "Number of replications: " + Integer.toString(mUnitStat.numberOfN);
            sumTime = mUnitStat.sumLength;
        } else if (mType == MethodType.batchmean) {
            sb.append("Batch means").append(nl);
            sb.append("Batch length: ").append(batch).append(nl);
            numberOfNString = "Number of batches: " + Integer.toString(mUnitStat.numberOfN);
            sumTime = mUnitStat.sumLength;
        }
        sb.append("Minimal sample size: ").append(minSampleSize).append(nl);
        sb.append("Warmup time: ").append(warmupLength).append(nl);
        sb.append("Accuracy: ").append(maxRelError).append(nl);
        sb.append("Confidencelevel: ").append(1 - alpha).append(nl);
        sb.append("Runtime: ").append(sumTime).append(nl);
        sb.append(numberOfNString).append(nl);
        sb.append("----------------------------------").append(nl);
        
        return sb.toString();
    }

    public HashMap<String, Place> getPlaces() {
        return places;
    }

    public HashMap<String, ImmedTransition> getImmedTranstions() {
        return immedTranstions;
    }

    public HashMap<String, TimedTransition> getMemoryTransitions() {
        return memoryTransitions;
    }

    public MethodType getmType() {
        return mType;
    }

    public double getTerminatingTime() {
        return terminatingTime;
    }

    public double getWarmupLength() {
        return warmupLength;
    }

    public double getAlpha() {
        return alpha;
    }

    public int getMinSampleSize() {
        return minSampleSize;
    }

    public double getMaxRelError() {
        return maxRelError;
    }

    public int getBatch() {
        return batch;
    }

    public String getOutFileNamePath() {
        return outFileNamePath;
    }

    public HashMap<String, HashMap<String, MemoryPolicy>> getMemoryMatrix() {
        return memoryMatrix;
    }

    public HashSet<String> getWatchTokenList() {
        return watchTokenList;
    }

    public HashSet<String> getWatchAvgTokenList() {
        return watchAvgTokenList;
    }

    public HashSet<String> getWatchDiffTokenList() {
        return watchDiffTokenList;
    }

    public HashSet<String> getListTokenList() {
        return listTokenList;
    }

    public HashSet<String> getListAvgTokenList() {
        return listAvgTokenList;
    }

    public HashSet<String> getListDiffTokenList() {
        return listDiffTokenList;
    }
}
