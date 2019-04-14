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

    public final int DEPTH_LIMIT = 3;
    public final int PAGES_LIMIT = 100;
    public LinkedHashMap<Servidor, LinkedList<URLAddress>> filaPaginas;
    public LinkedHashMap<Servidor, Record> records;
    public LinkedHashMap<URLAddress, Boolean> fetchedUrls;
    
    public int count = 0;

    public EscalonadorSimples() {
        filaPaginas = new LinkedHashMap<>();
        records = new LinkedHashMap<>();
        fetchedUrls = new LinkedHashMap<>();

//            filaPaginas.put(new Servidor("www.theguardian.com"), new LinkedList<>());
//            filaPaginas.put(new Servidor("www.submarino.com.br"), new LinkedList<>());
        filaPaginas.put(new Servidor("www.indianexpress.com"), new LinkedList<>());

        try {

//                adicionaNovaPagina(new URLAddress("https://www.submarino.com.br", 0));
//                adicionaNovaPagina(new URLAddress("https://www.theguardian.com", 0));
            adicionaNovaPagina(new URLAddress("https://www.indianexpress.com", 0));
        } catch (MalformedURLException e) {
            System.out.println("Exception: MalformedURLException - " + e);
        }
    }

    @Override
    public synchronized URLAddress getURL() {
        URLAddress url = null;

        for (Servidor s : filaPaginas.keySet()) {
            LinkedList<URLAddress> lista;
            lista = filaPaginas.get(s);
            if (s.isAccessible() && !lista.isEmpty()) {
                url = lista.removeFirst();
                
                if (fetchedUrls.get(url) == null){
                    s.acessadoAgora();
                    fetchedUrls.put(url, true);
                    return url;
                }
                else {
                    return null;
                }
            }
            else {
                try {
                    this.wait(Servidor.ACESSO_MILIS + 1000L);
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

        if (filaPaginas.containsKey(servidor)) {
            if (urlAdd.getDepth() <= DEPTH_LIMIT && !filaPaginas.get(servidor).contains(urlAdd)) {
                filaPaginas.get(servidor).add(urlAdd);
                return true;
            } else {
                return false;
            }
        } else {
            LinkedList<URLAddress> fila = new LinkedList<>();
            fila.add(urlAdd);
            filaPaginas.put(servidor, fila);
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
            Logger.getLogger(EscalonadorSimples.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public void putRecorded(String domain, Record domainRec) {
        records.put(new Servidor(domain), domainRec);
    }

    @Override
    public boolean finalizouColeta() {
        return count > PAGES_LIMIT;
    }

    @Override
    public void countFetchedPage() {
        count++;
    }

}
