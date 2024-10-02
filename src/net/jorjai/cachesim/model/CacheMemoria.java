package net.jorjai.cachesim.model;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * 8 blokeko cachea
 */
public class CacheMemoria {

    /** Hitz tamaina, bytetan */
    private final int hitzTamaina;
    /** Bloke tamaina, hitzetan*/
    private final int blokeTamaina;
    /** Multzo tamaina, bloketan */
    private final int multzoTamaina;

    /** Latentzia denborak */
    private final int TCM;
    private final int TMN;
    private final int TBuff;
    private final int TBl;

    /** true(FIFO) - false(LRU) */
    private final boolean ordPolitika;
    public final static boolean FIFO=true, LRU=false;

    /** false(write-back) - true(write-through) */
    private final boolean idazPolitika;
    public final static boolean WB=false, WT=true;

    /** false(write-allocate) - true(write-no-allocate) */
    private final boolean wtPolitika;
    public final static boolean WA=false, WNA =true;

    private final int OKUP=0, ALD=1, TAG=2, ORD=3, BLOKEA=4;
    private int [][] cache;

    private int azkenikAtzitutakoCMBlokea = -1;
    private CacheEstadistika estadistika;

    /**
     * CacheMemoria klasearen konstruktorea
     * @param hitzTamaina Hitz tamaina, bytetan
     * @param blokeTamaina Bloke tamaina, hitzetan
     * @param multzoTamaina Multzo tamaina, bloketan
     * @param ordPolitika FIFO(true) edo LRU(false)
     * @param idazPolitika write-back(false) edo write-through(true)
     * @param wtPolitika write-allocate(false) edo write-no-allocate(true).
     *                   Only used if idazPolitika is write-through.
     */
    public CacheMemoria(int hitzTamaina, int blokeTamaina, int multzoTamaina, boolean ordPolitika, boolean idazPolitika, boolean wtPolitika) {
        this.hitzTamaina = hitzTamaina;
        this.blokeTamaina = blokeTamaina;
        this.multzoTamaina = multzoTamaina;
        this.ordPolitika = ordPolitika;
        this.idazPolitika = idazPolitika;
        if (idazPolitika == WT) {
            this.wtPolitika = wtPolitika;
        } else {
            this.wtPolitika = WA;
        }

        this.TCM = 2;
        this.TMN = 20;
        this.TBuff = 1;
        this.TBl = TMN + (blokeTamaina-1)*TBuff;

        this.estadistika = new CacheEstadistika();

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

        printEragiketaInfo(helbidea);
        System.out.print("    Rd/Wr: Read -- ");

        int zikloak = 0;
        // Cachean bilatu
        if (cacheanDago(helbidea)) {
            // Hit
            System.out.println("ASMATZEA");
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
            System.out.println("HUTSEGITEA");
            zikloak += ekarriBlokea(helbidea);
        }
        estadistika.addZikloak(zikloak);
        estadistika.addReadCount();
        System.out.printf("    T_erag: %d ziklo, (Tcm: %d, Tmn: %d, bl: %d)\n", zikloak, TCM, TMN, TBl);
        return zikloak;
    }

    /**
     * Idatzi cachean
     * @param helbidea Helbide fisikoa
     * @return Ziklo kopurua
     */
    public int idatzi(int helbidea) {
        printEragiketaInfo(helbidea);
        System.out.print("    Rd/Wr: Write -- ");

        int zikloak = 0;

        if (idazPolitika==WB){
            if (cacheanDago(helbidea)) {
                // Hit
                zikloak += TCM;
                cache[ALD][azkenikAtzitutakoCMBlokea] = 1;
                System.out.println("ASMATZEA");
            }
            else {
                // Miss
                zikloak += ekarriBlokea(helbidea);
                cache[ALD][azkenikAtzitutakoCMBlokea] = 1;
                System.out.println("HUTSEGITEA");
            }
        } else {
            // Write-Through
            if (cacheanDago(helbidea)) {
                // Hit
                zikloak += TCM + TMN;
                System.out.println("ASMATZEA");
            }
            else {
                // Miss
                if (wtPolitika==WA) {
                    zikloak += ekarriBlokea(helbidea) + TMN;
                }
                else {
                    zikloak += TCM + TMN;
                }
                System.out.println("HUTSEGITEA");
            }
        }

        estadistika.addZikloak(zikloak);
        estadistika.addWriteCount();
        System.out.printf("    T_erag: %d ziklo, (Tcm: %d, Tmn: %d, bl: %d)\n", zikloak, TCM, TMN, TBl);
        return zikloak;
    }

    private void printEragiketaInfo(int helbidea) {
        int blokeaMN = helbidea / hitzTamaina / blokeTamaina;
        int multzoa = blokeaMN % (8/multzoTamaina);

        System.out.printf(" >> %d. eragiketa\n", estadistika.getEragiketaKopurua()+1);
        System.out.printf("    Helbidea: %d - Hitza: %d - Blokea: %d (%d-%d hitzak)\n", helbidea, helbidea /hitzTamaina, blokeaMN, blokeaMN *blokeTamaina, blokeaMN *blokeTamaina+blokeTamaina-1);
        System.out.printf("    Multzoa: %d - Tag: %d\n", multzoa, blokeaMN /(8/multzoTamaina));
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

                    // LRU politika
                    if (ordPolitika == LRU) {
                        for (int j = multzoa*multzoTamaina; j < multzoa*multzoTamaina+multzoTamaina; j++) {
                            if (cache[OKUP][j] == 1 && cache[ORD][j] < cache[ORD][i]) {
                                cache[ORD][j]++;
                            }
                        }
                        cache[ORD][i] = 0;
                    }
                    estadistika.addHit();
                    return true;
                }
            }
        }

        // Miss
        estadistika.addMiss();
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
        System.out.println("    ┌──────┬─────┬─────┬─────┬┬────────┐ ");
        System.out.print(  "    │ okup │ ald │ tag │ ord ││ blokea │");
        for (int i = 0; i < 8; i++) {
            System.out.println();
            if (i % multzoTamaina == 0) {
                System.out.println("    ├──────┼─────┼─────┼─────┼┼────────┤ ");
            }
            if (i == azkenikAtzitutakoCMBlokea) {
                System.out.printf("    │ "+color+"%4d"+reset+" │ "+color+"%3s"+reset+" │ "+color+"%3d"+reset+" │ "+color+"%3s"+reset+" ││ "+color+"%6s"+reset+" │",
                        cache[OKUP][i], idazPolitika?"-":cache[ALD][i], cache[TAG][i], (multzoTamaina==1)?"-":cache[ORD][i], (cache[OKUP][i]==1)?cache[BLOKEA][i]:"---");
            } else {
                System.out.printf("    │ %4d │ %3s │ %3d │ %3s ││ %6s │",
                        cache[OKUP][i], idazPolitika?"-":cache[ALD][i], cache[TAG][i], (multzoTamaina==1)?"-":cache[ORD][i], (cache[OKUP][i]==1)?cache[BLOKEA][i]:"---");
            }
        }
        System.out.println("\n    └──────┴─────┴─────┴─────┴┴────────┘ \n");

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
    }

    /**
     * Helbide bat idatzi eta cache taula inprimatu
     * @param helbidea Helbide fisikoa
     */
    public void idatziAndPrint(int helbidea) {
        int zikloak = idatzi(helbidea);
        printCache();
    }

    public void printStats() {
        // Titulua
        System.out.println("\n\n===== Simulazioaren emaitza globalak =====\n");
        // Cachearen karakteristikak
        System.out.printf("%d byteko hitzak - %d byteko blokeak (%d hitz)\n", hitzTamaina, blokeTamaina*hitzTamaina, blokeTamaina);
        System.out.printf("Cachea: %d multzo x %d bloke -- %s\n\n", 8/multzoTamaina, multzoTamaina, (ordPolitika==LRU)?"LRU":"FIFO");

        // Estadistikak
        estadistika.getStatistikenLaburpena();
    }



}
