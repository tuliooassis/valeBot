/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package crawler.escalonadorCurtoPrazo;

import com.trigonic.jrobotx.Record;
import com.trigonic.jrobotx.RobotExclusion;
import crawler.ColetorUtil;
import crawler.URLAddress;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;

/**
 *
 * @author aluno
 */
public class PageFetcher implements Runnable {

    private EscalonadorSimples escalonador = new EscalonadorSimples();
    private final String USERAGENT = "valeBot";

    @Override
    public void run() {
        URLAddress url = null;
        Record recordAllowRobots = null;
        while (!escalonador.finalizouColeta()) {
            try {
                url = escalonador.getURL();
                recordAllowRobots = escalonador.getRecordAllowRobots(url);

                if (url == null) continue;
                
                if (recordAllowRobots == null) {
                    RobotExclusion re = new RobotExclusion();
                    try {
                        recordAllowRobots = re.get(new URL(url.getAddress()), USERAGENT);
                        escalonador.putRecorded(url.getAddress(), recordAllowRobots);
                    } catch (MalformedURLException ex) {
                        escalonador.putBlackDominios(url);
                        Logger.getLogger(PageFetcher.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                if (recordAllowRobots.allows(url.getPath())) {
//                    System.out.println(Thread.currentThread().getName() + " [TENTANDO] \t\t\t\t\t\t" + url.getAddress());

                    InputStream is = ColetorUtil.getUrlStream(USERAGENT, new URL(url.getAddress()));
                    String page = ColetorUtil.consumeStream(is);
                    HtmlCleaner hc = new HtmlCleaner();
                    TagNode tag = hc.clean(page);
                    TagNode[] urlNodes = tag.getElementsHavingAttribute("href", true);
                    for (int i = 0; i < urlNodes.length; i++) {
                        String urlToCollect = urlNodes[i].getAttributeByName("href");
                        
                        urlToCollect = ColetorUtil.getAbsoluteURL(url, urlToCollect);

                        if (ColetorUtil.excludeURL(urlToCollect))
                            continue;
                        
                        escalonador.adicionaNovaPagina(new URLAddress(urlToCollect, url.getDepth() + 1));
                    }
                    
                    System.out.println(Thread.currentThread().getName() + " [PAG CONSUMIDA]" + "\tCOUNT: " + escalonador.getCount() + "\tDEPTH: " + url.getDepth() + "\t" + url.getAddress());
                    escalonador.putFetchedPage(url);
                    escalonador.countFetchedPage();

                }
            } catch (Exception ex) {
                escalonador.putBlackDominios(url);
            }
        }
        System.out.println(Thread.currentThread().getName() + " [FINALIZADA]");
     }

}
