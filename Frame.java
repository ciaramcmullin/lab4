/**
 * Ciara McMullin
 * Operating Systems Fall 2018
 * Lab 4: Demand Paging
 */

// FRAME CLASS
public class Frame {

    int id;
    int status;
    int pageNum;
    int position;
    int loaded;
    int lastRef;

    /**
     * Frame constructor
     * @param id
     * @param pageNum
     * @param loaded
     * @param position
     * @param status
     */
    public Frame(int id, int pageNum, int loaded, int position, int status){
        this.id = id;
        this.pageNum = pageNum;
        this.loaded = loaded;
        this.position = position;
        this.status = status;
    }
}
