package com.example.provamiavuota.services;

import com.example.provamiavuota.authentication.Utils;
import com.example.provamiavuota.dto.CarrelloDto;
import com.example.provamiavuota.dto.DettaglioDto;
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
    public List<DettaglioCarrello> mostraTutti(int numPagina, int dimPagina, String ordinamento, int idUtente) throws UtenteNonEsistenteONonValido, CarrelloNonValidoException, TentativoNonAutorizzato {
        int idUt = Utils.getIdUtente();
        if (idUtente != idUt) {
            throw new TentativoNonAutorizzato();
        }
        Sort.Direction tipoOrdinamento = Sort.Direction.DESC;
        Pageable paging = PageRequest.of(numPagina, dimPagina, Sort.by(tipoOrdinamento, ordinamento));
        Optional<Utente> opt = utenteRepository.findById(idUtente);

        //controllo innanzitutto se esiste con quell'id e poi se gli altri campi presenti nell'ordine sono validi
        if (opt.isEmpty() || !utentePresenteNelDb((Utente) opt.get())) {
            throw new UtenteNonEsistenteONonValido();
        }
        Utente u = (Utente) opt.get();
        Carrello c = u.getCarrello();
        if (c == null || !carrelloRepository.existsByIdAndAndAttivo(c.getId(), 1)) {
            throw new CarrelloNonValidoException();
        }
        Page<DettaglioCarrello> risultatiPagine = dettaglioCarrelloRepository.findByCarrello_Id(u.getCarrello().getId(), paging);
        if (risultatiPagine.hasContent()) {
            return risultatiPagine.getContent();
        } else {
            return new LinkedList<>();
        }
    }

    private boolean utentePresenteNelDb(Utente utente) {
        return utenteRepository.existsByNomeIgnoreCaseAndCognomeIgnoreCaseAndEmailIgnoreCaseAndPuntifedelta(utente.getNome(), utente.getCognome(), utente.getEmail(), utente.getPuntifedelta());
    }


    @Transactional(readOnly = false, rollbackFor = {CarrelloNonValidoException.class, ProdottoNonValidoException.class, QuantitaProdottoNonDisponibile.class, TentativoNonAutorizzato.class})
    public Carrello aggiungiProdotto(int idUtente, int idProdotto, int quantita) throws CarrelloNonValidoException, ProdottoNonValidoException, QuantitaProdottoNonDisponibile, TentativoNonAutorizzato {
        int idUt = Utils.getIdUtente();
        if (idUtente != idUt) {
            throw new TentativoNonAutorizzato();
        }
        Optional<Utente> utente = utenteRepository.findById(Utils.getIdUtente());
        Carrello carr = carrelloRepository.findActiveCarrelloByUtenteId(idUtente, 1);
        Optional<Prodotto> prod = prodottoRepository.findById(idProdotto);
        if (carr == null) {
            throw new CarrelloNonValidoException();
        }
        if (utente.isPresent()) {
            if (utente.get().getCarrello().getId() != carr.getId()) {
                throw new TentativoNonAutorizzato();
            }
        }
        if (prod.isEmpty()) {
            throw new ProdottoNonValidoException();
        }
        Prodotto prodotto = prod.get();
        if (quantita < 0 || prodotto.getQuantita() < quantita) {
            throw new QuantitaProdottoNonDisponibile();
        }
        //se il prodotto è nascosto non glielo faccio inserire nel carrello, se pero la quantita è zero si, perche
        //è caso in cui ha messo giorni prima nel carrello prodotto che ora non è disponibile e se fa ordine c'è mio avviso che dice
        //elimina se vuoi fare ordine oppure aspetta che torna disponibile
        //in realta per come ho pensato il FE questo problema nemmeno si pone perche non mostro prodotti nascosti
        if (quantita != 0 && prodottoRepository.existsByNomeIgnoreCaseAndNascosto(prodotto.getNome(), 1)) {
            throw new ProdottoNonValidoException();
        }

        //SE IN QUEL CARRELLO C'È GIA QUEL PRODOTTO AGGIORNA LA QUANTITA, IN QUESTO MODO POSSO USARLO SIA
        //PER AGGIUNTE CHE RIMOZIONI DI ELEMENTI, IN CASO DI RIMOZIONE ELEMENTO (QUANTITA = 0) FACCIO
        if (dettaglioCarrelloRepository.existsByCarrello_IdAndProdotto_Id(carr.getId(), idProdotto)) {
            DettaglioCarrello dettaglioCarrello = dettaglioCarrelloRepository.findByCarrello_IdAndProdotto_Id(carr.getId(), idProdotto);
            if (quantita == 0) {//SE IL PRODOTTO È GIA PRESENTE E PASSO QUANTITA UGUALE A ZERO LO RIMUOVO
                carr.getListaDettagliCarrello().remove(dettaglioCarrello);
                dettaglioCarrelloRepository.delete(dettaglioCarrello);

            } else {
                dettaglioCarrello.setQuantita(quantita);
            }
        } else {//SE IL PRODOTTO NON ERA GIA NEL CARRELLO AGGIUNGO SOLO NEL CASO FOSSE STATA PASSATA UNA QUANTITA MAGGIORE DI 0
            if (quantita != 0) {
                DettaglioCarrello dettaglioCarrello = new DettaglioCarrello();
                dettaglioCarrello.setCarrello(carr);
                dettaglioCarrello.setProdotto(prodotto);
                dettaglioCarrello.setQuantita(quantita);
                dettaglioCarrello.setPrezzoUnitario(prodotto.getPrezzo());
                dettaglioCarrelloRepository.save(dettaglioCarrello);
                carr.getListaDettagliCarrello().add(dettaglioCarrello);
            }
        }
        return carr;
    }

    @Transactional(readOnly = false, rollbackFor = {QuantitaProdottoNonDisponibile.class, OrdineNonValido.class, MinimoPuntiRichiestoNonSoddisfatto.class, PuntiFedeltaNonDisponibili.class, UtenteNonEsistenteONonValido.class, ProdottoNonValidoException.class, CarrelloNonValidoException.class, TentativoNonAutorizzato.class})
    public Ordine acquista(CarrelloDto carrelloDto, int puntiUsati) throws QuantitaProdottoNonDisponibile, OrdineNonValido,
            MinimoPuntiRichiestoNonSoddisfatto, PuntiFedeltaNonDisponibili,
            UtenteNonEsistenteONonValido, ProdottoNonValidoException, CarrelloNonValidoException, TentativoNonAutorizzato {
        int idUt = Utils.getIdUtente();
        //UTENTE PASSATOMI NEL DTO != DA QUELLO ATTUALE CHE VEDO DA TOKEN
        if (idUt != carrelloDto.idUtente()) {
            throw new TentativoNonAutorizzato();
        }
        //UTENTE NON ESISTENTE NEL DB
        Optional<Utente> u = utenteRepository.findById(idUt);
        if (u.isEmpty()) {
            throw new UtenteNonEsistenteONonValido();
        }
        Utente utente = u.get();
        //PRENDO CARRELLO NEL DB PER VERIFICARE SE I PRODOTTI CHE HO NEI DUE CARRELLI SONO UGUALI O MENO, ALTRIMENTI
        //C'È PROBLEMA MOGLIE COLLANA, MARITO CANNA DA PESCA
        Carrello carrelloBE = carrelloRepository.findActiveCarrelloByUtenteId(idUt, 1);
        if (carrelloBE == null) {
            throw new CarrelloNonValidoException();
        }
        //SE NEL DB IL MIO CARRELLO È VUOTO GIA NON HA SENSO ANDARE AVANTI, L'ORDINE NON È VALIDO
        if (carrelloBE.getListaDettagliCarrello().isEmpty()) {
            throw new OrdineNonValido();
        }

        //CREO ORDINE DA SALVARE
        Ordine ordine = new Ordine();
        ordine.setUtente(utente);
        ordine.setPuntiusati(puntiUsati);
        ordine.setListaDettagliOrdine(new LinkedList<>());//qui aggiungo uno per volta poi
        ordine.setData(new Date());
        double totale = 0.0;
        for (DettaglioDto dettaglioFE : carrelloDto.listaDettagli()) {
            //SE C'È MA È NASCOSTO DO AVVISO COSI O ELIMINA DAL CARRELLO L'ELEMENTO OPPURE LASCIA FINCHE NON TORNA DISPONIBILE IL PRODOTTO
            //(NEL FE IN REALTA NON FACCIO PROPRIO VEDERE NELLO STORE I PRODOTTI NASCOSTI QUINDI NEMMENO C'È QUESTO PROBLEMA)
            int idProdotto = dettaglioFE.idProdotto();
            if (idProdotto < 0) {
                throw new ProdottoNonValidoException();
            }
            //CERCO SE ESISTE PRODOTTO
            Optional<Prodotto> prod = prodottoRepository.findById(idProdotto);
            if (prod.isEmpty()) {
                throw new ProdottoNonValidoException();
            }
            //SE ESISTE PRENDO PRODOTTO
            Prodotto prodotto = prod.get();

            //SE I DETTAGLI ORDINE SONO DIVERSI A LIVELLO DI DIMENSIONE VUOL DIRE CHE HO PROPRIO IL CASO
            //IN CUI LA MOGLIE HA MESSO NEL CARRELLO PRODOTTO QUINDI NEL DB HO PIU' PRODOTTI RISPETTO
            //A QUELLI CHE IL MARITO MI STA PASSANDO DAL FE OVVERO CHE VEDE LUI
            if (carrelloBE.getListaDettagliCarrello().size() != carrelloDto.listaDettagli().size()) {
                throw new OrdineNonValido();
            }
            //NON BASTA COME CONTROLLO, POTREBBERO AVERE STESSA DIMENSIONE MA DI UN PRODOTTO HO QUANTITA DIVERSE,
            //OPPURE STESSA DIMENSIONE MA I PRODOTTI NEI DUE CARRELLI POTREBBERO NON CORRISPONDERE
            //CONTROLLO QUINDI

            //SE NEL MIO CARRELO LATO BE NON ESISTE UN DETTAGLIO ORDINE CHE HA STESSO PRODOTTO CON STESSA QUANTITA DI QUELLO
            //CHE STO CONSIDERANDO OVVIAMENTE NON VA BENE
            if (!dettaglioCarrelloRepository.existsByCarrello_IdAndProdotto_IdAndQuantitaAndPrezzoUnitario(carrelloBE.getId(),
                    prodotto.getId(), dettaglioFE.quantita(), dettaglioFE.prezzoUnitario())) {
                throw new OrdineNonValido();
            }
            DettaglioCarrello dettaglioBE = dettaglioCarrelloRepository.findByCarrello_IdAndProdotto_Id(carrelloBE.getId(), prodotto.getId());

            //CHECK SULLE QUANTITA (LO FACCIO CON IL DETTAGLIO LATO BE, MA POCO CAMBIA VISTO CHE HO FATTO CHECK PRIMA PER VEDERE
            //SE I DUE CORRISPONDEVANO
            if (dettaglioBE.getQuantita() <= 0 || dettaglioBE.getQuantita() > prodotto.getQuantita()) {
                throw new QuantitaProdottoNonDisponibile();
            }
            //PREZZO NEL DETTAGLIO CARRELLO DIVERSO DA QUELLO VERO DEL PRODOTTO
            if (!Objects.equals(dettaglioBE.getPrezzoUnitario(), prodotto.getPrezzo())) {
                throw new ProdottoNonValidoException();
            }

            DettaglioOrdine dettaglioOrdine = new DettaglioOrdine();
            dettaglioOrdine.setProdotto(prodotto);
            dettaglioOrdine.setQuantita(dettaglioBE.getQuantita());
            dettaglioOrdine.setPrezzoUnitario(dettaglioBE.getPrezzoUnitario());
            ordine.getListaDettagliOrdine().add(dettaglioOrdine);
            totale += dettaglioBE.getQuantita() * dettaglioBE.getPrezzoUnitario();
            //DEVO RIMUOVERE DAL CARRELLO GLI ELEMENTI INSERITI NELL'ORDINE
            //NON CHIAMO METODO SOPRA AGGIUNGIPRODOTTO CON QUANTITA = 0 OPPURE
            //POTREI AVERE LA CONCURRENTMODIFICATIONEXCEPTION
            dettaglioCarrelloRepository.delete(dettaglioBE);
        }
        ordine.setTotale(totale);
        return ordineService.salvaOrdine(ordine);

    }


}
