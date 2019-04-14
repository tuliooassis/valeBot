/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package crawler;

import crawler.escalonadorCurtoPrazo.PageFetcher;

/**
 *
 * @author aluno
 */
public class Coletor1 {

    private final static int NUM_THREADS = 10;

    public static void main(String[] args) throws InterruptedException {
        for (int i = 0; i < NUM_THREADS; i++) {
            PageFetcher pf = new PageFetcher();

            Thread thread = new Thread(pf);
            thread.start();
            thread.join();
        }
    }
}
