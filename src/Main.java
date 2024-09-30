//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        CacheMemoria cache = new CacheMemoria(4, 4, 4, CacheMemoria.LRU, CacheMemoria.WB, CacheMemoria.WA);
        cache.printCache();
        cache.irakurriAndPrint(0);
        cache.irakurriAndPrint(4);
        cache.irakurriAndPrint(8);
        cache.irakurriAndPrint(12);

        cache.irakurriAndPrint(16);
        cache.irakurriAndPrint(32);
        cache.irakurriAndPrint(48);
        cache.irakurriAndPrint(64);
        cache.irakurriAndPrint(80);
        cache.irakurriAndPrint(96);
        cache.irakurriAndPrint(112);
        cache.irakurriAndPrint(128);
        cache.irakurriAndPrint(144);
    }
}