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
    public LinkedHashMap<Servidor, LinkedList<URLAddress>> paginasColetadas;
    public LinkedHashMap<Servidor, Record> records;
    
    public int count = 0;

    public EscalonadorSimples() {
        filaPaginas = new LinkedHashMap<>(); // páginas a serem coletadas de cada servidor
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

        for (Servidor s : filaPaginas.keySet()) {
            if (s.isAccessible() && !filaPaginas.get(s).isEmpty()) {
                url = filaPaginas.get(s).removeFirst();
                if (!paginasColetadas.get(new Servidor(url.getDomain())).contains(url)){
                    s.acessadoAgora();
                    return url;
                }
                else {
                    return null;
                }
            }
            else if (filaPaginas.get(s).isEmpty()){
                try {
                    System.out.println(Thread.currentThread().getName() + "[PARADA]");
                    this.wait(Servidor.ACESSO_MILIS);
                } catch (InterruptedException ex) {
                    Logger.getLogger(EscalonadorSimples.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
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

        if (filaPaginas.containsKey(servidor)) {
            if (urlAdd.getDepth() <= DEPTH_LIMIT && !filaPaginas.get(servidor).contains(urlAdd)) {
                filaPaginas.get(servidor).add(urlAdd);
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public synchronized Record getRecordAllowRobots(URLAddress url) {
        if (url == null) {
            return null;
        }

        RobotExclusion robotExclusion = new RobotExclusion();
        try {
            return robotExclusion.get(new URL(url.getAddress()), "valeBot");
        } catch (MalformedURLException ex) {
            Logger.getLogger(EscalonadorSimples.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public synchronized void putRecorded(String domain, Record domainRec) {
        records.put(new Servidor(domain), domainRec);
    }

    public synchronized void putFetchedPage(URLAddress url) {
        paginasColetadas.get(new Servidor(url.getDomain())).add(url);
    }

    @Override
    public synchronized boolean finalizouColeta() {
        return count > PAGES_LIMIT;
    }

    @Override
    public synchronized void countFetchedPage() {
        count++;
    }

}
