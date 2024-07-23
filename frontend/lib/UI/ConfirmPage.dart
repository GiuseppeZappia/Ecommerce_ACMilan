import 'package:flutter/material.dart';
import 'package:frontend/UI/CartPage.dart';
import 'package:frontend/UI/HomePage.dart';
import 'package:frontend/model/DTO/CarrelloDto.dart';
import 'package:frontend/model/DTO/DettaglioDto.dart';
import 'package:frontend/model/Model.dart';
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
  Utente? utenteOrdine;

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
      appBar: AppBar(
        title: Text('Informazioni di Spedizione'),
        backgroundColor: Colors.blueGrey.shade300,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(15.0)),
      ),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Form(
          key: _formKey,
          child: ListView(
            children: [
              Text(
                'Inserisci le informazioni di spedizione:',
                style: Theme.of(context).textTheme.headlineMedium?.copyWith(
                  fontWeight: FontWeight.bold,
                ),
              ),
              SizedBox(height: 16.0),
              _buildTextFormField('Nome', (value) {
                nome = value;
              }),
              SizedBox(height: 16.0),
              _buildTextFormField('Cognome', (value) {
                cognome = value;
              }),
              SizedBox(height: 16.0),
              _buildTextFormField('Indirizzo', (value) {
                indirizzo = value;
              }),
              SizedBox(height: 16.0),
              _buildTextFormField('Città', (value) {
                citta = value;
              }),
              SizedBox(height: 16.0),
              _buildTextFormField('CAP', (value) {
                cap = value;
              }),
              if (!prodottiInPromozione) ...[
                SizedBox(height: 16.0),
                _buildPointsField(),
                SizedBox(height: 16.0),
                Text(
                  'Puoi usare i punti fedeltà dal momento in cui non ci sono prodotti in promozione nel tuo ordine.',
                  style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                    color: Colors.green,
                  ),
                ),
              ] else ...[
                SizedBox(height: 16.0),
                Text(
                  'Non puoi usare punti fedeltà perché ci sono prodotti in promozione.',
                  style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                    color: Colors.red,
                  ),
                ),
              ],
              SizedBox(height: 20.0),
              ElevatedButton(
                onPressed: () {
                  if (_formKey.currentState!.validate()) {
                    _formKey.currentState!.save();
                    _processOrder();
                  }
                },
                child: Text('Procedi con l\'ordine',style: TextStyle(color: Colors.black),),
                style: ElevatedButton.styleFrom(
                  backgroundColor: Colors.blueGrey,
                  padding: EdgeInsets.symmetric(vertical: 15.0),
                  textStyle: TextStyle(fontSize: 18),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  TextFormField _buildTextFormField(String label, FormFieldSetter<String?> onSaved) {
    return TextFormField(
      decoration: InputDecoration(
        labelText: label,
        border: OutlineInputBorder(),
        contentPadding: EdgeInsets.symmetric(horizontal: 12.0, vertical: 8.0),
      ),
      validator: (value) {
        if (value == null || value.isEmpty) {
          return 'Per favore inserisci $label';
        }
        return null;
      },
      onSaved: onSaved,
    );
  }

  TextFormField _buildPointsField() {
    return TextFormField(
      decoration: InputDecoration(
        labelText: 'Punti Fedeltà (0 per non usare)',
        border: OutlineInputBorder(),
        contentPadding: EdgeInsets.symmetric(horizontal: 12.0, vertical: 8.0),
      ),
      initialValue: '0',
      keyboardType: TextInputType.number,
      validator: (value) {
        if (value == null || int.tryParse(value) == null) {
          return 'Per favore inserisci un numero valido';
        }
        int punti = int.parse(value);
        if(punti.sign.isNegative){
          return 'Per favore inserisci un numero valido';
        }
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
                'Si è verificato un errore durante l\'acquisto. Per favore ricontrolla i prodotti nel carrello e riprova.'
            ),
            actions: <Widget>[
              TextButton(
                child: Text('OK'),
                onPressed: () {
                  Navigator.of(context).pop();
                  Navigator.push(
                    context,
                    MaterialPageRoute(builder: (context) => HomePage(selectedIndex: 1,)),
                  );
                },
              ),
            ],
          );
        },
      );
    } else {
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
