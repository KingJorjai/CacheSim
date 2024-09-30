//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        CacheMemoria c = new CacheMemoria(2, 4, 2, CacheMemoria.LRU, CacheMemoria.WT, CacheMemoria.WA);
        c.printCache();
        c.idatziAndPrint(34);
        c.irakurriAndPrint(100);
        c.idatziAndPrint(32);
        c.irakurriAndPrint(58);
        c.irakurriAndPrint(2);
        c.idatziAndPrint(36);
        c.irakurriAndPrint(0);
        c.idatziAndPrint(4);

    }
}