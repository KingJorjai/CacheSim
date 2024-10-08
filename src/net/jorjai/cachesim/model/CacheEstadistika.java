package net.jorjai.cachesim.model;

public class CacheEstadistika {

    private int zikloak;
    private int hit;
    private int miss;
    private int readCount;
    private int writeCount;
    private int WBCount;
    private int WBzikloak;

    public CacheEstadistika() {
        zikloak = 0;
        hit = 0;
        miss = 0;
        readCount = 0;
        writeCount = 0;
    }

    public void addZikloak(int z) {
        zikloak += z;
    }

    public void addHit() {
        hit++;
    }

    public void addMiss() {
        miss++;
    }

    public void addReadCount() {
        readCount++;
    }

    public void addWriteCount() {
        writeCount++;
    }

    public int getZikloak() {
        return zikloak;
    }

    public double getHitRate() {
        return (double)hit / (hit + miss);
    }

    public double getZikloEragiketaRate() {
        return (double)(readCount + writeCount) / zikloak;
    }
    public int getEragiketaKopurua() {
        return readCount + writeCount;
    }

    public int getHit() {
        return hit;
    }

    public int getMiss() {
        return miss;
    }

    public int getReadCount() {
        return readCount;
    }

    public int getWriteCount() {
        return writeCount;
    }

    public void getStatistikenLaburpena() {
        System.out.printf(  "   Eragiketak:   %d (%d RD + %d WR)\n", getEragiketaKopurua(), getReadCount(), getWriteCount());
        System.out.println( "   Asmatzeak:    "+getHit());
        System.out.printf(  "   Asmatze tasa: h = %.2f\n", getHitRate());
    }

    public void addWBCount() {
        WBCount++;
    }

    public void addWBZikloak(int z) {
        WBzikloak += z;
    }

    public int getWBZikloak() {
        return WBzikloak;
    }
}
