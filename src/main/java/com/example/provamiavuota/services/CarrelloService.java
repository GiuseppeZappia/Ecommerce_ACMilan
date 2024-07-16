package com.example.provamiavuota.services;

import com.example.provamiavuota.entities.*;
import com.example.provamiavuota.repositories.CarrelloRepository;
import com.example.provamiavuota.repositories.DettaglioCarrelloRepository;
import com.example.provamiavuota.repositories.ProdottoRepository;
import com.example.provamiavuota.repositories.UtenteRepository;
import com.example.provamiavuota.supports.exceptions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class CarrelloService {
    @Autowired
    private CarrelloRepository carrelloRepository;
    @Autowired
    private DettaglioCarrelloRepository dettaglioCarrelloRepository;
    @Autowired
    private UtenteRepository utenteRepository;
    @Autowired
    private ProdottoRepository prodottoRepository;
    @Autowired
    private OrdineService ordineService;

    @Transactional(readOnly = true)
    public List<DettaglioCarrello> mostraTutti(int numPagina, int dimPagina, String ordinamento, int idUtente) throws UtenteNonEsistenteONonValido, CarrelloNonValidoException {
        Sort.Direction tipoOrdinamento = Sort.Direction.DESC;
        Pageable paging = PageRequest.of(numPagina, dimPagina, Sort.by(tipoOrdinamento, ordinamento));
        Optional<Utente> opt = utenteRepository.findById(idUtente);
        if (opt.isEmpty() || !utentePresenteNelDb((Utente) opt.get())) {//controllo innanzitutto se esiste con quell'id e poi se gli altri campi presenti nell'ordine sono validi
            throw new UtenteNonEsistenteONonValido();
        }
        Utente u = (Utente) opt.get();
        Carrello c = u.getCarrello();
        if (c == null || !carrelloRepository.existsByIdAndAndAttivo(c.getId(), 1)) {
            throw new CarrelloNonValidoException();
        }
        //QUA HA SENSO PRENDERLO DAL DB O È MEGLIO PRENDERLO DAL CARRELLO CON LA GETLISTADETTAGLIORDINE   ??????
        Page<DettaglioCarrello> risultatiPagine = dettaglioCarrelloRepository.findByCarrello_Id(u.getCarrello().getId(), paging);
        if (risultatiPagine.hasContent()) {
            return risultatiPagine.getContent();
        } else {
            return new LinkedList<>();
        }
    }

    private boolean utentePresenteNelDb(Utente utente) {
        return utenteRepository.existsByNomeIgnoreCaseAndCognomeIgnoreCaseAndEmailIgnoreCaseAndUsernameIgnoreCaseAndPasswordAndPuntifedelta(utente.getNome(), utente.getCognome(), utente.getEmail(), utente.getUsername(), utente.getPassword(), utente.getPuntifedelta());
    }


    @Transactional(readOnly = false, rollbackFor = {CarrelloNonValidoException.class, ProdottoNonValidoException.class,QuantitaProdottoNonDisponibile.class})
    public Carrello aggiungiProdotto(int idCarrello, int idProdotto, int quantita) throws CarrelloNonValidoException, ProdottoNonValidoException, QuantitaProdottoNonDisponibile {
        Optional<Carrello> carr = carrelloRepository.findByIdAndAttivo(idCarrello,1);
        Optional<Prodotto> prod = prodottoRepository.findById(idProdotto);
        if (carr.isEmpty()) {
            throw new CarrelloNonValidoException();
        }
        if (prod.isEmpty()) {
            throw new ProdottoNonValidoException();
        }
        Carrello carrello = carr.get();
        Prodotto prodotto = prod.get();
        if (quantita < 0 || prodotto.getQuantita() < quantita) {
            throw new QuantitaProdottoNonDisponibile();
        }
        //se il prodotto è nascosto non glielo faccio inserire nel carrello, se pero la quantita è zero si, perche
        //è caso in cui ha messo giorni prima nel carrello prodotto che ora non è disponibile e se fa ordine c'è mio avviso che dice
        //elimina se vuoi fare ordine oppure aspetta che torna disponibile
        if(quantita!=0 && prodottoRepository.existsByNomeIgnoreCaseAndNascosto(prodotto.getNome(), 1)) {
            throw new ProdottoNonValidoException();
        }
        //SE IN QUEL CARRELLO C'È GIA QUEL PRODOTTO AGGIORNA LA QUANTITA, IN QUESTO MODO POSSO USARLO SIA
        //PER AGGIUNTE CHE RIMOZIONI DI ELEMENTI, IN CASO DI RIMOZIONE ELEMENTO (QUANTITA = 0) FACCIO
        if (dettaglioCarrelloRepository.existsByCarrello_IdAndProdotto_Id(idCarrello, idProdotto)) {
            DettaglioCarrello dettaglioCarrello = dettaglioCarrelloRepository.findByCarrello_IdAndProdotto_Id(idCarrello, idProdotto);
            if (quantita == 0) {//SE IL PRODOTTO È GIA PRESENTE E PASSO QUANTITA UGUALE A ZERO LO RIMUOVO
                carrello.getListaDettagliCarrello().remove(dettaglioCarrello);
                dettaglioCarrelloRepository.delete(dettaglioCarrello);

            } else {
                dettaglioCarrello.setQuantita(quantita);
            }
        } else {//SE IL PRODOTTO NON ERA GIA NEL CARRELLO AGGIUNGO SOLO NEL CASO FOSSE STATA PASSATA UNA QUANTITA MAGGIORE DI 0
            if (quantita != 0) {
                DettaglioCarrello dettaglioCarrello = new DettaglioCarrello();
                dettaglioCarrello.setCarrello(carrello);
                dettaglioCarrello.setProdotto(prodotto);
                dettaglioCarrello.setQuantita(quantita);
                dettaglioCarrello.setPrezzoUnitario(prodotto.getPrezzo());
                dettaglioCarrelloRepository.save(dettaglioCarrello);
                carrello.getListaDettagliCarrello().add(dettaglioCarrello);
            }
        }
        return carrello;
    }

    @Transactional(readOnly = false,rollbackFor = {UtenteNonEsistenteONonValido.class,
            CarrelloNonValidoException.class,ProdottoNonDisponibileAlMomento.class, ProdottoNonValidoException.class,
            QuantitaProdottoNonDisponibile.class})
    public Ordine acquista(int idUtente,int puntiUsati) throws UtenteNonEsistenteONonValido, CarrelloNonValidoException, ProdottoNonDisponibileAlMomento, ProdottoNonValidoException, QuantitaProdottoNonDisponibile, OrdineNonValido, MinimoPuntiRichiestoNonSoddisfatto, PuntiFedeltaNonDisponibili {
        Optional<Utente> u = utenteRepository.findById(idUtente);
        if (u.isEmpty()) {
            throw new UtenteNonEsistenteONonValido();
        }
        Utente utente=u.get();
        Carrello carrello = carrelloRepository.findActiveCarrelloByUtenteId(idUtente,1);
        if(carrello==null){
            throw new CarrelloNonValidoException();
        }
        if(carrello.getListaDettagliCarrello().isEmpty()){
            throw new OrdineNonValido();
        }
        Ordine ordine = new Ordine();
        ordine.setUtente(utente);
        ordine.setPuntiusati(puntiUsati);
        ordine.setListaDettagliOrdine(new LinkedList<>());//qui aggiungo uno per volta poi
        ordine.setData(new Date());

        double totale=0.0;

        for(DettaglioCarrello d : carrello.getListaDettagliCarrello()){
            //SE C'è MA è NASCOSTO DO AVVISO COSI O ELIMINA DAL CARRELLO L'ELEMENTO OPPURE LASCIA FINCHE NON TORNA DISPONIBILE IL PRODOTTO
            if(!prodottoRepository.existsByNomeIgnoreCaseAndNascosto(d.getProdotto().getNome(), 0)){
                throw new ProdottoNonDisponibileAlMomento();
            }
            Optional<Prodotto> prod = prodottoRepository.findById(d.getProdotto().getId());
            if(prod.isEmpty()){
                throw new ProdottoNonValidoException();
            }
            Prodotto prodotto=prod.get();
            if(d.getQuantita()<=0 || d.getQuantita()>prodotto.getQuantita()){
                throw new QuantitaProdottoNonDisponibile();
            }
            if(!Objects.equals(d.getPrezzoUnitario(), prodotto.getPrezzo())){
                throw new ProdottoNonValidoException();
            }
            DettaglioOrdine dettaglioOrdine=new DettaglioOrdine();
            dettaglioOrdine.setProdotto(prodotto);
            dettaglioOrdine.setQuantita(d.getQuantita());
            dettaglioOrdine.setPrezzoUnitario(d.getPrezzoUnitario());
            ordine.getListaDettagliOrdine().add(dettaglioOrdine);
            totale+=d.getQuantita()*d.getPrezzoUnitario();
            //DEVO RIMUOVERE DAL CARRELLO GLI ELEMENTI INSERITI NELL'ORDINE
            //NON CHIAMO METODO SOPRA AGGIUNGIPRODOTTO CON QUANTITA = 0 OPPURE
            //POTREI AVERE LA CONCURRENTMODIFICATIONEXCEPTION
            dettaglioCarrelloRepository.delete(d);
        }
        ordine.setTotale(totale);
        return  ordineService.salvaOrdine(ordine);
    }



}
