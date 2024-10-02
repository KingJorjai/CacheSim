package net.jorjai.cachesim.menu;

import net.jorjai.cachesim.model.CacheMemoria;

import java.util.NoSuchElementException;
import java.util.Scanner;

public class CacheMenu {
    CacheMemoria c;
    Scanner sc;

    public CacheMenu() {
        sc = new Scanner(System.in);
    }

    public void init() {
        printTitle();
        createCache();
        int option = 0;
        while (option != -1) {
            option = askForOperation();
        }
        c.printStats();
        c.kenduGeratzenDirenBlokeak();
    }

    private void printTitle() {
        System.out.println(
                "+-------------------------------------+\n" +
                "|          CACHE SIMULADOREA          |\n" +
                "|       Jorge Arévalo - 2024-25       |\n" +
                "+-------------------------------------+\n"
        );
    }

    private int askForOperation() {
        System.out.print("Irakurri (0) / Idatzi (1) [Irten (-1)] > ");
        int option = ReadIntFromPool(new int[]{-1, 0, 1});

        if (option == 0) {
            System.out.print("Memoria helbidea > ");
            int helbidea = ReadAdress();
            System.out.println();
            c.irakurriAndPrint(helbidea);
        } else if (option == 1) {
            System.out.print("Memoria helbidea > ");
            int helbidea = ReadAdress();
            System.out.println();
            c.idatziAndPrint(helbidea);
        }
        return option;
    }

    private void createCache() {
        System.out.print("Hitzen tamaina (4-8 byte) > ");
        int hitzTamaina = ReadIntFromPool(new int[]{4, 8});

        System.out.print("Blokeen tamaina (32-64-128 byte) > ");
        int blokeTamaina = ReadIntFromPool(new int[]{32, 64, 128});

        System.out.print("Multzoen tamaina (1-2-4-8 bloke) > ");
        int multzoTamaina = ReadIntFromPool(new int[]{1, 2, 4, 8});

        int ordezketaPolitika = 0;
        if (multzoTamaina > 1) {
            System.out.print("Ordezk.-polit.: 0(FIFO) - 1(LRU) > ");
            ordezketaPolitika = ReadIntFromPool(new int[]{0, 1});
        }

        System.out.print("Idazketa politika: 0(write-through) - 1(write-back) > ");
        int idazketaPolitika = ReadIntFromPool(new int[]{0, 1});

        int writeAllocate;
        if (idazketaPolitika == 0) {
            System.out.print("Write-through politika: 0(write-allocate) - 1(write-no-allocate) > ");
            writeAllocate = ReadIntFromPool(new int[]{0, 1});
        } else {
            writeAllocate = 1;
        }

        System.out.println();

        boolean ordezketaPolitikaBool = ordezketaPolitika == 0;
        boolean idazketaPolitikaBool = idazketaPolitika == 0;
        boolean writeAllocateBool = writeAllocate == 1;

        c = new CacheMemoria(hitzTamaina, blokeTamaina/hitzTamaina, multzoTamaina, ordezketaPolitikaBool, idazketaPolitikaBool, writeAllocateBool);
    }

    int ReadIntFromPool(int[] possibleOptions) {
        int option = -1;
        boolean validOption = false;
        while (!validOption) {
            option = ReadInt();
            if (arrayContains(possibleOptions, option)) {
                validOption = true;
            } else {
                System.out.print("Aukera okerra. Saiatu berriro > ");
            }
        }
        return option;
    }

    private int ReadAdress() {
        Integer helbidea = null;
        while (helbidea == null || helbidea < 0) {
            helbidea = ReadInt();
        }
        return helbidea;
    }

    private int ReadInt() {
        Integer option = null;
        while (option == null) {
            try {
                option = Integer.parseInt(sc.nextLine());
            } catch (NumberFormatException e) {
                System.out.print("Zenbaki bat sartu behar duzu > ");
            } catch (NoSuchElementException e) {
                System.exit(0);
            }
        }
        return option;
    }

    private boolean arrayContains(int[] possibleOptions, int option) {
        for (int possibleOption : possibleOptions) {
            if (possibleOption == option) {
                return true;
            }
        }
        return false;
    }
}
