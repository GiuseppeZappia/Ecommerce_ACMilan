import 'dart:async';
import 'dart:convert';
import 'package:frontend/model/DTO/CarrelloDto.dart';
import 'package:frontend/model/managers/RestManager.dart';
import 'package:frontend/model/objects/Carrello.dart';
import 'package:frontend/model/objects/Dettagliocarrello.dart';
import 'package:frontend/model/objects/DettaglioOrdine.dart';
import 'package:frontend/model/objects/Ordine.dart';
import 'package:frontend/model/objects/Prodotto.dart';
import 'package:frontend/model/objects/Promozione.dart';
import 'package:frontend/model/objects/Utente.dart';
import 'package:frontend/model/DTO/UtenteRegistrDTO.dart';
import 'package:frontend/model/support/Constants.dart';
import 'package:frontend/model/support/LoginResult.dart';
import 'package:frontend/model/objects/AuthenticationData.dart';
import 'package:intl/intl.dart';

class Model {
  static Model sharedInstance = Model();


  RestManager _restManager = RestManager();
  AuthenticationData? _authenticationData;

  Future<LogInResult> logIn(String email, String password) async {
    try{
      Map<String, String> params = Map();
      params["grant_type"] = "password";
      params["client_id"] = Constants.CLIENT_ID;
      params["client_secret"] = Constants.CLIENT_SECRET;
      params["username"] = email;
      params["password"] = password;
      String result = await _restManager.makePostRequest(Constants.ADDRESS_AUTHENTICATION_SERVER, Constants.REQUEST_LOGIN, params, type: TypeHeader.urlencoded);
      _authenticationData = AuthenticationData.fromJson(jsonDecode(result));
      if ( _authenticationData!.hasError() ) {
        if ( _authenticationData!.error == "Invalid user credentials" ) {
          return LogInResult.error_wrong_credentials;
        }
        else if ( _authenticationData!.error == "Account is not fully set up" ) {
          return LogInResult.error_not_fully_setupped;
        }
        else {
          return LogInResult.error_unknown;
        }
      }
      _restManager.token = _authenticationData!.accessToken;
      Timer.periodic(Duration(seconds: (_authenticationData!.expiresIn! - 50)), (Timer t) {
        _refreshToken();
      });
      return LogInResult.logged;
    }
    catch (e) {
      return LogInResult.error_unknown;
    }
  }

  Future<bool> _refreshToken() async {
    try {
      Map<String, String> params = Map();
      params["grant_type"] = "refresh_token";
      params["client_id"] = Constants.CLIENT_ID;
      params["client_secret"] = Constants.CLIENT_SECRET;
      params["refresh_token"] = _authenticationData!.refreshToken!;
      String result = await _restManager.makePostRequest(Constants.ADDRESS_AUTHENTICATION_SERVER, Constants.REQUEST_LOGIN, params, type: TypeHeader.urlencoded);
      _authenticationData = AuthenticationData.fromJson(jsonDecode(result));
      if ( _authenticationData!.hasError() ) {
        return false;
      }
      _restManager.token = _authenticationData!.accessToken;
      return true;
    }
    catch (e) {
      return false;
    }
  }

  Future<bool> logOut() async {
    try{
      Map<String, String> params = Map();
      _restManager.token = null;
      params["client_id"] = Constants.CLIENT_ID;
      params["client_secret"] = Constants.CLIENT_SECRET;
      params["refresh_token"] = _authenticationData!.refreshToken!;
      await _restManager.makePostRequest(Constants.ADDRESS_AUTHENTICATION_SERVER, Constants.REQUEST_LOGOUT, params, type: TypeHeader.urlencoded);
      return true;
    }
    catch (e) {
      return false;
    }
  }





  //--------------PRODOTTI--------------
  Future<List<Prodotto>?>? trovaProdottoPerNome(String nome) async {
    try {
      if (nome.isEmpty) {
        // Se il nome è vuoto, ritorna tutti i prodotti
        return elencoProdotti(0, 20, "prezzo");
      }
      final url="${Constants.REQUEST_ELENCO_PRODOTTI_PER_NOME}/$nome";
      // String response=await _restManager.makeGetRequest(Constants.ADDRESS_STORE_SERVER,url);
      Prodotto prod=Prodotto.fromJson(json.decode(await _restManager.makeGetRequest(Constants.ADDRESS_STORE_SERVER,url)));
      // var lista=List<Prodotto>.from(jsonDecode(response).map((i) => Prodotto.fromJson(i)).toList());
      List<Prodotto> lista2=[];
      lista2.add(prod);
      return lista2;
    }
    catch (e) {
      return null; // not the best solution
    }
  }

  Future<List<Prodotto>?>? elencoProdotti(int numPagina,int dimPagina,String ordinamento) async {
    Map<String, String> params = Map();
    params["numPagina"]=numPagina.toString();
    params["dimPagina"]=dimPagina.toString();
    params["ordinamento"]=ordinamento;
    try {
      String url = Constants.REQUEST_ELENCO_PRODOTTI;

      String response = await _restManager.makeGetRequest(Constants.ADDRESS_STORE_SERVER, url, params);
      return List<Prodotto>.from(json.decode(response).map((i) => Prodotto.fromJson(i)).toList());

    } catch (e) {
      return null; // non la migliore soluzione
    }
  }

  Future<List<Prodotto>?>? elencoProdottiPerCategoria(int numPagina,int dimPagina,String ordinamento,String categoria) async {
    Map<String, String> params = Map();
    params["numPagina"]=numPagina.toString();
    params["dimPagina"]=dimPagina.toString();
    params["ordinamento"]=ordinamento;
    params["categoria"]=categoria;
    try {
      if (categoria.isEmpty) {
        // Se il categoria è vuota, ritorna tutti i prodotti
        return elencoProdotti(0, 20, "prezzo");
      }
      String url = Constants.REQUEST_ELENCO_PRODOTTI_PER_CATEGORIA;
      String response = await _restManager.makeGetRequest(Constants.ADDRESS_STORE_SERVER, url, params);
      return List<Prodotto>.from(json.decode(response).map((i) => Prodotto.fromJson(i)).toList());
    } catch (e) {
      return null; // non la migliore soluzione
    }
  }

  Future<List<Prodotto>?>? ricercaAvanzata(int numPagina,int dimPagina,String nome,String? categoria,double prezzoMin,double prezzoMax,String ordinamento,int quantita) async {
    Map<String, String> params = Map();
    params["numPagina"]=numPagina.toString();
    params["dimPagina"]=dimPagina.toString();
    params["nome"]=nome;
    params["categoria"]=categoria!;
    params["prezzoMin"]=prezzoMin.toString();
    params["prezzoMax"]=prezzoMax.toString();
    params["ordinamento"]=ordinamento;
    params["quantita"]=quantita.toString();
    try {
      String url = Constants.REQUEST_ELENCO_PRODOTTI_RICERCA_AVANZATA;
      String response = await _restManager.makeGetRequest(Constants.ADDRESS_STORE_SERVER, url, params);
      return List<Prodotto>.from(json.decode(response).map((i) => Prodotto.fromJson(i)).toList());
    } catch (e) {
      return null; // non la migliore soluzione
    }
  }

  Future<List<Prodotto>?>? ricercaPerFasciaPrezzo(int numPagina,int dimPagina,double prezzoMin,double prezzoMax) async {
    Map<String, String> params = Map();
    params["numPagina"]=numPagina.toString();
    params["dimPagina"]=dimPagina.toString();
    params["minPrezzo"]=prezzoMin.toString();
    params["maxPrezzo"]=prezzoMax.toString();
    try {
      String url = Constants.REQUEST_ELENCO_PRODOTTI_PER_FASCIA_PREZZO;
      String response = await _restManager.makeGetRequest(Constants.ADDRESS_STORE_SERVER, url, params);
      return List<Prodotto>.from(json.decode(response).map((i) => Prodotto.fromJson(i)).toList());
    } catch (e) {
      return null; // non la migliore soluzione
    }
  }


  //PROVARE SE CON PRODOTTO CHE LANCIA ECCEZIONE NEL BE COSA RESTITUISCE
  Future<bool> isProdottoCoinvolto(int idProdotto) async {
    try {
      String url= "${Constants.REQUEST_PRODOTTO_COINVOLTO_IN_PROMO}/$idProdotto";
      String response=await _restManager.makeGetRequest(Constants.ADDRESS_STORE_SERVER, url);
      return bool.parse(response);
    } catch (e) {
      throw Exception('Errore nella comunicazione con il server');
    }
  }

    //------------ORDINE---------------

  //CONTROLLA CON UNO SOLO PERCHE FORSE NON ANDAVA
  Future<List<Ordine>?>? elencoOrdiniNelPeriodo(DateTime dataInizio, DateTime dataFine, int numPagina, int dimPagina, String ordinamento) async {
    try {

      // Formatto le date come richiesto dal controller
      DateFormat dateFormat = DateFormat('dd-MM-yyyy');
      String formattedDataInizio = dateFormat.format(dataInizio);
      String formattedDataFine = dateFormat.format(dataFine);

      String url = "${Constants.REQUEST_ORDINI_NEL_PERIODO}/$formattedDataInizio/$formattedDataFine";
      print(url);
      Map<String, String> params = {
        "numPagina": numPagina.toString(),
        "dimPagina": dimPagina.toString(),
        "ordinamento": ordinamento
      };

      String response = await _restManager.makeGetRequest(Constants.ADDRESS_STORE_SERVER, url, params);
      return List<Ordine>.from(json.decode(response).map((i) => Ordine.fromJson(i)).toList());

    } catch (e) {
      return null; // non la soluzione migliore
    }
  }


  //CONTROLLA CON UNO SOLO PERCHE FORSE NON ANDAVA COME PRODOTTO
  Future<List<Ordine>?>? elencoOrdiniUtente(int numPagina, int dimPagina, String ordinamento) async {
    try {
      String url = Constants.REQUEST_ORDINI_UTENTE;

      Map<String, String> params = {
        "numPagina": numPagina.toString(),
        "dimPagina": dimPagina.toString(),
        "ordinamento": ordinamento
      };

      String response = await _restManager.makeGetRequest(Constants.ADDRESS_STORE_SERVER, url, params);
      return List<Ordine>.from(json.decode(response).map((i) => Ordine.fromJson(i)).toList());

    } catch (e) {
      return null; // non la soluzione migliore
    }
  }


  Future<bool?>? rimozioneOrdine(int idOrdine) async {
    try {

      String url = "${Constants.REQUEST_RIMUOVI_ORDINE}/$idOrdine";

      String response=await _restManager.makeDeleteRequest(Constants.ADDRESS_STORE_SERVER,url);
      if(response=="RIMOZIONE ANDATA A BUON FINE"){
        return true;
      }
      else{
        return null;
      }
    } catch (e) {
      print(e);
      return null; // non la soluzione migliore
    }
  }


  //CONTROLLA CON UNO SOLO PERCHE FORSE NON ANDAVA COME PRODOTTO
  Future<List<DettaglioOrdine>?>? dettagliOrdineUtente(int idOrdine,int numPagina, int dimPagina, String ordinamento) async {
    try {

      String url = "${Constants.REQUEST_ELENCO_DETTAGLIO_ORDINE}/$idOrdine";


      Map<String, String> params = {
        // "idOrdine":idOrdine.toString(),
        "numPagina": numPagina.toString(),
        "dimPagina": dimPagina.toString(),
        "ordinamento": ordinamento
      };

      String response = await _restManager.makeGetRequest(Constants.ADDRESS_STORE_SERVER, url, params);
      print(response);
      return List<DettaglioOrdine>.from(json.decode(response).map((i) => DettaglioOrdine.fromJson(i)).toList());

    } catch (e) {
      print(e);
      return null; // non la soluzione migliore
    }
  }


  //----------PROMOZIONI---------
  //CONTROLLA CON UNO SOLO PERCHE FORSE NON ANDAVA COME PRODOTTO
  Future<List<Promozione>?>? elencoPromozioni(int numPagina, int dimPagina, String ordinamento) async {
    try {
      String url = Constants.REQUEST_ELENCO_PROMOZIONI;

      Map<String, String> params = {
        "numPagina": numPagina.toString(),
        "dimPagina": dimPagina.toString(),
        "ordinamento": ordinamento
      };

      String response = await _restManager.makeGetRequest(Constants.ADDRESS_STORE_SERVER, url, params);
      return List<Promozione>.from(json.decode(response).map((i) => Promozione.fromJson(i)).toList());


    } catch (e) {
      return null; // non la soluzione migliore
    }
  }




  //-----------CARRELLO----------

  //CONTROLLA CON UNO SOLO PERCHE FORSE NON ANDAVA COME PRODOTTO
  Future<List<Dettagliocarrello>?>? visualizzaCarrello(int idUtente,int numPagina, int dimPagina, String ordinamento) async {
    try {
      String url = "${Constants.REQUEST_VEDI_CARRELLO}/$idUtente";
      Map<String, String> params = {
        "numPagina": numPagina.toString(),
        "dimPagina": dimPagina.toString(),
        "ordinamento": ordinamento
      };

      String response = await _restManager.makeGetRequest(Constants.ADDRESS_STORE_SERVER, url, params);

      return List<Dettagliocarrello>.from(json.decode(response).map((i) => Dettagliocarrello.fromJson(i)).toList());
    } catch (e) {
      return null; // non la soluzione migliore
    }
  }

  Future<List<Carrello>?>? aggiungiAcarrello(int idUtente,int idProdotto, int quantita) async {
    try {
      String url = "${Constants.REQUEST_AGGIUNGI_A_CARRELLO}/$idUtente/$idProdotto/$quantita";
      Map<String, String> params = Map();

      Carrello carrello=Carrello.fromJson(json.decode(await _restManager.makePostRequest(Constants.ADDRESS_STORE_SERVER,url,params)));
      List<Carrello> lista2=[];
      lista2.add(carrello);
      return lista2;
    } catch (e) {
      return null; // non la soluzione migliorei
    }
  }

  Future<List<Ordine>?>? acquista(CarrelloDto carrello,int puntiUsati) async {
    try {
      String url = "${Constants.REQUEST_ACQUISTA_CARRELLO}/$puntiUsati";
      var response=await _restManager.makePostRequest(Constants.ADDRESS_STORE_SERVER,url, carrello);
      Ordine ordine=Ordine.fromJson(json.decode(response));
      List<Ordine> lista2=[];
      lista2.add(ordine);
      return lista2;
    } catch (e) {
      print(e);
      return null;
    }
  }

  Future<Utente?>? getLoggedUser() async {
    try {
      //OTTENGO UTENTE DAL BE, CHE LO RICAVA DA TOKEN CHE POSSIEDE
      String url = Constants.REQUEST_OTTIENI_USER;
      String response = await _restManager.makeGetRequest(Constants.ADDRESS_STORE_SERVER, url);
      if(response.isEmpty){
        return null;
      }

      Utente utente=Utente.fromJson(json.decode(await _restManager.makeGetRequest(Constants.ADDRESS_STORE_SERVER,url)));

      //inutile perche restituisco a response se è vuota
      if(utente.id == null){
        return null;
      }

      return utente;
    }
    catch (e) {
      return null;// not the best solution
    }
  }

Future<Utente?>? addUser(UtenteRegistrDTO user) async {
    try {
      String rawResult = await _restManager.makePostRequest(Constants.ADDRESS_STORE_SERVER, Constants.REQUEST_REGISTRAZIONE, user);
      if ( rawResult.contains(Constants.RESPONSE_ERROR_MAIL_USER_ALREADY_EXISTS) ) {
        return null; // not the best solution
      }
      else {
        Utente u=Utente.fromJson(jsonDecode(rawResult));
        return u;
      }
    }
    catch (e) {
      return null; // not the best solution
    }
  }


}
