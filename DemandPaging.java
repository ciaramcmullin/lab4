/**
 * Ciara McMullin
 * Operating Systems Fall 2018
 * Lab 4: Demand Paging
 */

import java.util.*;
import java.lang.*;
import java.io.*;

public class DemandPaging {

    private static int M; // machine size
    private static int P; // page size
    private static int S; // size of each process
    private static int J; // job mix
    private static int N; // number of references for each process
    private static String R; // type of replacement algorithm
    private static int debug;
    private final static int QUANTUM = 3; // 3 references per process for round robin scheduling
    private static ArrayList<Process> processes = new ArrayList<>(); // process list
    private static ArrayList<Process> finishedProcesses; // finished process list
    private static int numOfFrames;
    private static ArrayList<Frame> frameTable; // global frame table
    private static Scanner random; // scanner for reading random numbers
    private static File file; // random number file

    /**
     * MAIN DRIVER FUNCTION- reads all input and calls helper function simulate DP to simulates n references for each process and
     * to produce all output.
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {

        // read in machine size, page size, size of each process, job mix, num of ref, and replacement algorithm from command line
        try {

            M = Integer.parseInt(args[0]);
            P = Integer.parseInt(args[1]);
            S = Integer.parseInt(args[2]);
            J = Integer.parseInt(args[3]);
            N = Integer.parseInt(args[4]);
            R = args[5];
            debug = Integer.parseInt(args[6]);

        } catch (InputMismatchException e) {

            System.out.println("INVALID INPUT");
            System.exit(1);

        }

        // set up scanner to read in random-numbers file
        String directory = System.getProperty("user.dir");
        String randFile = directory + "/random-numbers";
        file = new File(randFile);
        random = new Scanner(file);

        // print the output data!!
        System.out.println("The machine size is " + M);
        System.out.println("The page size is " + P);
        System.out.println("The process size is " + S);
        System.out.println("The job mix number is " + J);
        System.out.println("The number of references per process is " + N);
        System.out.println("The replacement aglorithm is " + R);
        System.out.println("The level of debugging output is " + debug);

        // get number of pages/frames and initialize the frame table
        numOfFrames = M / P;
        frameTable = new ArrayList<>();

        for (int i = 0; i < numOfFrames; i++) {
            Frame f = new Frame(i + 1, -1, -1, -1, 0);
            frameTable.add(f);
        }

        // four possible values of J from input:

        // val 1: fully sequential
        if (J == 1) {
            Process p = new Process(1, 1.0, 0.0, 0.0, S);
            processes.add(p);

        }
        // val 2
        else if (J == 2) {
            for (int i = 0; i < 4; i++) {
                Process process = new Process(i + 1, 1.0, 0.0, 0.0, S);
                processes.add(process);
            }
        }
        // val 3: fully random references
        else if (J == 3) {
            for (int i = 0; i < 4; i++) {
                Process p = new Process(i + 1, 0, 0, 0, S);
                processes.add(p);
            }
        }
        // val 4
        else if (J == 4) {
            for (int i = 0; i < 4; i++) {
                Process p;
                if (i == 0) {
                    p = new Process(i + 1, .75, .25, 0, S);
                } else if (i == 1) {
                    p = new Process(i + 1, .75, 0, .25, S);
                } else if (i == 2) {
                    p = new Process(i + 1, .75, .125, .125, S);
                } else {
                    p = new Process(i + 1, .5, .125, .125, S);
                }
                processes.add(p);
            }
        } else {
            System.out.println("INVALID INPUT FOR J");
            System.exit(1);
        }

        // call function to simulate N memory references per program and produce the output!!!
        simulateDP();
    }

    /**
     * function that simulates demand paging. It checks if each reference causes a page fault and then produces all output
     *
     */
    private static void simulateDP() {

        int clock = 1; // keeps track of time/cycles
        int position = -1;
        boolean hit = false;
        boolean full = true;
        finishedProcesses = new ArrayList<>(); // we use this list to see when all processes are finished aka in the finished list

        // while there are still processes left....
        while (finishedProcesses.size() != processes.size()) {

            // loop through the processes
            for (int i = 0; i < processes.size(); i++) {

                Process p = processes.get(i);
                // we use round robin scheduling meaning 3 references per process; first we simulate this reference for process then we calculate next reference
                for (int j = 0; j < QUANTUM; j++) {

                    // if the processes num of references equals N(input value) then we are done--> add process to finished list
                    if (p.numOfRef == N) {
                        p.finished = true;
                        finishedProcesses.add(p);
                        break;
                    }

                    int page = p.reference / P; // calculate page

                    // we check if there is a hit
                    for (int k = 0; k < frameTable.size(); k++) {
                        Frame f = frameTable.get(k);
                        // if the process id equals the current frame id and the status of the frame is 1 and the page is equal to frame page num we
                        // have a hit
                        if ((p.id == f.id) && (f.status == 1) && ((p.reference / P) == f.pageNum)) {
                            hit = true;
                            position = k;
                            break;
                        }
                    }
                    // if hit == true...
                    if (hit) {
                        Frame f = frameTable.get(position);
                        f.lastRef = clock;
                        if (debug == 1) {
                            System.out.print(p.id + " references word " + p.reference + " (page " + page + ") at time ");
                            System.out.println(clock + ": Hit in frame " + position);

                        }
                    }
                    // if hit == false...
                    else {
                        int framePos = -1;
                        // we must choose the highest numbered free frame as specified in the instructions
                        for (int l = frameTable.size() - 1; l >= 0; l--) {
                            Frame f = frameTable.get(l);
                            if (f.status == 0) {
                                full = false;
                                framePos = l;
                                break;

                            }
                        }
                        //System.out.println(framePos);
                        // if the frame table is not full, page fault --> make frame resident
                        if (!full) {
                            makeResident(p, clock, framePos, page);

                        } else {
                            // full table aka no free frames
                            // call replacement algorithm to evict resident
                            if (R.equals("lifo")) {
                                position = getLIFO(frameTable);
                            } else if (R.equals("random")) {
                                position = getRandom(frameTable);
                            } else if (R.equals("lru")) {
                                position = getLRU(frameTable);
                            }
                            // need to evict resident page
                            evictResident(p, position, clock, page);
                        }
                    }
                    // update processes references and clock
                    p.numOfRef += 1;
                    clock += 1;
                    // reset variables to their default values
                    position = -1;
                    hit = false;
                    full = true;
                    // calculate next reference for this process
                    nextReference(p);

                }
            }
        }

        // print output!!!
        int totalFaults = 0;
        double totalResidency = 0;
        int totalEvictions = 0;
        System.out.println();

        for (int i = 0; i < processes.size(); i++) {
            int pEvict = processes.get(i).numOfEvictions;

            System.out.print("Process " + processes.get(i).id + " had " + processes.get(i).numOfPageFaults +
                    " faults and ");
            if (pEvict == 0) {
                System.out.println();
                System.out.println("\tWith no evictions, the average residence is undefined.");

            } else {
                System.out.println(+(double) processes.get(i).residencyTime / pEvict + " average residency."
                );
            }
            totalFaults += processes.get(i).numOfPageFaults;
            totalResidency += processes.get(i).residencyTime;
            totalEvictions += processes.get(i).numOfEvictions;

        }

        System.out.println();

        System.out.print("The total number of faults is " + totalFaults);

        if (totalEvictions == 0) {
            System.out.println();
            System.out.println("\t With no evictions, the overall average residence is undefined.");
        } else {
            System.out.println(" and the overall average residency is " + totalResidency / totalEvictions);
        }


    }

    /**
     * This is a helper function for getting the next reference for Process p. We read a random number from the random-numbers file
     * and then divide it by RAND_MAX + 1 = Integer.MAX_VALUE +1d in Java. We compare y to the probability of A, B, C, or else random = 1 - A - B - C
     * @param p
     */
    private static void nextReference(Process p) {

        double fracA = p.A;
        double fracB = p.B;
        double fracC = p.C;

        double y = random.nextInt() / (Integer.MAX_VALUE + 1d);
        int nextReference = -1;

        if (y < fracA) {
            nextReference = (p.reference + 1) % S; // w + 1 mod S
        } else if (y < fracA + fracB) {
            nextReference = (p.reference + S - 5) % S; // w - 5 mod S
        } else if (y < fracA + fracB + fracC) {
            nextReference = (p.reference + 4) % S; // w + 4 mod S
        } else {
            nextReference = random.nextInt() % S; // (1 - A - B - C) / S
        }

        p.reference = nextReference;


    }

    /**
     * helper method that is used for the replacement of pages in LIFO manner
     * @param ft
     * @return position
     */
    private static int getLIFO(ArrayList<Frame> ft) {

        int lastLoaded = Integer.MIN_VALUE;
        int position = -1;

        for (int i = 0; i < ft.size(); i++) {
            if (ft.get(i).loaded > lastLoaded) {
                lastLoaded = ft.get(i).loaded;
                position = i;
            }
        }
        return position;
    }

    /**
     * helper method that is used for the replacement of pages in random manner
     * @param ft
     * @return random
     */
    private static int getRandom(ArrayList<Frame> ft) {
        int r = random.nextInt();
        return (r % ft.size());
    }

    /**
     * helper method that is used for the replacement of pages in LRU manner
     * @param ft
     * @return position
     */
    private static int getLRU(ArrayList<Frame> ft) {

        int minReference = Integer.MAX_VALUE;
        int position = -1;

        for (int i = 0; i < ft.size(); i++) {
            if (ft.get(i).lastRef < minReference) {
                minReference = ft.get(i).lastRef;
                position = i;
            }
        }
        return position;
    }

    /**
     * helper method that makes a page resident if a a page fault occurs and the frame table is not full
     * @param p
     * @param clock
     * @param position
     * @param page
     */
    private static void makeResident(Process p, int clock, int position, int page) {

        p.numOfPageFaults += 1;
        Frame f = frameTable.get(position);
        f.status = 1;
        f.id = p.id;
        f.pageNum = page;
        f.loaded = clock;
        f.lastRef = clock;
        if (debug == 1) {
            System.out.print(p.id + " references word " + p.reference + " (page " + page + ") at time ");
            System.out.println(clock + ": Fault, using free frame " + position);
        }
    }

    /**
     * helper method that evicts a resident page if a page fault occurs and there are no free frames in the frame table
     * @param p
     * @param position
     * @param clock
     */
    private static void evictResident(Process p, int position, int clock, int page) {

        // get selected victim and it's information
        Frame resident = frameTable.get(position);
       // int resPage = resident.pageNum;
        int resID = resident.id;

        // get process and calculate + update residency time
        Process process = processes.get(resID - 1);
        process.numOfEvictions += 1;
        process.residencyTime += (clock - resident.loaded); // res time = time the page was evicted minus time loaded

        // update the resident
        resident.loaded = clock;
        resident.lastRef = clock;
        resident.id = p.id;
        resident.pageNum = page;
        resident.status = 1;

        p.numOfPageFaults += 1; // increment page faults

        if (debug == 1) {
            System.out.println(p.id + " references word " + p.reference + " " + "(page " + page + ") at time " + clock
                    + ": Fault, evicting page " + resident.pageNum + " of " + process.id + " from frame " + position + " .");
        }
    }

}
