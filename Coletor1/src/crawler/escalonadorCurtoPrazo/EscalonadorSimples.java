package crawler.escalonadorCurtoPrazo;

import java.net.MalformedURLException;
import java.net.URL;

import com.trigonic.jrobotx.Record;
import com.trigonic.jrobotx.RobotExclusion;

import crawler.Servidor;
import crawler.URLAddress;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EscalonadorSimples implements Escalonador {

    public final int DEPTH_LIMIT = 4;
    public final int PAGES_LIMIT = 500;
    public LinkedHashMap<Servidor, LinkedList<URLAddress>> filaPaginas;
    public LinkedHashMap<Servidor, LinkedList<URLAddress>> blackDominios;
    public LinkedHashMap<Servidor, LinkedList<URLAddress>> paginasColetadas;
    public LinkedHashMap<Servidor, Record> records;
    
    private int count = 0;

    public EscalonadorSimples() {
        filaPaginas = new LinkedHashMap<>(); // páginas a serem coletadas de cada servidor
        blackDominios = new LinkedHashMap<>(); // páginas que deram exception e não serão mais coletadas
        paginasColetadas = new LinkedHashMap<>(); // páginas já coletadas de cada servidor
        records = new LinkedHashMap<>(); // records dos servidores
        
        filaPaginas.put(new Servidor("computerworld.com.br"), new LinkedList<>());      // Felipe
        filaPaginas.put(new Servidor("www.taringa.net"), new LinkedList<>());           // Felipe
        filaPaginas.put(new Servidor("www.theguardian.com"), new LinkedList<>());       // Marcela
        filaPaginas.put(new Servidor("www.pcworld.com"), new LinkedList<>());           // Marcela
        filaPaginas.put(new Servidor("indianexpress.com"), new LinkedList<>());         // Tulio
        filaPaginas.put(new Servidor("www.soy502.com"), new LinkedList<>());            // Tulio


        paginasColetadas.put(new Servidor("computerworld.com.br"), new LinkedList<>());
        paginasColetadas.put(new Servidor("www.taringa.net"), new LinkedList<>());
        paginasColetadas.put(new Servidor("www.theguardian.com"), new LinkedList<>());
        paginasColetadas.put(new Servidor("www.pcworld.com"), new LinkedList<>());
        paginasColetadas.put(new Servidor("indianexpress.com"), new LinkedList<>());
        paginasColetadas.put(new Servidor("www.soy502.com"), new LinkedList<>());

        try {
            adicionaNovaPagina(new URLAddress("https://computerworld.com.br", 0));
            adicionaNovaPagina(new URLAddress("https://www.taringa.net", 0));
            adicionaNovaPagina(new URLAddress("https://www.theguardian.com", 0));
            adicionaNovaPagina(new URLAddress("https://www.pcworld.com", 0));
            adicionaNovaPagina(new URLAddress("https://indianexpress.com", 0));
            adicionaNovaPagina(new URLAddress("https://www.soy502.com", 0));

        } catch (MalformedURLException e) {
            System.out.println("Exception: MalformedURLException - " + e);
        }
    }

    @Override
    public synchronized URLAddress getURL() {
        URLAddress url = null;
        LinkedHashMap<Servidor, LinkedList<URLAddress>> filaPaginasCopy;
        filaPaginasCopy = (LinkedHashMap<Servidor, LinkedList<URLAddress>>) filaPaginas.clone();
        
        for (Servidor s : filaPaginasCopy.keySet()) {
            if (s.isAccessible() && !filaPaginasCopy.get(s).isEmpty()) {
                url = filaPaginasCopy.get(s).removeFirst();
                s.acessadoAgora();
                return url;
            }
        }
        
        
        try {
            System.out.println(Thread.currentThread().getName() + " [PARADA]");
            this.wait(Servidor.ACESSO_MILIS);
        } catch (InterruptedException ex) {
            Logger.getLogger(EscalonadorSimples.class.getName()).log(Level.SEVERE, null, ex);
        }


        return url;
    }
    
    @Override
    public synchronized boolean adicionaNovaPagina(URLAddress urlAdd) {
        Servidor servidor = new Servidor(urlAdd.getDomain());
        if (urlAdd.getDepth() > DEPTH_LIMIT) {
            return false;
        }
        
        if (paginasColetadas.containsKey(servidor)) {
            if (paginasColetadas.get(servidor).contains(urlAdd))
                return false;
        }
        
        if (blackDominios.containsKey(servidor)) {
            return false;
        }

        if (filaPaginas.containsKey(servidor)) {
            if (urlAdd.getDepth() <= DEPTH_LIMIT && !filaPaginas.get(servidor).contains(urlAdd)) {
                this.putPagina(urlAdd);
                return true;
            } else {
                return false;
            }
        } else {
            this.putPagina(urlAdd);
            this.putFetchedPage(urlAdd);
            return true;
        }
    }

    @Override
    public Record getRecordAllowRobots(URLAddress url) {
        if (url == null) {
            return null;
        }

        RobotExclusion robotExclusion = new RobotExclusion();
        try {
            return robotExclusion.get(new URL(url.getAddress()), "valeBot");
        } catch (MalformedURLException ex) {
            this.putBlackDominios(url);
            Logger.getLogger(EscalonadorSimples.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public synchronized void putRecorded(String domain, Record domainRec) {
        records.put(new Servidor(domain), domainRec);
    }

    public synchronized void putFetchedPage(URLAddress url) {
        Servidor s = new Servidor(url.getDomain());
        if (paginasColetadas.get(s) != null){
            paginasColetadas.get(s).add(url);
        }
        else {
            LinkedList<URLAddress> fila = new LinkedList<>();
            fila.add(url);
            paginasColetadas.put(s, fila);
        }
    }
    
    public synchronized void putPagina(URLAddress url) {
        Servidor s = new Servidor(url.getDomain());
        if (filaPaginas.get(s) != null){
            filaPaginas.get(s).add(url);
        }
        else {
            LinkedList<URLAddress> fila = new LinkedList<>();
            fila.add(url);
            filaPaginas.put(s, fila);
        }
    }
    
    public synchronized void putBlackDominios(URLAddress url) {
        Servidor s = new Servidor(url.getDomain());
        if (blackDominios.get(s) != null){
            blackDominios.get(s).add(url);
        }
        else {
            LinkedList<URLAddress> fila = new LinkedList<>();
            fila.add(url);
            blackDominios.put(s, fila);
        }
        System.out.println(Thread.currentThread().getName() + " [EXCECÃO] \t\t\t\t\t\t" + url.getAddress());
    }

    @Override
    public boolean finalizouColeta() {
        return count > PAGES_LIMIT;
    }

    @Override
    public synchronized void countFetchedPage() {
        count++;
    }
    
    public int getCount() {
        return count;
    }

}
