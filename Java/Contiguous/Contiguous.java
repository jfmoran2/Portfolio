import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Contiguous {

    private static String reviewsInputFile = "//Users//johnmoran//Documents//GitHub//CS412//Programming//Contiguous//data//reviews_sample.txt";
    private static String patternsOutputFile = "//Users//johnmoran//Documents//GitHub//CS412//Programming//Contiguous//data//patterns.txt";

    private ArrayList<ItemSet> masterItemSet;
    private ArrayList<String> originalDatabase;

    private static double MINIMUM_SUPPORT_LEVEL = 0.01;

    private int minimumSupport;

    public Contiguous() {
        this.minimumSupport = 1;
        this.masterItemSet = new ArrayList<>();
        this.originalDatabase = new ArrayList<>();
    }

    private ArrayList<String> getOriginalReviews() {
        return this.originalDatabase;
    }

    private void addToMasterItemSet(ArrayList<ItemSet> list) {
        this.masterItemSet.addAll(list);

        return;
    }

    private int calculateMinimumSupport(int sizeOriginalData) {
        minimumSupport = (int) Math.floor((double) sizeOriginalData * MINIMUM_SUPPORT_LEVEL);
        return (minimumSupport);
    }

    private void readOriginalDatabase(String pathname) {

        try {
            File file = new File(pathname);

            BufferedReader reader = new BufferedReader(new FileReader(file));

            String line;
            while ((line = reader.readLine()) != null) {
                this.originalDatabase.add(line);
            }

        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return;
    }

    private void writeMasterItemSet(String pathname) {
        try {
            File file = new File(pathname);

            BufferedWriter writer = new BufferedWriter(new FileWriter(file));

            Iterator<ItemSet> iterator = masterItemSet.iterator();

            String separator = ";";

            while (iterator.hasNext()) {

                ItemSet itemset = iterator.next();
                Integer support = itemset.getSupport();

                String itemsAsString = itemset.getItemsAsString(separator);

                writer.write(support.toString());
                writer.write(":");

                writer.write(itemsAsString);

                writer.newLine();
            }

            writer.close();

        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return;
    }

    private ArrayList<ItemSet> getFrequentLengthOneItemSets(ArrayList<String> candidates) {
        Collections.sort(candidates);

        ArrayList<ItemSet> itemsetList = new ArrayList<>();

        int i = 0;

        while (i < candidates.size()) {
            String compareString = candidates.get(i);

            int count = Collections.frequency(candidates, compareString);

            if (count > minimumSupport) {
                ItemSet itemset = new ItemSet();
                itemset.addItem(compareString);
                itemset.setSupport(count);
                itemsetList.add(itemset);

                System.out.println(i + ": " + compareString + ", support = " + count);
            }
            i += count;
        }
        return (itemsetList);
    }


    private Boolean allStringsFound(ArrayList<String> candidateStrings, ArrayList<String> master) {

        boolean matched = false;
        int size = candidateStrings.size();

        int i = 0;
        int matchCount = 0;

        while (i < size) {
            for (int j = 0; j < master.size(); j++) {
                if (master.get(j).equals(candidateStrings.get(i))) {
                    matchCount++;
                }
            }
            i++;
        }

        if (matchCount == size)
            matched = true;

        return matched;
    }

    private Boolean allStringsFoundInOrder(ArrayList<String> candidateStrings, ArrayList<String> master) {

        boolean matched = false;
        int size = candidateStrings.size();

        int i = 0;
        int matchCount = 0;

        while (i < size) {
            for (int j = 0; j < master.size(); j++) {
                if (master.get(j).equals(candidateStrings.get(i))) {
                    matchCount++;
                }
            }
            i++;
        }

        if (matchCount == size)
            matched = true;

        return matched;
    }

    private ArrayList<ItemSet> calulateSupport(ArrayList<ItemSet> candidates, ArrayList<ItemSet> originalDatabase) {
        ArrayList<ItemSet> itemsetList = new ArrayList<>();


        for (ItemSet candidateItemSet : candidates) {
            // get the strings from the candidate ItemSet
            ArrayList<String> compareStrings = candidateItemSet.getItems();
            int count = 0;

            for (int i = 0; i < originalDatabase.size(); i++) {
                ItemSet origset = originalDatabase.get(i);

                Boolean found = false;
                found = allStringsFound(compareStrings, origset.getItems());

                if (found) {
                    count++;
                }
            }

            if (count > 0) {
                ItemSet itemset = new ItemSet();
                itemset.addItems(candidateItemSet.getItems());
                itemset.setSupport(count);

                itemsetList.add(itemset);
            }
        }
        return (itemsetList);
    }

    private ArrayList<ItemSet> generateLengthTwoCandidates(ArrayList<ItemSet> inputItems) {
        ArrayList<ItemSet> outputSets = new ArrayList<>();

        int i = 0;
        int j = 0;

        while (i < inputItems.size()) {
            j = 0;
            while (j < inputItems.size()) {
                //      if (i != j) {
                ItemSet itemSet1 = inputItems.get(i);
                ItemSet itemSet2 = inputItems.get(j);

                ArrayList<String> s1 = itemSet1.getItems();
                ArrayList<String> s2 = itemSet2.getItems();

                ItemSet out1 = new ItemSet();
                out1.addItems(s1);
                out1.addItems(s2);

                outputSets.add(out1);
                //  }
                j++;
            }
            i++;
        }
        return outputSets;
    }

    private ArrayList<ItemSet> generateLengthKCandidates(ArrayList<ItemSet> masterItems, ArrayList<ItemSet> inputItems) {
        ArrayList<ItemSet> outputSets = new ArrayList<>();

        int i = 0;
        int j = 0;

        while (i < masterItems.size()) {
            j = 0;
            while (j < inputItems.size()) {
                ItemSet itemSet1 = masterItems.get(i);
                ItemSet itemSet2 = inputItems.get(j);

                ArrayList<String> s1 = itemSet1.getItems();
                ArrayList<String> s2 = itemSet2.getItems();

                ItemSet out1 = new ItemSet();
                out1.addItems(s1);
                out1.addItems(s2);

                outputSets.add(out1);
                j++;
            }
            i++;
        }
        return outputSets;
    }


    private ArrayList<ItemSet> getCandidatesFromRange(ArrayList<ItemSet> inputItems, Integer startIndex, Integer endIndex) {
        ArrayList<ItemSet> outputSet = new ArrayList<>();

        for (int i = startIndex; i < endIndex; i++) {
            for (int j = startIndex + 1; j < endIndex; j++) {

                ItemSet itemSet1 = inputItems.get(i);
                ItemSet itemSet2 = inputItems.get(j);

                String s1 = itemSet1.getItems().get(0);
                String s2 = itemSet1.getItems().get(1);
                String s3 = itemSet2.getItems().get(1);

                // how does this happen??   Food;Restaurants;Restaurants and Nightlife;Sports Bars;Sports Bars for
                // examples
                if (!s2.equals(s3)) {
                    ItemSet out1 = new ItemSet();
                    out1.addItem(s1);
                    out1.addItem(s2);
                    out1.addItem(s3);

                    outputSet.add(out1);
                }
            }
            i++;
        }

        return outputSet;
    }

    private ArrayList<ItemSet> generateFreqThreeCandidates(ArrayList<ItemSet> inputItems) {
        ArrayList<ItemSet> outputSets = new ArrayList<>();

        int i = 0;

        Boolean isMoreData = true;
        Boolean foundAllFirstMatches = false;
        int numberOfItems = inputItems.size();

        String matchString = inputItems.get(0).getItems().get(0);

        // iterate through inputItems while match String matches
        Integer startIndex = 0;
        Integer endIndex = 1;

        while (isMoreData) {
            while (isMoreData && inputItems.get(endIndex).getItems().get(0).equals(matchString)) {
                // do something
                endIndex++;
                if (endIndex >= numberOfItems) {
                    isMoreData = false;
                }
            }

            // handle if there were first string matches
            if ((endIndex - startIndex) > 1) {
                ArrayList<ItemSet> temp = getCandidatesFromRange(inputItems, startIndex, endIndex);
                outputSets.addAll(temp);
            }

            // increment and do something else
            startIndex = endIndex;
            endIndex++;

            if (endIndex >= numberOfItems) {
                isMoreData = false;
            } else {
                matchString = inputItems.get(startIndex).getItems().get(0);
            }
        }
        return outputSets;
    }

    private static boolean isContain(String source, Pattern p) {
        Matcher m = p.matcher(source);
        return m.find();
    }

/*
    private static boolean isContain(String source, String subItem) {
        String pattern = "\\b" + subItem + "\\b";
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(source);
        return m.find();
    }
    */
    private ArrayList<ItemSet> getFrequentItemSets(ArrayList<String> originalDatabase, ArrayList<ItemSet> candidates) {
        ArrayList<ItemSet> outputList = new ArrayList<>();

        for (ItemSet candidateItemSet : candidates) {
            String compareString = candidateItemSet.getItemsAsString(" ");
            int count = 0;

            Iterator<String> dbIterator = originalDatabase.iterator();

            // COMPILE PATTERN before loop through DB
            // \\b is word boundary

            //String pattern = "\\b" + compareString + "\\b";
            //Pattern pattern = Pattern.compile("\\b" + compareString + "\\b");

            while (dbIterator.hasNext()) {
                String dbLine = dbIterator.next();

                if (dbLine.contains(compareString)) {
                    // check you found the entire string, not a substring
                    Integer len = compareString.length();
                    Integer startIndex = dbLine.indexOf(compareString);
                    Integer stopIndex = startIndex + len - 1;
                    Integer endOfDBStringPos = dbLine.length() - 1;
                    Boolean startIsOkay = false;
                    Boolean stopIsOkay = false;

                    //System.out.println("stopIndex: " + stopIndex + "endOfDBStringPos: " + endOfDBStringPos);
                    // check front for space or -1
                    // check case 1, you're at the beginning of the string, so start is okay
                    if (startIndex == 0) {
                        startIsOkay = true;
                    } else {
                        // safe to check previous character, since it can't be less than zero
                        startIsOkay = Character.isWhitespace(dbLine.charAt(startIndex - 1));
                    }

                    // no need to further check is start not okay
                    if (startIsOkay) {
                        // have to use equals because Integer is object!
                        if (stopIndex.equals(endOfDBStringPos)) {
                            stopIsOkay = true;
                        } else {
                            // safe to check next character
                                stopIsOkay = Character.isWhitespace(dbLine.charAt(stopIndex + 1));
                        }
                    }
                    if (startIsOkay && stopIsOkay) {
                        count++;
                    }
                }
            }

            if (count > minimumSupport) {
                ItemSet itemset = new ItemSet();
                itemset.addItems(candidateItemSet.getItems());
                itemset.setSupport(count);
                outputList.add(itemset);

                System.out.println(itemset.getSupport() + ":" + itemset.getItemsAsString(";"));
            }
        }

        return outputList;
    }

    public void display(ArrayList<ItemSet> inputItemSet) {
        Iterator<ItemSet> iterator = inputItemSet.iterator();

        while (iterator.hasNext()) {
            ItemSet itemset = iterator.next();

            System.out.print(itemset.getSupport() + ":");
            ArrayList<String> items;
            items = itemset.getItems();
            for (int i = 0; i < items.size(); i++) {
                System.out.print(items.get(i));

                if (i < (items.size() - 1)) {
                    System.out.print(';');
                }
            }
            System.out.println();
        }
    }


    private ArrayList<String> generateLengthOneCandidates(ArrayList<String> originalDatabase) {
        ArrayList<String> candidates = new ArrayList<>();

        Iterator<String> iterator = originalDatabase.iterator();

        while (iterator.hasNext()) {
            String line = iterator.next();
            ArrayList<String> temp = new ArrayList<>();
            temp.addAll(Arrays.asList(line.split("\\s+")));

            // hash to count only the first occurrence so if it is in one line many times, add it once
            HashSet hs = new HashSet();
            for (String s : temp) {
                hs.add(s);
            }
            ArrayList<String> temp2 = new ArrayList<>(hs);

            candidates.addAll(temp2);
        }
        return candidates;
    }


    private ArrayList<ItemSet> getOriginalItemSetDB(ArrayList<String> originalDatabase) {
        ArrayList<ItemSet> master = new ArrayList<>();

        Iterator<String> iterator = originalDatabase.iterator();

        while (iterator.hasNext()) {
            String line = iterator.next();
            ItemSet entry = new ItemSet();

            ArrayList<String> items = new ArrayList<String>(Arrays.asList(line.split("\\s+")));
            entry.addItems(items);

            master.add(entry);
        }
        return master;
    }


    public static void main(String[] args) {
        Contiguous contiguous = new Contiguous();
        ArrayList<String> originalReviews;
        ArrayList<ItemSet> originalItemSetDB;

        contiguous.readOriginalDatabase(reviewsInputFile);
        originalReviews = contiguous.getOriginalReviews();
        // originalItemSetDB = contiguous.getOriginalItemSetDB(originalReviews);

        contiguous.calculateMinimumSupport(originalReviews.size());

        System.out.println(originalReviews.size() + " database lines read.");

        ArrayList<String> lengthOneCandidates = contiguous.generateLengthOneCandidates(originalReviews);

        ArrayList<ItemSet> lengthOneItemSet = contiguous.getFrequentLengthOneItemSets(lengthOneCandidates);

        // ArrayList<ItemSet> lengthOneItemSet = contiguous.getFrequentItemSets(freqOneCandidateItemSets);

        contiguous.addToMasterItemSet(lengthOneItemSet);


        // contiguous.displayMasterItemSet();

        ArrayList<ItemSet> lengthTwoCandidates = contiguous.generateLengthTwoCandidates(lengthOneItemSet);


        System.out.println(lengthTwoCandidates.size() + " Length Two Candidates");
        ArrayList<ItemSet> lengthTwoItemSet = contiguous.getFrequentItemSets(originalReviews, lengthTwoCandidates);

        contiguous.addToMasterItemSet(lengthTwoItemSet);

        ArrayList<ItemSet> lengthThreeCandidates = contiguous.generateLengthKCandidates(lengthTwoItemSet, lengthOneItemSet);
        System.out.println("");
        System.out.println("************************************");
        System.out.println(lengthThreeCandidates.size() + " Length Three Candidates");

        ArrayList<ItemSet> lengthThreeItemSet = contiguous.getFrequentItemSets(originalReviews, lengthThreeCandidates);

        contiguous.addToMasterItemSet(lengthThreeItemSet);

        contiguous.writeMasterItemSet(contiguous.patternsOutputFile);
        /*
        // contiguous.displayItemSet(freqTwoCandidates);
        ArrayList<ItemSet> freqTwoCandidateItemSets = contiguous.calulateSupport(freqTwoCandidates, originalItemSetDB);

        ArrayList<ItemSet> freqTwoItemSet = contiguous.getFrequentItemSets(freqTwoCandidateItemSets);
        // contiguous.displayItemSet(freqTwoItemSet);
        System.out.println(freqTwoItemSet.size() + " Freq Two found");
        contiguous.addToMasterItemSet(freqTwoItemSet);

        ArrayList<ItemSet> freqThreeCandidates = contiguous.generateFreqThreeCandidates(freqTwoItemSet);
        ArrayList<ItemSet> freqThreeCandidateItemSets = contiguous.calulateSupport(freqThreeCandidates, originalItemSetDB);

        ArrayList<ItemSet> freqThreeItemSet = contiguous.getFrequentItemSets(freqThreeCandidateItemSets);
        contiguous.displayItemSet(freqThreeItemSet);  freqThreeItemSet.display();
        System.out.println(freqThreeItemSet.size() + " Freq Three found");
        contiguous.addToMasterItemSet(freqThreeItemSet);
        contiguous.writeMasterItemSet(contiguous.patternsOutputFile);
*/
        //   ArrayList<CategorySet> set;
        //   set = contiguous.buildItemSet(frequencyOneItemsetMinSup, k);

        //  int p = 0;
        // write out frequency 1 data to patterns.txt

        // next implement contiguous now that have freq one itemset
    }

}
