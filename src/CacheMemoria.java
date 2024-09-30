import java.util.ArrayList;
import java.util.Arrays;

/**
 * 8 blokeko cachea
 */
public class CacheMemoria {

    /** Cachearen propietateak */
    private final int hitzTamaina;
    private final int blokeTamaina;
    private final int multzoTamaina;

    /** Latentzia denborak */
    private final int TCM;
    private final int TMN;
    private final int TBuff;
    private final int TBl;

    /** 0(FIFO) - 1(LRU) */
    private final boolean ordPolitika;
    public final static boolean FIFO=true, LRU=false;

    /** 0(Write-Back) - 1(Write-Through) */
    private final boolean idazPolitika;
    public final static boolean WB=false, WT=true;

    /** 0(Write-Allocate) - 1(No-Write-Allocate) */
    private final boolean wtPolitika;
    public final static boolean WA=false, NWA=true;

    private final int OKUP=0, ALD=1, TAG=2, ORD=3, BLOKEA=4;
    private int [][] cache;

    private int azkenikAtzitutakoCMBlokea = -1;

    public CacheMemoria(int hitzTamaina, int blokeTamaina, int multzoTamaina, boolean ordPolitika, boolean idazPolitika, boolean wtPolitika) {
        this.hitzTamaina = hitzTamaina;
        this.blokeTamaina = blokeTamaina;
        this.multzoTamaina = multzoTamaina;
        this.ordPolitika = ordPolitika;
        this.idazPolitika = idazPolitika;
        this.wtPolitika = wtPolitika;

        this.TCM = 2;
        this.TMN = 20;
        this.TBuff = 1;
        this.TBl = TMN + (blokeTamaina-1)*TBuff;

        cache = new int[5][8];
        Arrays.fill(Arrays.stream(cache).flatMapToInt(Arrays::stream).toArray(), 0);
    }

    /**
     * Helbidea irakurriz, ziklo kopurua itzuli
     * @param helbidea Helbide fisikoa
     * @return Ziklo kopurua
     */
    public int irakurri(int helbidea) {
        int blokeaMN = helbidea / hitzTamaina / blokeTamaina;
        int multzoa = blokeaMN % (8/multzoTamaina);
        int blokeaCM = helbidea % blokeTamaina;

        int zikloak = 0;
        // Cachean bilatu
        if (cacheanDago(helbidea)) {
            // Hit
            zikloak += 2;
            if (ordPolitika==LRU) {
                for (int i=multzoa*multzoTamaina; i<multzoa*multzoTamaina+multzoTamaina-1; i++) {
                    if (cache[OKUP][i] == 1 && cache[ORD][i] < cache[ORD][multzoa*multzoTamaina+blokeaCM]) {
                        cache[ORD][i]++;
                    }
                }
                cache[ORD][multzoa*multzoTamaina+blokeaCM] = 0;

            }
        }
        else {
            // Miss
            zikloak += ekarriBlokea(helbidea);
        }

        return zikloak;
    }

    /**
     * Bloke bat ekarri cache memorian eta ziklo kopurua itzuli
     * @param helbidea Helbide fisikoa
     * @return Ziklo kopurua
     */
    private int ekarriBlokea(int helbidea) {
        int zikloak = 0;

        int blokeaMN = helbidea / hitzTamaina / blokeTamaina;
        int multzoa = blokeaMN % (8/multzoTamaina);
        int tag = blokeaMN / (8/multzoTamaina);

        // Bilatu bloke hutsik
        int[] blokeHutsik = new int[multzoTamaina];
        ArrayList<Integer> blokeHutsikIndizeak = new ArrayList<>();
        for (int i = multzoa*multzoTamaina; i < multzoa*multzoTamaina+multzoTamaina; i++) {
            if (cache[OKUP][i] == 0) {// hemos cambiado aqui el valor de i
                // Blokea hutsik
                blokeHutsikIndizeak.add(i);
            }
        }
        if (!blokeHutsikIndizeak.isEmpty()) {
            // Hutsik dauden blokeen arteko bat aukeratu (RANDOM)
            int aukeratutakoBlokea;
            aukeratutakoBlokea = blokeHutsikIndizeak.get((int)(Math.random()*blokeHutsikIndizeak.size()));
            // Ekarri blokea
            cache[OKUP][aukeratutakoBlokea] = 1;
            cache[ALD][aukeratutakoBlokea] = 0;
            cache[TAG][aukeratutakoBlokea] = tag;
            cache[BLOKEA][aukeratutakoBlokea] = blokeaMN;
            // Eguneratu ordena
            for (int i = multzoa*multzoTamaina; i < multzoa*multzoTamaina+multzoTamaina; i++) {
                if (cache[OKUP][i] == 1) {
                    cache[ORD][i]++;
                }
            }
            cache[ORD][aukeratutakoBlokea] = 0;
            azkenikAtzitutakoCMBlokea = aukeratutakoBlokea;
            zikloak += TCM + TBl;

        } else {
            // Hutsik ez dago
            zikloak += kenduBlokea(multzoa); // Kendu 'ord' gehiena duen blokea
            return zikloak + ekarriBlokea(helbidea);
        }
        return zikloak;
    }

    /**
     * Cachean dagoen bilatu
     * @param helbidea Helbide fisikoa
     * @return True: Hit, False: Miss
     */
    private boolean cacheanDago(int helbidea) {

        int blokeaMN = helbidea / hitzTamaina / blokeTamaina;
        int multzoa = blokeaMN % (8/multzoTamaina);
        int tag = blokeaMN / (8/multzoTamaina);

        for (int i = multzoa*multzoTamaina; i < multzoa*multzoTamaina+multzoTamaina; i++) {
            if (cache[OKUP][i] == 1) {
                if (cache[TAG][i] == tag) {
                    // Hit
                    azkenikAtzitutakoCMBlokea = i;
                    return true;
                }
            }
        }

        // Miss
        return false;
    }

    /**
     * Bloke bat kendu cache memoriatik eta ziklo kopurua itzuli.
     * Write-Back politika bada, blokea MNan eguneratu zikloak gehituz
     *
     * @param multzoa Zein multzotik kendu blokea
     * @return Ziklo kopurua
     */
    private int kenduBlokea(int multzoa) {
        int kentzekoBlokea = -1;
        // Bilatu kentzeko blokea (ORD gehiena duena)
        for (int i = multzoa * multzoTamaina; i < multzoa * multzoTamaina + multzoTamaina; i++) {
            if (cache[OKUP][i] == 1 && cache[ORD][i] >= multzoTamaina - 1) {
                kentzekoBlokea = i;
            }
        }
        // Kendu blokea eta eguneratu ordena
        for (int i = multzoa * multzoTamaina; i < multzoa * multzoTamaina + multzoTamaina; i++) {
            if (cache[OKUP][i] < multzoTamaina - 1) {
                cache[ORD][i]++;
            }
        }
        cache[OKUP][kentzekoBlokea] = 0;

        if (idazPolitika == WB && cache[ALD][kentzekoBlokea] == 1) {
            // Write-Back
            cache[ALD][kentzekoBlokea] = 0;
            return TCM + TBl;
        } else
            return 0;

    }

    /**
     * Cache taula inprimatu ASCII art moduan printf erabiltzen
     */
    private void printCacheHighlightedWith(int azkenikAtzitutakoCMBlokea, String color) {
        String reset = "\033[0m";

        System.out.print(reset);
        System.out.println("┌──────┬─────┬─────┬─────┬┬────────┐ ");
        System.out.print(  "│ okup │ ald │ tag │ ord ││ blokea │");
        for (int i = 0; i < 8; i++) {
            System.out.println();
            if (i % multzoTamaina == 0) {
                System.out.println("├──────┼─────┼─────┼─────┼┼────────┤ ");
            }
            if (i == azkenikAtzitutakoCMBlokea) {
                System.out.printf("│ "+color+"%4d"+reset+" │ "+color+"%3d"+reset+" │ "+color+"%3d"+reset+" │ "+color+"%3d"+reset+" ││ "+color+"%6s"+reset+" │", cache[OKUP][i], cache[ALD][i], cache[TAG][i], cache[ORD][i], (cache[OKUP][i]==1)?String.valueOf(cache[BLOKEA][i]):"---");
            } else {
                System.out.printf("│ %4d │ %3d │ %3d │ %3d ││ %6s │", cache[OKUP][i], cache[ALD][i], cache[TAG][i], cache[ORD][i], (cache[OKUP][i]==1)?String.valueOf(cache[BLOKEA][i]):"---");
            }
        }
        System.out.println("\n└──────┴─────┴─────┴─────┴┴────────┘ ");
    }

    /**
     * Cache taula inprimatu cache memoriako azkenik atzitutako blokea koloreztatuz
     *
     * @see #printCacheHighlightedWith
     */
    public void printCache() {
        printCacheHighlightedWith(azkenikAtzitutakoCMBlokea, "\u001B[0;93m");
    }

    /**
     * Helbide bat irakurri eta cache taula inprimatu
     * @param helbidea Helbide fisikoa
     *
     * @see #irakurri
     * @see #printCache
     */
    public void irakurriAndPrint(int helbidea) {
        int zikloak = irakurri(helbidea);
        printCache();
        System.out.println("Helbide: "+helbidea+" Zikloak: "+zikloak);
    }

}
