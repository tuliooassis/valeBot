/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package crawler;

import crawler.escalonadorCurtoPrazo.PageFetcher;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author aluno
 */
public class Coletor1 {

    private final static int NUM_THREADS = 100;

    public static void main(String[] args) throws InterruptedException {
        PageFetcher pf = new PageFetcher();
        List<Thread> threads = new ArrayList<Thread>();
        
        for (int i = 0; i < NUM_THREADS; i++) {
            Thread thread = new Thread(pf);
            thread.start();
            threads.add(thread);
        }
        
        for(Thread thread : threads) {
            thread.join();
        }
    }
}
