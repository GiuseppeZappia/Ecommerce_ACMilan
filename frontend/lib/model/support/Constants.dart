class Constants {
  // app info
  static final String APP_VERSION = "0.0.1";
  static final String APP_NAME = "A.C. Milan Store";

  // addresses
  static final String ADDRESS_STORE_SERVER = "localhost:9090";
  static final String ADDRESS_AUTHENTICATION_SERVER = "localhost:8080";

  // authentication
  static final String REALM = "Progetto_E-commerce";
  static final String CLIENT_ID = "admin-cli";
  static final String CLIENT_SECRET = "EijBYxp6Y9eUZ2zBCzbbHQQcy2eKn5hZ";
  static final String REQUEST_LOGIN = "/realms/" + REALM + "/protocol/openid-connect/token";
  static final String REQUEST_LOGOUT = "/realms/" + REALM + "/protocol/openid-connect/logout";

  // requests

  //CARRELLO
  static final String REQUEST_VEDI_CARRELLO="/carrello";//aggiungere {idUtente}??
  static final String REQUEST_AGGIUNGI_A_CARRELLO="/carrello/aggiungi";//aggiungere {idCarrello}/{idUtente}/{quantita}??
  static final String REQUEST_ACQUISTA_CARRELLO = "/carrello/acquista";//aggiungere {idUtente}/{puntiUsati}??

  //ORDINE
  static final String REQUEST_ELENCO_ORDINI = "/ordini/elencoOrdini";
  static final String REQUEST_INSERIMENTO_ORDINE = "/ordini/inserimento";
  static final String REQUEST_ORDINI_NEL_PERIODO = "/ordini/elencoOrdini";//aggiungere {utente}/{DataInizio}/{DataFine}??
  static final String REQUEST_ORDINI_UTENTE = "/ordini/elencoOrdini/perUtente";
  static final String REQUEST_RIMUOVI_ORDINE = "/ordini";//aggiungere {idOrdine}??
  static final String REQUEST_ELENCO_DETTAGLIO_ORDINE = "/ordini/dettagliOrdine";//aggiungere {idOrdine}??

  //PRODOTTO
  static final String REQUEST_ELENCO_PRODOTTI = "/prodotti/elencoDisponibili";
  static final String REQUEST_ELENCO_PRODOTTI_PER_CATEGORIA = "/prodotti/percategoria";
  static final String REQUEST_ELENCO_PRODOTTI_PER_FASCIA_PREZZO = "/prodotti/perFasciaPrezzo";
  static final String REQUEST_ELENCO_PRODOTTI_PER_NOME = "/prodotti/perNome";//aggiungere {nomeProdotto}??
  static final String REQUEST_ELENCO_PRODOTTI_RICERCA_AVANZATA = "/prodotti/ricercaAvanzata";
  static final String REQUEST_SALVA_PRODOTTO = "/prodotti";
  static final String REQUEST_ELIMINA_PRODOTTO = "/prodotti";//aggiungere {idProdotto}??
  static final String REQUEST_GET_PREFERITI = "/preferiti";
  static final String REQUEST_AGGIUNGI_A_PREFERITI = "/preferiti";//aggiungere {idProdotto}??

  //PROMOZIONE
  static final String REQUEST_ELENCO_PROMOZIONI = "/promozioni/elenco";
  static final String REQUEST_AGGIUNTA_PROD_A_PROMOZIONE = "/promozioni/aggiuntaApromo/";//aggiungere {idPromozione}/{idProdotto}/{percentualeSconto}??
  static final String REQUEST_CREA_NUOVA_PROMO = "/promozioni/creaNuova";
  static final String REQUEST_PRODOTTO_COINVOLTO_IN_PROMO="/promozioni/coinvolto";

  //REGISTRAZIONE/LOGIN
  static final String REQUEST_REGISTRAZIONE= "/utenti/registrazione";
  static final String REQUEST_LOGIN_BE="/utenti/login";
  static final String REQUEST_LOGOUT_BE="/utenti/logout/";//aggiungere {refreshToken}??
  static final String REQUEST_OTTIENI_USER="/utenti/trovaUtente";

  // states
  static final String STATE_CLUB = "club";

  // responses
  static final String RESPONSE_ERROR_MAIL_USER_ALREADY_EXISTS = "PROBLEMA NELLA REGISTRAZIONE DELL'UTENTE";

  // messages
  static final String MESSAGE_CONNECTION_ERROR = "connection_error";

  // links
  static final String LINK_RESET_PASSWORD = "***";
  static final String LINK_FIRST_SETUP_PASSWORD = "***";


}