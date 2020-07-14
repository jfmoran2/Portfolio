import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

public class Apriori {

    private static String itemsetInputFile = "//Users//johnmoran//Documents//GitHub//CS412//Programming//Apriori//data//categories.txt";
    private static String frequentOneOutputFile = "//Users//johnmoran//Documents//GitHub//CS412//Programming//Apriori//data//part1_patterns.txt";
    private static String allFrequentsOutputFile = "//Users//johnmoran//Documents//GitHub//CS412//Programming//Apriori//data//part2_patterns.txt";
    private ArrayList<ItemSet> masterItemSet;
    private ArrayList<String> originalDatabase;

    private static double MINIMUM_SUPPORT_LEVEL = 0.01;

    private int minimumSupport;

    public Apriori() {
        this.minimumSupport = 1;
        this.masterItemSet = new ArrayList<>();
        this.originalDatabase = new ArrayList<>();
    }

    private ArrayList<String> getOriginalRawDB() {
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

            while (iterator.hasNext()) {

                ItemSet itemset = iterator.next();
                Integer support = itemset.getSupport();
                ArrayList<String> items = itemset.getItems();

                writer.write(support.toString());
                writer.write(":");

                for (int i = 0; i < items.size(); i++) {
                    writer.write(items.get(i));

                    if (i < (items.size() - 1)) {
                        writer.write(';');
                    }
                }

                writer.newLine();
            }

            writer.close();

        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return;
    }

    private ArrayList<ItemSet> getFrequentItemSets(ArrayList<ItemSet> candidateItemSets) {
        ArrayList<ItemSet> frequentItems = new ArrayList<>();

        Iterator<ItemSet> iterator = candidateItemSets.iterator();

        while (iterator.hasNext()) {
            ItemSet candidateItemSet = iterator.next();
            if (candidateItemSet.getSupport() > minimumSupport) {
                frequentItems.add(candidateItemSet);
            }
        }

        return (frequentItems);
    }


    private ArrayList<ItemSet> calulateSupportFreqOne(ArrayList<String> candidates) {
        Collections.sort(candidates);

        ArrayList<ItemSet> itemsetList = new ArrayList<>();
        ArrayList<String> parsedItemStrings = new ArrayList<>();

        int i = 0;

        while (i < candidates.size()) {
            String compareString = candidates.get(i);
            if (!parsedItemStrings.contains(compareString)) {
                int count = Collections.frequency(candidates, compareString);

                ItemSet itemset = new ItemSet();
                itemset.addItem(compareString);
                itemset.setSupport(count);
                itemsetList.add(itemset);
                parsedItemStrings.add(compareString);
            }

            i++;
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

    private ArrayList<ItemSet> generateFreqTwoCandidates(ArrayList<ItemSet> inputItems) {
        ArrayList<ItemSet> outputSets = new ArrayList<>();

        int i = 0;

        while (i < inputItems.size()) {
            for (int j = i + 1; j < inputItems.size(); j++) {

                ItemSet itemSet1 = inputItems.get(i);
                ItemSet itemSet2 = inputItems.get(j);

                ArrayList<String> s1 = itemSet1.getItems();
                ArrayList<String> s2 = itemSet2.getItems();

                ItemSet out1 = new ItemSet();
                out1.addItems(s1);
                out1.addItems(s2);

                outputSets.add(out1);
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
            while (isMoreData && inputItems.get(endIndex).getItems().get(0).equals(matchString))
            {
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

    private ArrayList<String> generateFreqOneCandidates(ArrayList<String> originalDatabase) {
        ArrayList<String> candidates = new ArrayList<>();

        Iterator<String> iterator = originalDatabase.iterator();

        while (iterator.hasNext()) {
            String line = iterator.next();

            candidates.addAll(Arrays.asList(line.split(";")));
        }
        return candidates;
    }

    public void displayItemSet(ArrayList<ItemSet> inputItemSet) {
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

    public void displayMasterItemSet() {
        displayItemSet(masterItemSet);
    }

    private ArrayList<ItemSet> getOriginalItemSetDB(ArrayList<String> originalDatabase) {
        ArrayList<ItemSet> master = new ArrayList<>();

        Iterator<String> iterator = originalDatabase.iterator();

        while (iterator.hasNext()) {
            String line = iterator.next();
            ItemSet entry = new ItemSet();

            ArrayList<String> items = new ArrayList<String>(Arrays.asList(line.split(";")));
            entry.addItems(items);

            master.add(entry);
        }
        return master;
    }

    public static void main(String[] args) {
        Apriori apriori = new Apriori();
        ArrayList<String> originalRawDB;
        ArrayList<ItemSet> originalItemSetDB;

        apriori.readOriginalDatabase(itemsetInputFile);
        originalRawDB = apriori.getOriginalRawDB();
        originalItemSetDB = apriori.getOriginalItemSetDB(originalRawDB);

        apriori.calculateMinimumSupport(originalRawDB.size());

        System.out.println(originalRawDB.size() + " database lines read.");

        ArrayList<String> freqOneCandidateStrings = apriori.generateFreqOneCandidates(originalRawDB);

        ArrayList<ItemSet> freqOneCandidateItemSets = apriori.calulateSupportFreqOne(freqOneCandidateStrings);

        ArrayList<ItemSet> freqOneItemSet = apriori.getFrequentItemSets(freqOneCandidateItemSets);

        apriori.addToMasterItemSet(freqOneItemSet);

        // apriori.displayMasterItemSet();

        apriori.writeMasterItemSet(apriori.frequentOneOutputFile);

        ArrayList<ItemSet> freqTwoCandidates = apriori.generateFreqTwoCandidates(freqOneItemSet);

        System.out.println(freqTwoCandidates.size() + " Freq Two Candidates");

        // apriori.displayItemSet(freqTwoCandidates);
        ArrayList<ItemSet> freqTwoCandidateItemSets = apriori.calulateSupport(freqTwoCandidates, originalItemSetDB);

        ArrayList<ItemSet> freqTwoItemSet = apriori.getFrequentItemSets(freqTwoCandidateItemSets);
        // apriori.displayItemSet(freqTwoItemSet);
        System.out.println(freqTwoItemSet.size() + " Freq Two found");
        apriori.addToMasterItemSet(freqTwoItemSet);

        ArrayList<ItemSet> freqThreeCandidates = apriori.generateFreqThreeCandidates(freqTwoItemSet);
        ArrayList<ItemSet> freqThreeCandidateItemSets = apriori.calulateSupport(freqThreeCandidates, originalItemSetDB);

        ArrayList<ItemSet> freqThreeItemSet = apriori.getFrequentItemSets(freqThreeCandidateItemSets);
        apriori.displayItemSet(freqThreeItemSet);
        System.out.println(freqThreeItemSet.size() + " Freq Three found");
        apriori.addToMasterItemSet(freqThreeItemSet);
        apriori.writeMasterItemSet(apriori.allFrequentsOutputFile);

        //   ArrayList<CategorySet> set;
        //   set = apriori.buildItemSet(frequencyOneItemsetMinSup, k);

        //  int p = 0;
        // write out frequency 1 data to patterns.txt

        // next implement apriori now that have freq one itemset
    }

}