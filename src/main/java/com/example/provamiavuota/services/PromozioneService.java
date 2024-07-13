package com.example.provamiavuota.services;

import com.example.provamiavuota.entities.ProdottiPromo;
import com.example.provamiavuota.entities.Prodotto;
import com.example.provamiavuota.entities.Promozione;
import com.example.provamiavuota.repositories.ProdottiPromoRepository;
import com.example.provamiavuota.repositories.ProdottoRepository;
import com.example.provamiavuota.repositories.PromozioneRepository;
import com.example.provamiavuota.supports.exceptions.*;
import com.example.provamiavuota.supports.exceptions.PromozioneGiaPresenteException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.Null;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Service
public class PromozioneService {

    @Autowired
    private PromozioneRepository promozioneRepository;
    @Autowired
    private ProdottoRepository prodottoRepository;
    @Autowired
    private ProdottiPromoRepository prodottiPromoRepository;

    @Transactional(readOnly = true)
    public List<Promozione> elencoPromozioni(int numPagina, int dimPagina, String ordinamento) {
        Sort.Direction tipoOrdinamento=Sort.Direction.ASC;
        Pageable paging = PageRequest.of(numPagina, dimPagina, Sort.by(tipoOrdinamento,ordinamento));
        Page<Promozione> risultatiPagine = promozioneRepository.findByAttiva(1,paging);//cerco tutte le promo attive
        if (risultatiPagine.hasContent()){
            return risultatiPagine.getContent();
        }
        else {
            return new LinkedList<>();
        }
    }

    @Transactional(readOnly = false)
    public Promozione aggiungiProdotto(int idPromozione, int idProdotto, int percentualeSconto) throws PromozioneNonPresenteException, ProdottoNonPresenteNelDbExceptions, PromozioneNonAttivaException, PercentualeScontoNonValidaExceptions, ProdottoGiaPresenteNellaPromozioneException {
        Optional<Promozione> ricercaPromozione = promozioneRepository.findById(idPromozione);
        if(percentualeSconto<=0 || percentualeSconto>100){
            throw new PercentualeScontoNonValidaExceptions();
        }
        if (ricercaPromozione.isEmpty()){
            throw new PromozioneNonPresenteException();
        }
        Optional<Prodotto> ricercaProdotto= prodottoRepository.findById(idProdotto);
        if(ricercaProdotto.isEmpty()){
            throw new ProdottoNonPresenteNelDbExceptions();
        }
        Promozione promozione = ricercaPromozione.get();
        Prodotto prodotto=ricercaProdotto.get();
        if(promozione.getAttiva()==0){
            throw new PromozioneNonAttivaException();
        }
        boolean giaPresente= prodottiPromoRepository.existsByProdottoAndPromo(prodotto,idPromozione);
        if(giaPresente){
            throw new ProdottoGiaPresenteNellaPromozioneException();
        }

        ProdottiPromo prodottiPromo=new ProdottiPromo();
        prodottiPromo.setProdotto(prodotto);
        prodottiPromo.setPromozione(promozione);
        prodottiPromo.setSconto(percentualeSconto);
        prodottiPromoRepository.save(prodottiPromo);
        return promozione;
    }


    //DEVO CONTROLLARE NON È CHE MI VIENE PASSATA CON ID GIA IMPOSTATO E QUINDI NON VALIDO PERHCE MAGARI SUS? STESSA COSA IN CASO PER ORDINI?
    @Transactional(readOnly = false,rollbackFor = {PromozioneNonPresenteException.class,PromozioneNonValida.class})
    public Promozione creaNuovaPromo(Promozione p) throws PromozioneNonValida, PromozioneGiaPresenteException, ProdottoNonPresenteNelDbExceptions, PromozioneNonPresenteException, PercentualeScontoNonValidaExceptions, PromozioneNonAttivaException, ProdottoGiaPresenteNellaPromozioneException {
        if(p==null){
            throw new PromozioneNonValida();
        }
        //MI BASTA FARE QUESTI CONTROLLI PERCHE O ESISTE GIA CON QUELL'ID O AL MASSIMO CON IL NOME, POI NO LE CHAIVI SONO QUESTE
        boolean esisteGia=(promozioneRepository.existsById(p.getId()) || promozioneRepository.existsByNomeAndAttiva(p.getNome(),1));
        if(esisteGia){
            throw new PromozioneGiaPresenteException();
        }
        //SE ESISTE MA NON È ATTIVA NON ME NE FREGA, LA CREO NUOVA E NON ATTIVO QUELLA DISATTIVATA PERCHE
        // MAGARI PRODOTTI SONO DIVERSI ECC E QUELLA IN FUTURO POTREBBE VENIRE ATTIVATA CAMBIANDO DATE
        Date inizio=p.getInizio();
        Date fine=p.getFine();
        if(inizio.after(fine) || p.getDettagli()== null){
            throw new PromozioneNonValida();
        }
        //SE NON HA PRODOTTI LA AGGIUNGO PERCHE MAGARI POI VENGONO SETTATI DOPO
        // (A PROPOSITO FAI PROVA DI COSA SUCCEDE SE FAI ELENCO PRODOTTI DI UNA PROMO SENZA PRODOTTI DENTRO)
        if(p.getProdottiPromozione().isEmpty()){
           p.setAttiva(1);
           Promozione salvata=promozioneRepository.save(p);
           return salvata;
        }

        p.setAttiva(1);//SALVO PREVENTIVAMENTE IN MODO DA POTER SALVARE SE VALIDI ANCHE I PRODOTTIPROMO
        Promozione salvata=promozioneRepository.save(p);

        //SE ARRIVO QUI MI È STATA MANDATA UNA PROMO CON PRODOTTI DENTRO E DEVO VEDERE SE ESISTONO
        //INOLTRE NON DEVO CONTROLLARE CHE DETTAGLIOPROMO ESISTA GIA PERCHE LA PROMO NON ESISTEVA QUINDI SICURO NON È PRESENTE NEL DB
        for(ProdottiPromo prodPromo : p.getProdottiPromozione()){
                Prodotto prodotto=prodPromo.getProdotto();
                if(! prodottoRepository.existsById(prodotto.getId())){
                    throw new ProdottoNonPresenteNelDbExceptions();
                }
                if(prodPromo.getSconto()<=0 || prodPromo.getSconto()>100){
                    throw new PromozioneNonValida();
                }
                prodPromo.setPromozione(p);
                prodPromo.setProdotto(prodotto);
                prodottiPromoRepository.save(prodPromo);
                //CONTROLLA SE AL PRODOTTO CON LA PROVA COME IERI INSERISCE NELLA LISTA I PRODOTTI PROMO IN AUTOMATICO E SE CI SONO PROBLEMI CON QUELLA DELLA PROMO INVECE
        }
    return salvata;
    }


}
