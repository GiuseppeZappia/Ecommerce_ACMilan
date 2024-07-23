package com.example.provamiavuota.services;

import com.example.provamiavuota.authentication.Utils;
import com.example.provamiavuota.entities.*;
import com.example.provamiavuota.repositories.*;
import com.example.provamiavuota.supports.exceptions.*;
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


    @Transactional(readOnly = false,rollbackFor = {OrdineNonValido.class,QuantitaProdottoNonDisponibile.class, PuntiFedeltaNonDisponibili.class, UtenteNonEsistenteONonValido.class, MinimoPuntiRichiestoNonSoddisfatto.class})
    public Ordine salvaOrdine(@NotNull Ordine ordine) throws QuantitaProdottoNonDisponibile, OrdineNonValido, PuntiFedeltaNonDisponibili, UtenteNonEsistenteONonValido, MinimoPuntiRichiestoNonSoddisfatto {
        boolean applicataPromozione = false;

        //serve per aggiunta punti ad utente che dipende se c'erano prod in promo nell'ordine o meno
        double totaleOrdineRicevuto= ordine.getTotale();

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
                applicataPromozione=true;//in modo tale da non dare i punti alla tessera dell'utente alla fine
                List<ProdottiPromo> prodPromoConScontoMax=prodPromoRepository.findPromozioneWithMaxScontoByProdotto(prodottoDaAcquistare);
                int percentualeScontoMax=prodPromoConScontoMax.get(0).getSconto();//col get(0) prendo la prima promozione ricevuta che coinvolge mio prodotto,
                                                                                  // ovvero la piu conveniente perche ordinate DECR da query
                double sconto= (((double) percentualeScontoMax/100) * prodottoDaAcquistare.getPrezzo());

                //aggiorno il prezzo sia per l'ordine che per il dettaglio ordine
                d.setPrezzoUnitario(prodottoDaAcquistare.getPrezzo()-sconto);
                ordineSalvato.setTotale(ordineSalvato.getTotale()-sconto*d.getQuantita()); //detraggo sconto moltiplicato per quantita acquistata
            }
            dettaglioOrdineRepository.save(d);
        }

        //PREZZO NON È QUELLO CHE DOVREBBE ESSERE
        if(totaleOrdineRicevuto!=totalePrevistoSenzaSconti){
            throw new OrdineNonValido();
        }

        int idUtenteOrdine=ordineSalvato.getUtente().getId();
        Optional<Utente> utente = utenteRepository.findById(idUtenteOrdine);
        //controllo innanzitutto se esiste con quell'id e poi se gli altri campi presenti nell'ordine sono validi
        if (utente.isEmpty() || !utentePresenteNelDb(ordineSalvato.getUtente())) {
            throw new UtenteNonEsistenteONonValido();
        }
        Utente u = utente.get();
        if ((applicataPromozione && ordineSalvato.getPuntiusati()!=0) || ordineSalvato.getPuntiusati() < 0 || ordineSalvato.getPuntiusati() > u.getPuntifedelta())//punti non utilizzabili con prodotti in promo VENGONO PASSATI PIU PUNTI DI QUANTI NE ABBIA L'UTENTE OPPURE UN NUMERO NEGATIVO
            throw new PuntiFedeltaNonDisponibili();
        //se non usa 0 punti e non soddisfa il minimo richiesto lancio eccezione
        if (ordineSalvato.getPuntiusati() !=0 && ordineSalvato.getPuntiusati() < minimoPuntiUsabili)
            throw new MinimoPuntiRichiestoNonSoddisfatto();
        int maxScontoPossibileDatiPunti = (int) Math.floor(ordineSalvato.getPuntiusati() / 2);
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
            double detrazione =Math.floor( (double) ordineSalvato.getPuntiusati() / 2);
            double nuovoTotale = ordineSalvato.getTotale() - detrazione;
            ordineSalvato.setTotale(nuovoTotale);//sottraggo sconto a totale
            u.setPuntifedelta(u.getPuntifedelta() - ordineSalvato.getPuntiusati());//sottraggo punti usati
        }
        ordineSalvato.setUtente(u);//cosi restituisco ordine con utente con punti aggiornati
        return ordineSalvato;
    }

    private boolean utentePresenteNelDb(Utente utente) {
        return utenteRepository.existsByNomeIgnoreCaseAndCognomeIgnoreCaseAndEmailIgnoreCaseAndPuntifedelta(utente.getNome(), utente.getCognome(), utente.getEmail(),utente.getPuntifedelta());
    }

    @Transactional(readOnly = true)
    public List<Ordine> getOrdiniInPeriodo(Date inizio, Date fine, int numPagina, int dimPagina, String ordinamento) throws RangeDateNonAccettabile, UtenteNonEsistenteONonValido {
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
    public List<Ordine> ordiniCliente(int numPagina, int dimPagina, String ordinamento) throws UtenteNonEsistenteONonValido{
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

    @Transactional(readOnly = false,rollbackFor = {OrdineNonValido.class,QuantitaProdottoNonDisponibile.class, PuntiFedeltaNonDisponibili.class, UtenteNonEsistenteONonValido.class, MinimoPuntiRichiestoNonSoddisfatto.class,TentativoNonAutorizzato.class,PuntiUsatiInAltroOrdineException.class})
    public void rimuoviOrdine(int idOrdine) throws OrdineNonPresenteNelDbExceptions, UtenteNonEsistenteONonValido, DettaglioOrdineNonValido, ProdottoNonValidoException, OrdineNonPiuAnnullabileException, TentativoNonAutorizzato, PuntiUsatiInAltroOrdineException {
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
            int puntiDaRestituire=utenteOrdine.getPuntifedelta()+ daEliminare.getPuntiusati();
            utenteOrdine.setPuntifedelta(puntiDaRestituire);//restituisco punti usati ad utente

            utenteOrdine.getOrdini().remove(daEliminare);//elimino questo tra i suoi ordini

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

                //SE NEL CARRELLO C'ERA GIA UNO DEI PRODOTTI PRESENTE NELL'ORDINE ANNULLATO
                //AUMENTO LA QUANTITA SENZA CREARE NUOVO DETTAGLIOCARRELLO O VIOLO VINCOLI UNIQUE ID_PRODOTTO-ID_CARRELLO
                if(dettaglioCarrelloRepository.existsByCarrello_IdAndProdotto_Id(utente.getCarrello().getId(), prodotto.getId())){
                    DettaglioCarrello dettaglioCarrello=dettaglioCarrelloRepository.findByCarrello_IdAndProdotto_Id(utente.getCarrello().getId(), prodotto.getId());
                    dettaglioCarrello.setQuantita(dettaglioCarrello.getQuantita()+d.getQuantita());
                }
                //SE NON C'È AGGIUNGO
                else {
                    DettaglioCarrello dettcarr = new DettaglioCarrello();
                    dettcarr.setProdotto(d.getProdotto());
                    dettcarr.setQuantita(d.getQuantita());
                    dettcarr.setPrezzoUnitario(prodotto.getPrezzo());
                    dettcarr.setCarrello(utenteOrdine.getCarrello());
                    dettaglioCarrelloRepository.save(dettcarr);
                }
                dettaglioOrdineRepository.delete(d);
            }
            //CONTROLLO SE DEVO TOGLIERE PUNTI AD UTENTE:
            //-SE NON ERANO STATI USATI PUNTI E NON ERANO STATE APPLICATE PROMOZIONI AI PRODOTTI, DEVO TOGLIERE I PUNTI CHE GLI AVEVO
            //AGGIUNTO CIOÈ PREZZOTOTALE/2
            //POTREBBE DARSI CHE L'UTENTE ABBIA USATO I PUNTI DI QUESTO ORDINE SUBITO PER UN ALTRO, QUINDI QUESTO VALORE SAREBBE NEGATIVO,
            //IN QUESTO CASO GLI DO ERRORE E GLI DICO DI ELIMINARE PRIMA ORDINE DOVE HA USATO PUNTI E POI QUESTO, OPPPURE LASCIA COSI.

            if(daEliminare.getPuntiusati()==0 && totaleSenzaScontiOPromo==daEliminare.getTotale()) {
                utenteOrdine.setPuntifedelta(utenteOrdine.getPuntifedelta()-(int )Math.floor(totaleSenzaScontiOPromo/2));
                if(utenteOrdine.getPuntifedelta()<0){
                    throw new PuntiUsatiInAltroOrdineException();
                }
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
