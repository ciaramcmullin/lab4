/**
 * Ciara McMullin
 * Operating Systems Fall 2018
 * Lab 4: Demand Paging
 */

// PROCESS CLASS
public class Process {

    double A;
    double B;
    double C;
    int id;
    int S;
    int reference;
    int numOfRef;
    int numOfPageFaults;
    int residencyTime;
    int numOfEvictions;
    boolean finished;

    /**
     * Process constructor
     * @param id
     * @param A
     * @param B
     * @param C
     * @param S
     */
    public Process(int id, double A, double B, double C, int S){
        // initialize input data and all others to 0
        this.id = id;
        this.A = A;
        this.B = B;
        this.C = C;
        this.S = S;
        this.finished = false;
        this.reference = (111 * id) % S;
        this.numOfRef= 0;
        this.numOfPageFaults = 0;
        this.residencyTime = 0;
        this.numOfEvictions = 0;

    }


}
