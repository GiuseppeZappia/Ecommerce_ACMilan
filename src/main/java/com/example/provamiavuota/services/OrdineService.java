package com.example.provamiavuota.services;

import com.example.provamiavuota.authentication.Utils;
import com.example.provamiavuota.entities.*;
import com.example.provamiavuota.repositories.*;
import com.example.provamiavuota.supports.exceptions.*;
import jdk.jshell.execution.Util;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class OrdineService {

    @Autowired
    private OrdineRepository ordineRepository;
    @Autowired
    private ProdottoRepository prodottoRepository;
    @Autowired
    private DettaglioOrdineRepository dettaglioOrdineRepository;
    @Autowired
    private UtenteRepository utenteRepository;
    @Autowired
    private ProdottiPromoRepository prodPromoRepository;
    @Value("${variabili.minimoPuntiUsabili}")
    private int minimoPuntiUsabili;
    @Autowired
    private DettaglioCarrelloRepository dettaglioCarrelloRepository;

    @Transactional(readOnly = true)
    public List<Ordine> mostraTutti(int numPagina, int dimPagina, String ordinamento) {
        Sort.Direction tipoOrdinamento = Sort.Direction.DESC;
        Pageable paging = PageRequest.of(numPagina, dimPagina, Sort.by(tipoOrdinamento, ordinamento));
        Page<Ordine> risultatiPagine = ordineRepository.findAll(paging);
        if (risultatiPagine.hasContent()) {
            return risultatiPagine.getContent();
        } else {
            return new LinkedList<>();
        }
    }


    //QUINDI LA SAVE LA FACCIO SOLO DELLE ENTITY CHE NON PRENDO DAL DB MA CHE CREO IO O RICEVO DA FE, NEGLI ALTRI CASI INVECE
    //NON FACCIO LA SAVE PERCHE SONO GESTITE IN AUTOMATICO
    //CON IL ROLLBACKFOR GESTISCO LE ECCEZIONI CHE QUESTO METODO LANCIA GARANTENDO CHE VENGANO EFFETTUATE LE ROLLBACK LASCIANDO IL DB CONSISTENTE
    @Transactional(readOnly = false,rollbackFor = {OrdineNonValido.class,QuantitaProdottoNonDisponibile.class, PuntiFedeltaNonDisponibili.class, UtenteNonEsistenteONonValido.class, MinimoPuntiRichiestoNonSoddisfatto.class})
    public Ordine salvaOrdine(@NotNull Ordine ordine) throws QuantitaProdottoNonDisponibile, OrdineNonValido, PuntiFedeltaNonDisponibili, UtenteNonEsistenteONonValido, MinimoPuntiRichiestoNonSoddisfatto {
        boolean applicataPromozione = false;
        double totaleOrdineRicevuto= ordine.getTotale();
        System.out.println("APPENA RICEVUTO "+ordine.getTotale());
        //serve per aggiunta punti ad utente che dipende se c'erano prod in promo nell'ordine o meno
        Ordine ordineSalvato=ordineRepository.save(ordine);
        //QUI CONTROLLO che l'ordine contenga qualche prodotto
        if(ordineSalvato.getListaDettagliOrdine().isEmpty()){
            throw new OrdineNonValido();
        }

        double totalePrevistoSenzaSconti=0.0;

        for (DettaglioOrdine d : ordineSalvato.getListaDettagliOrdine()) {
            Prodotto prodottoDaAcquistare = prodottoRepository.findById(d.getProdotto().getId()).orElse(null);
            //CONTROLLO CH ESISTA PRODOTTO, CHE QUANTITA SIANO VALIDE E CHE PREZZO PASSATOMI CORRISPONDA A QUELLO EFFETTIOVO PER QUEL PRODOTTO
            if (prodottoDaAcquistare == null || d.getQuantita()<=0 || d.getPrezzoUnitario()==null ||
                    d.getPrezzoUnitario()<=0|| !d.getPrezzoUnitario().equals(prodottoDaAcquistare.getPrezzo())){
                //controllo per verificare se i dettagli ordine sono ok, vanno fatti? se si, qui?
                System.out.println("IN ORDINE ERRORE MIO 1");
                throw new OrdineNonValido();
            }
            if (prodottoDaAcquistare.getQuantita() < d.getQuantita())
                throw new QuantitaProdottoNonDisponibile();

            //se sono qui sicuramente non sarà null il prodotto
            int nuovaQuantita = prodottoDaAcquistare.getQuantita() - d.getQuantita();
            prodottoDaAcquistare.setQuantita(nuovaQuantita);
            d.setProdotto(prodottoDaAcquistare);
            d.setOrdine(ordineSalvato);
            totalePrevistoSenzaSconti+=d.getQuantita()*d.getPrezzoUnitario();

            boolean prodottoInPromozione=prodPromoRepository.existsByProdottoAndAttiva(prodottoDaAcquistare);
            //verifico se quel prodotto è parte di qualche promo

            if (prodottoInPromozione){//se esiste almeno una promozione...
                applicataPromozione=true;//in modo tale da non dare i punti alla tessera dell'utente
                ProdottiPromo prodPromoConScontoMax=prodPromoRepository.findPromozioneWithMaxScontoByProdotto(prodottoDaAcquistare);
                int percentualeScontoMax=prodPromoConScontoMax.getSconto();
                double sconto= (((double) percentualeScontoMax/100) * prodottoDaAcquistare.getPrezzo());

                //aggiorno il prezzo sia per l'ordine che per il dettaglio ordine
                d.setPrezzoUnitario(prodottoDaAcquistare.getPrezzo()-sconto);
                ordineSalvato.setTotale(ordineSalvato.getTotale()-sconto*d.getQuantita()); //detraggo sconto moltiplicato per quantita acquistata
            }
            DettaglioOrdine salvatoDef=dettaglioOrdineRepository.save(d);
        }

        //PREZZO NON È QUELLO CHE DOVREBBE ESSERE
        if(totaleOrdineRicevuto!=totalePrevistoSenzaSconti){
            throw new OrdineNonValido();
        }
        //qua ha senso lasciare solo la findByid? perche se mi viene passato un id valido ma gli altri campi no come faccio? (in questo caso controllo come sotto)
        int idUtenteOrdine=ordineSalvato.getUtente().getId();
        Optional<Utente> utente = utenteRepository.findById(idUtenteOrdine);
        if (utente.isEmpty() || !utentePresenteNelDb(ordineSalvato.getUtente())) {//controllo innanzitutto se esiste con quell'id e poi se gli altri campi presenti nell'ordine sono validi
            throw new UtenteNonEsistenteONonValido();
        }
        Utente u = utente.get();
        if ((applicataPromozione && ordineSalvato.getPuntiusati()!=0) || ordineSalvato.getPuntiusati() < 0 || ordineSalvato.getPuntiusati() > u.getPuntifedelta())//punti non utilizzabili con prodotti in promo VENGONO PASSATI PIU PUNTI DI QUANTI NE ABBIA L'UTENTE OPPURE UN NUMERO NEGATIVO
            throw new PuntiFedeltaNonDisponibili();
        if (ordineSalvato.getPuntiusati() !=0 && ordineSalvato.getPuntiusati() < minimoPuntiUsabili)//se non usa 0 punti e non soddisfa il minimo richiesto lancio eccezione
            throw new MinimoPuntiRichiestoNonSoddisfatto();
        int maxScontoPossibileDatiPunti = ordineSalvato.getPuntiusati() / 2;
        if (maxScontoPossibileDatiPunti > ordineSalvato.getTotale()) {//i punti coprono una cifra maggiore rispetto l'ordine
            int puntiUsati = (int) Math.ceil(ordineSalvato.getTotale() * 2);
            ordineSalvato.setPuntiusati(puntiUsati);//aggiorno i punti usati per l'ordine perche non li usa tutti l'utente in quanto ne aveva di piu
            ordineSalvato.setTotale(0.0);//gratis perche avevi massimo punti
            u.setPuntifedelta(u.getPuntifedelta() - puntiUsati);//rimuovo ad utente punti usati e non ne aggiungo perche ha usato punti per questo ordine
        } else {
            if (ordineSalvato.getPuntiusati() == 0 && !applicataPromozione) {
                u.setPuntifedelta(u.getPuntifedelta() + (int) Math.ceil(ordineSalvato.getTotale()) / 2);
                //se non ha usato i punti e non c'erano prodotti in promo gli aggiungo quelli che gli spettano
            }
            //qui ci vado anche se usa zero punti ma tanto non succede nulla, lascia tutto inalterato
            double detrazione = (double) ordineSalvato.getPuntiusati() / 2;
            double nuovoTotale = ordineSalvato.getTotale() - detrazione;
            ordineSalvato.setTotale(nuovoTotale);//sottraggo sconto a totale
            u.setPuntifedelta(u.getPuntifedelta() - ordineSalvato.getPuntiusati());//sottraggo punti usati
        }
        //qua se rimetti save vedi che non è u ma utentepuntiaggiornati
        ordineSalvato.setUtente(u);//cosi restituisco ordine con utente con punti aggiornati
        return ordineSalvato;
    }

    private boolean utentePresenteNelDb(Utente utente) {
        return utenteRepository.existsByNomeIgnoreCaseAndCognomeIgnoreCaseAndEmailIgnoreCaseAndPuntifedelta(utente.getNome(), utente.getCognome(), utente.getEmail(),utente.getPuntifedelta());
    }

    @Transactional(readOnly = true)
    public List<Ordine> getOrdiniInPeriodo(//Utente u,
                                           Date inizio, Date fine, int numPagina, int dimPagina, String ordinamento) throws RangeDateNonAccettabile, UtenteNonEsistenteONonValido {
        if(inizio.after(fine)){
            throw new RangeDateNonAccettabile();
        }
        if(!utenteRepository.existsById(Utils.getIdUtente()) ){
            throw new UtenteNonEsistenteONonValido();
        }
        Optional<Utente> u= utenteRepository.findById(Utils.getIdUtente());
        if(u.isEmpty()){
            throw new UtenteNonEsistenteONonValido();
        }
        Utente utente=u.get();
        Sort.Direction tipoOrdinamento = Sort.Direction.DESC;
        Pageable paging = PageRequest.of(numPagina, dimPagina, Sort.by(tipoOrdinamento, ordinamento));
        Page<Ordine> risultatiPagine = ordineRepository.ricercaOrdiniInPeriodo(utente,inizio,fine,paging);
        if (risultatiPagine.hasContent()) {
            return risultatiPagine.getContent();
        } else {
            return new LinkedList<>();
        }
    }

    @Transactional(readOnly = true)
    public List<Ordine> ordiniCliente(//Utente u,
                                      int numPagina, int dimPagina, String ordinamento) throws UtenteNonEsistenteONonValido{
        if(!utenteRepository.existsById(Utils.getIdUtente())){
            throw new UtenteNonEsistenteONonValido();
        }
        Optional<Utente> utente=utenteRepository.findById(Utils.getIdUtente());
        if(utente.isEmpty()){
            throw new UtenteNonEsistenteONonValido();
        }
        Utente u=utente.get();
        Sort.Direction tipoOrdinamento = Sort.Direction.DESC;
        Pageable paging = PageRequest.of(numPagina, dimPagina, Sort.by(tipoOrdinamento, ordinamento));
        Page<Ordine> risultatiPagine = ordineRepository.findByUtente(u,paging);
        if (risultatiPagine.hasContent()) {
            return risultatiPagine.getContent();
        } else {
            return new LinkedList<>();
        }
    }

    @Transactional(readOnly = false,rollbackFor = {OrdineNonValido.class,QuantitaProdottoNonDisponibile.class, PuntiFedeltaNonDisponibili.class, UtenteNonEsistenteONonValido.class, MinimoPuntiRichiestoNonSoddisfatto.class,TentativoNonAutorizzato.class})
    public void rimuoviOrdine(int idOrdine) throws OrdineNonPresenteNelDbExceptions, UtenteNonEsistenteONonValido, DettaglioOrdineNonValido, ProdottoNonValidoException, OrdineNonPiuAnnullabileException, TentativoNonAutorizzato {
        Optional<Ordine> ordine = ordineRepository.findById(idOrdine);   //cerco ordine, alternativam potevo if ! ordineRepository.existsByid(idOrdine)
        if(ordine.isPresent()){
            Ordine daEliminare=ordine.get();
            Date adesso=new Date();
            Date dataOrdine=daEliminare.getData();
            long millisecondiTrascorsi=Math.abs(dataOrdine.getTime()-adesso.getTime());
            if(millisecondiTrascorsi>3600*1000){//è passata piu di un'ora dall'ordine
                throw new OrdineNonPiuAnnullabileException();
            }
            Utente utenteOrdine=utenteRepository.findById(daEliminare.getUtente().getId()).orElse(null);

            Optional<Utente> u=utenteRepository.findById(Utils.getIdUtente());
            if(u.isEmpty()){
                throw new UtenteNonEsistenteONonValido();
            }
            Utente utente=u.get();
            //SE RICEVO NULL O UTENTE PRESENTE NELL'ORDINE NON È QUELLO DEL TOKEN
            if(utenteOrdine==null||utenteOrdine.getId()!=utente.getId()){
                throw new UtenteNonEsistenteONonValido();
            }
            if(!utente.getOrdini().contains(ordine.get())){
                throw new TentativoNonAutorizzato();
            }
            System.out.println("l'ordine usava "+daEliminare.getPuntiusati());
            int puntiDaRestituire=utenteOrdine.getPuntifedelta()+ daEliminare.getPuntiusati();
            utenteOrdine.setPuntifedelta(puntiDaRestituire);//restituisco punti usati ad utente

            utenteOrdine.getOrdini().remove(daEliminare);//elimino questo tra i suoi ordini
            //RESTITUISCO SOLDI ???????
            double totaleSenzaScontiOPromo=0.0;
            for(DettaglioOrdine d: daEliminare.getListaDettagliOrdine()){
                DettaglioOrdine dettaglioDaEliminare=dettaglioOrdineRepository.findById(d.getId()).orElse(null);
                if(dettaglioDaEliminare==null){
                    throw new DettaglioOrdineNonValido();
                }
                Prodotto prodotto=prodottoRepository.findById(d.getProdotto().getId()).orElse(null);
                if(prodotto==null){
                    throw new ProdottoNonValidoException();
                }
                totaleSenzaScontiOPromo+=prodotto.getPrezzo()*d.getQuantita();
                int quantitaAggiornata=prodotto.getQuantita()+d.getQuantita();
                prodotto.setQuantita(quantitaAggiornata);//reimposto quantita disponibile prodotto
                prodotto.getDettaglioOrdini().remove(dettaglioDaEliminare);
                //ANCHE QUI, NECESSARIE LE SAVE???
//                prodottoRepository.save(prodotto);
                //RIMETTO NEL CARRELLO I PRODOTTI DELL'ORDINE ANNULLATO COSI IN CASO PUO RIFARE ORDINE
                DettaglioCarrello dettcarr=new DettaglioCarrello();
                dettcarr.setProdotto(d.getProdotto());
                dettcarr.setQuantita(d.getQuantita());
                dettcarr.setPrezzoUnitario(d.getPrezzoUnitario());
                dettcarr.setCarrello(utenteOrdine.getCarrello());
                dettaglioCarrelloRepository.save(dettcarr);

                dettaglioOrdineRepository.delete(d);
            }
            //CONTROLLO SE DEVO TOGLIERE PUNTI AD UTENTE:
            //-SE NON ERANO STATI USATI PUNTI E NON ERANO STATE APPLICATE PROMOZIONI AI PRODOTTI DEVO TOGLIERE I PUNTI CHE GLI AVEVO
            //AGGIUNTO, CHE SONO  PREZZOTOTALE/2

            System.out.println("TOTALE SENZA SCONTI O PROMO:  "+totaleSenzaScontiOPromo);
            System.out.println("TOTALE INVECE DA ELIMINARE: " +daEliminare.getTotale());
            if(daEliminare.getPuntiusati()==0 && totaleSenzaScontiOPromo==daEliminare.getTotale()) {
                utenteOrdine.setPuntifedelta(utenteOrdine.getPuntifedelta()-(int )Math.ceil(totaleSenzaScontiOPromo/2));
            }
            //ELIMINO ORDINE ALLA FINE
            ordineRepository.delete(daEliminare);
        }else{
            throw new OrdineNonPresenteNelDbExceptions();
        }
    }


    @Transactional(readOnly = true)
    public List<DettaglioOrdine> trovaDettagliOrdine(int idOrdine,int numPagina, int dimPagina,String ordinamento) throws OrdineNonPresenteNelDbExceptions, UtenteNonEsistenteONonValido, TentativoNonAutorizzato {
        Sort.Direction tipoOrdinamento = Sort.Direction.DESC;
        Pageable paging = PageRequest.of(numPagina, dimPagina, Sort.by(tipoOrdinamento, ordinamento));
        Optional<Ordine> ordine = ordineRepository.findById(idOrdine);
        if (ordine.isEmpty()) {
            throw new OrdineNonPresenteNelDbExceptions();
        }
        Utente utenteOrdine=utenteRepository.findById(ordine.get().getUtente().getId()).orElse(null);
        Optional<Utente> u=utenteRepository.findById(Utils.getIdUtente());
        if(u.isEmpty()){
            throw new UtenteNonEsistenteONonValido();
        }
        Utente utente=u.get();
        //SE RICEVO NULL O UTENTE PRESENTE NELL'ORDINE NON È QUELLO DEL TOKEN
        if(utenteOrdine==null||utenteOrdine.getId()!=utente.getId()){
            throw new UtenteNonEsistenteONonValido();
        }
        if(!utente.getOrdini().contains(ordine.get())){
            throw new TentativoNonAutorizzato();
        }
        Page<DettaglioOrdine> risultatiPagine = dettaglioOrdineRepository.findByOrdine(ordine.get(),paging);
        if (risultatiPagine.hasContent()) {
            return risultatiPagine.getContent();
        } else {
            return new LinkedList<>();
        }
    }


}
