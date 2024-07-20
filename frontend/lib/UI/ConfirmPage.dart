import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:frontend/UI/HomePage.dart';
import 'package:frontend/UI/ProductsPage.dart';
import 'package:frontend/model/DTO/CarrelloDto.dart';
import 'package:frontend/model/DTO/DettaglioDto.dart';
import 'package:frontend/model/Model.dart';
import 'package:frontend/model/objects/Carrello.dart';
import 'package:frontend/model/objects/Dettagliocarrello.dart';

import '../model/objects/Utente.dart';

class ConfirmPage extends StatefulWidget {
  final List<Dettagliocarrello> cartItems;
  final double totalPrice;

  ConfirmPage({required this.cartItems, required this.totalPrice});

  @override
  _ConfirmPageState createState() => _ConfirmPageState();
}

class _ConfirmPageState extends State<ConfirmPage> {
  final _formKey = GlobalKey<FormState>();
  String? nome;
  String? cognome;
  String? indirizzo;
  String? citta;
  String? cap;
  int puntiFedelta = 0;
  bool prodottiInPromozione = false;
  Utente ? utenteOrdine;

  @override
  void initState() {
    super.initState();
    _checkPromozione();
  }

  void _checkPromozione() async {
    utenteOrdine = await Model.sharedInstance.getLoggedUser();
    for (var item in widget.cartItems) {
      if (await Model.sharedInstance.isProdottoCoinvolto(item.prodotto.id)) {
        setState(() {
          prodottiInPromozione = true;
        });
        break;
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text('Informazioni di Spedizione')),
      body: Padding(
        padding: const EdgeInsets.all(8.0),
        child: Form(
          key: _formKey,
          child: Column(
            children: [
              TextFormField(
                decoration: InputDecoration(labelText: 'Nome'),
                validator: (value) {
                  if (value == null || value.isEmpty) {
                    return 'Per favore inserisci il nome';
                  }
                  return null;
                },
                onSaved: (value) {
                  nome = value;
                },
              ),
              TextFormField(
                decoration: InputDecoration(labelText: 'Cognome'),
                validator: (value) {
                  if (value == null || value.isEmpty) {
                    return 'Per favore inserisci il cognome';
                  }
                  return null;
                },
                onSaved: (value) {
                  cognome = value;
                },
              ),
              TextFormField(
                decoration: InputDecoration(labelText: 'Indirizzo'),
                validator: (value) {
                  if (value == null || value.isEmpty) {
                    return 'Per favore inserisci l\'indirizzo';
                  }
                  return null;
                },
                onSaved: (value) {
                  indirizzo = value;
                },
              ),
              TextFormField(
                decoration: InputDecoration(labelText: 'Città'),
                validator: (value) {
                  if (value == null || value.isEmpty) {
                    return 'Per favore inserisci la città';
                  }
                  return null;
                },
                onSaved: (value) {
                  citta = value;
                },
              ),
              TextFormField(
                decoration: InputDecoration(labelText: 'CAP'),
                validator: (value) {
                  if (value == null || value.isEmpty) {
                    return 'Per favore inserisci il CAP';
                  }
                  return null;
                },
                onSaved: (value) {
                  cap = value;
                },
              ),
              if (!prodottiInPromozione) ...[
                TextFormField(
                  decoration: InputDecoration(
                      labelText: 'Punti Fedeltà (0 per non usare)'),
                  initialValue: '0',
                  keyboardType: TextInputType.number,
                  validator: (value) {
                    if (value == null || int.tryParse(value) == null) {
                      return 'Per favore inserisci un numero valido';
                    }
                    int punti = int.parse(value);
                    if (punti < 0) {
                      return 'Per favore inserisci un numero valido';
                    }
                    if (punti > utenteOrdine!.puntifedelta ||
                        (punti < 100 && punti != 0)) {
                      return 'Devi usare almeno 100 punti e non più di quelli a tua disposizione, hai a disposizione ' +
                          utenteOrdine!.puntifedelta.toString() + " punti";
                    }
                    return null;
                  },
                  onSaved: (value) {
                    puntiFedelta = int.parse(value!);
                  },
                ),
                Text(
                    'Puoi usare i punti fedeltà dal momento in cui non ci sono prodotti in promozione nel tuo ordine.')
              ] else
                ...[
                  Text(
                      'Non puoi usare punti fedeltà perché ci sono prodotti in promozione.'),
                ],
              SizedBox(height: 10),
              ElevatedButton(
                onPressed: () {
                  if (_formKey.currentState!.validate()) {
                    _formKey.currentState!.save();
                    _processOrder();
                  }
                },
                child: Text('Procedi con l\'ordine'),
              ),
            ],
          ),
        ),
      ),
    );
  }

  void _processOrder() async {
    List<DettaglioDto> dettagliList = widget.cartItems.map((item) {
      return DettaglioDto(
        idProdotto: item.prodotto.id,
        quantita: item.quantita,
        prezzoUnitario: item.prodotto.prezzo,
      );
    }).toList();

    CarrelloDto carrelloDto = CarrelloDto(
      listaDettagli: dettagliList,
      idUtente: utenteOrdine!.id,
    );
    var risposta = await Model.sharedInstance.acquista(
        carrelloDto, puntiFedelta);
    if (risposta == null) {
      showDialog(
        context: context,
        builder: (BuildContext context) {
          return AlertDialog(
            title: Text('Errore'),
            content: Text(
                'Si è verificato un errore durante l\'acquisto. Per favore riprova.'),
            actions: <Widget>[
              TextButton(
                child: Text('OK'),
                onPressed: () {
                  Navigator.of(context).pop();
                },
              ),
            ],
          );
        },
      );
    }
    else {
      showDialog(
        context: context,
        builder: (BuildContext context) {
          return AlertDialog(
            title: Text('Grazie'),
            content: Text('Il tuo ordine è stato completato con successo!'),
            actions: <Widget>[
              TextButton(
                child: Text('OK'),
                onPressed: () {
                  Navigator.of(context).pop();
                  Navigator.pushAndRemoveUntil(
                    context,
                    MaterialPageRoute(builder: (context) => HomePage()),
                        (Route<dynamic> route) => false,
                  );
                },
              ),
            ],
          );
        },
      );
    }
  }
}
