package com.example.provamiavuota.services;

import com.example.provamiavuota.entities.ProdottiPromo;
import com.example.provamiavuota.entities.Prodotto;
import com.example.provamiavuota.repositories.ProdottiPromoRepository;
import com.example.provamiavuota.repositories.ProdottoRepository;
import com.example.provamiavuota.repositories.PromozioneRepository;
import com.example.provamiavuota.supports.exceptions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Service
public class ProdottoService {

    @Value("${variabili.listaStringhe}")
    private List<String> categorieValide= new LinkedList<>();
    //CATEGORIE VALIDE CONTENUTE NEL YML COME VARIABILI ACCESSIBILI OVUNQUE NEL CODICE

    @Autowired
    private ProdottoRepository prodottoRepository;
    @Autowired
    private ProdottiPromoRepository prodottiPromoRepository;
    @Autowired
    private PromozioneRepository promozioneRepository;


    @Transactional(readOnly = true)
    public List<Prodotto> elencoProdotti(int numPagina,int dimPagina,String ordinamento) {
        Sort.Direction tipoOrdinamento=Sort.Direction.ASC;
        Pageable paging = PageRequest.of(numPagina, dimPagina, Sort.by(tipoOrdinamento,ordinamento));
        Page<Prodotto> risultatiPagine = prodottoRepository.findAllByNascosto(paging,0);//cerco tutti   quelli non elmiinati
        if (risultatiPagine.hasContent()){
            return risultatiPagine.getContent();
        }
        else {
            return new LinkedList<>();
        }
    }

    @Transactional(readOnly = true)
    public List<Prodotto> elencoProdottiPerCategoria(String categoria,int numPagina,int dimPagina,String ordinamento) {
        if(categoria==null){//se non mi viene passata la specifica categoria come RequiredParam, mostro tutti i prodotti ordinati in base alla categoria
                            //non metto default value nel requestparam perche mica so quale categoria vuole vedere, le mostro tutte. Mentre con numero di
                            // pagina potrei mettere dafault tanto mostro quale pagina voglio io
            return this.elencoProdotti(numPagina,dimPagina,"categoria");
        }
        else{//se invece mi viene passata la categoria mi limito a fare ricerca in base a quella
            Sort.Direction tipoOrdinamento=Sort.Direction.ASC;
            Pageable paging = PageRequest.of(numPagina, dimPagina,Sort.by(tipoOrdinamento,ordinamento));
            Page<Prodotto> risultatiPagine = prodottoRepository.findByCategoriaContainingIgnoreCaseAndNascosto(categoria,paging,0);
            if (risultatiPagine.hasContent()){
                return risultatiPagine.getContent();
            }
            else {
                return new LinkedList<>();
            }
        }
    }

    @Transactional(readOnly = true)
    public Prodotto trovaProdottoByNome(String nomeProdotto){
        Prodotto p= prodottoRepository.findByNomeIgnoreCaseAndNascosto(nomeProdotto,0);
        return p;
    }

    @Transactional(readOnly = true)
    public List<Prodotto> ricercaAvanzata(int numPagina,int dimPagina,double prezzoMin,double prezzoMax,String nome,String categoria, int quantita,String ordinamento) throws FasciaDiPrezzoNonValidaException {
        if(prezzoMax<=0||prezzoMin<=0 ||prezzoMax<prezzoMin ){
            throw new FasciaDiPrezzoNonValidaException();
        }
        Sort.Direction tipoOrdinamento=Sort.Direction.ASC;
        Pageable paging = PageRequest.of(numPagina, dimPagina,Sort.by(tipoOrdinamento,ordinamento));
        Page<Prodotto> risultatiPagine = prodottoRepository.ricercaAvanzata(prezzoMin,prezzoMax,nome,categoria,quantita,paging);
        if (risultatiPagine.hasContent()){
            return risultatiPagine.getContent();
        }
        else{
            return new LinkedList<>();
        }
    }

    @Transactional(readOnly = false,rollbackFor = Exception.class)
    public void salvaProdotto(Prodotto prodotto) throws ProdottoGiaEsistenteException, FasciaDiPrezzoNonValidaException, ProdottoNonValidoException, CategoriaNonValidaException {
        if(prodotto.getNome()!=null && prodottoRepository.existsByNomeIgnoreCaseAndNascosto(prodotto.getNome(),0)){
            throw new ProdottoGiaEsistenteException();
        }
        if(prodotto.getPrezzo()<=0){
            throw new FasciaDiPrezzoNonValidaException();
        }
        if(prodotto.getQuantita()<=0){
            throw new ProdottoNonValidoException();
        }
        prodotto.setNascosto(0);//NASCOSTO SOLO DOPO ELIMINAZIONE
        if(!categorieValide.contains(prodotto.getCategoria())){
            throw new CategoriaNonValidaException();
        }
        prodottoRepository.save(prodotto);
    }

    @Transactional(readOnly = false,rollbackFor = Exception.class)
    public void rimuoviProdotto(int idProdotto) throws ProdottoNonPresenteNelDbExceptions, ProdottoGiaEliminatoException {
        Optional<Prodotto> prodotto = prodottoRepository.findById(idProdotto);   //cerco il prodotto, alternativam potevo if ! prodrepo.existsByid(idProdotto)
        if(prodotto.isPresent()){
            Prodotto daEliminare=prodotto.get();
            if(daEliminare.getNascosto()==1){
                throw new ProdottoGiaEliminatoException();
            }
            //elimino i prodotti_promo che lo coinvolgono
            for(ProdottiPromo pp: daEliminare.getDettaglioPromozioni()){
                Optional<ProdottiPromo> prodPromoDaEliminare= prodottiPromoRepository.findById(pp.getId());
                if(prodPromoDaEliminare.isPresent()){
                    ProdottiPromo eliminare=prodPromoDaEliminare.get();
                    prodottiPromoRepository.delete(eliminare);
                    //non faccio operazioni sulla promozione che magari può rimanere senza
                    // prodotti perche potrebbe sempre vedersi assegnati nuovi prodotti
                }
            }
            daEliminare.setNascosto(1);
        }else{
            throw new ProdottoNonPresenteNelDbExceptions();
        }
    }


    //DEVO MOSTRARE QUELLI NON PIU IN VENDITA O NO? FACCIO CHIAMATA DOVE CONTROLLO ANCHE SE HIDEEN??
    @Transactional(readOnly = true)
    public List<Prodotto> getPreferiti(int numPagina,int dimPagina,String ordinamento){
        Sort.Direction tipoOrdinamento=Sort.Direction.ASC;
        Pageable paging = PageRequest.of(numPagina, dimPagina, Sort.by(tipoOrdinamento,ordinamento));
        Page<Prodotto> risultatiPagine = prodottoRepository.findAllByPreferito(paging,1);//cerco tutti i preferiti
        if (risultatiPagine.hasContent()){
            return risultatiPagine.getContent();
        }
        else {
            return new LinkedList<>();
        }

    }


    @Transactional(readOnly = false)
    public String aggiuntaAiPreferiti(int idProdotto) throws ProdottoNonPresenteNelDbExceptions {
        Optional<Prodotto> daAggiungereAiPreferiti=prodottoRepository.findById(idProdotto);
        if(daAggiungereAiPreferiti.isPresent() ){
            Prodotto p=daAggiungereAiPreferiti.get();
            if(p.getNascosto()==1){
                throw new ProdottoNonPresenteNelDbExceptions();
            }
            if(p.getPreferito()==0){
                p.setPreferito(1);
                return "aggiunto";
            }
            else{
                p.setPreferito(0);
                return "rimosso";
            }
        }
        throw new ProdottoNonPresenteNelDbExceptions();
    }


}