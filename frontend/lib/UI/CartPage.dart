import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:frontend/UI/HomePage.dart';
import 'package:frontend/UI/SummaryPage.dart';
import 'package:frontend/model/Model.dart';
import 'package:frontend/model/objects/Dettagliocarrello.dart';
import '../model/objects/Utente.dart';

class CartPage extends StatefulWidget {
  @override
  _CartPageState createState() => _CartPageState();
}

class _CartPageState extends State<CartPage> {
  List<Dettagliocarrello> _cartItems = [];
  double _totalPrice = 0.0;
  Map<String, String> categoryImageMap = {
    'Tazze': 'assets/images/tazza2.webp',
    'Portachiavi': 'assets/images/portachiavi.jpg',
    'Palloni': 'assets/images/pallone.webp',
    'Bracciali': 'assets/images/bracciale.webp',
    'Sciarpe': 'assets/images/sciarpa.webp',
    'Berretti': 'assets/images/berretto.webp',
    'Cappelli': 'assets/images/cappello.webp',
    'Gemelli': 'assets/images/gemelli.webp'
  };

  @override
  void initState() {
    super.initState();
    _loadCart();
  }

  Future<void> _loadCart() async {
    Utente? loggedUser = await Model.sharedInstance.getLoggedUser();
    if (loggedUser == null) {
      showDialog(
        context: context,
        builder: (context) => AlertDialog(
          title: Text("Autenticazione richiesta"),
          content: Text("Effettua il login o registrati per accedere al carrello."),
          actions: [
            TextButton(
              onPressed: () {
                Navigator.of(context).pop();
                Navigator.push(context,MaterialPageRoute(builder: (context)=>HomePage(selectedIndex:4),),);
              },
              child: Text("OK"),
            ),
          ],
        ),
      );
    } else {
      int idUtente = loggedUser.id;
      var cartItems = await Model.sharedInstance.visualizzaCarrello(idUtente, 0, 20, "quantita");
      setState(() {
        _cartItems = cartItems ?? [];
        _totalPrice = _cartItems.fold(0, (sum, item) => sum + item.prodotto.prezzo * item.quantita);
      });
    }
  }

  Future<void> _updateQuantity(int productId, int quantityChange) async {
    Utente? loggedUser = await Model.sharedInstance.getLoggedUser();
    if (loggedUser != null) {
      int idUtente = loggedUser.id;
      await Model.sharedInstance.aggiungiAcarrello(idUtente, productId, quantityChange);
      await _loadCart();
    }
  }

  Future<void> _acquista() async {
    if(_cartItems.isEmpty){
      showDialog(
        context: context,
        builder: (context) => AlertDialog(
          title: Text("Errore acquisto"),
          content: Text("Inserisci qualche prodotto nel carrello per procedere con l'ordine."),
          actions: [
            TextButton(
              onPressed: () {
                Navigator.of(context).pop();
              },
              child: Text("OK"),
            ),
          ],
        ),
      );
    }
    else{
    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (context) => SummaryPage(cartItems: _cartItems, totalPrice: _totalPrice),
      ),
    );
  }}

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Carrello'),
        backgroundColor: Colors.blueGrey.shade300,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12.0)),
        actions: <Widget>[
          Center(
            child: Text(
              'Totale: €$_totalPrice',
              style: TextStyle(fontSize: 18, color: Colors.black,fontWeight: FontWeight.bold),
            ),
          ),
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16.0),
            child: ElevatedButton(
              onPressed: _acquista,
              style: ElevatedButton.styleFrom(
                backgroundColor: Colors.blueGrey.shade300,
                padding: EdgeInsets.symmetric(horizontal: 20, vertical: 12),
                elevation: 0,
              ),
              child: Text(
                'Acquista',
                style: TextStyle(fontSize: 18,color: Colors.black),
              ),
            ),
          ),
        ],
      ),
      body: Stack(
        children: <Widget>[
          _cartItems.isEmpty
              ? Center(child: Text('Il carrello è vuoto', style: TextStyle(fontSize: 18, color: Colors.grey[600])))
              : Column(
            children: <Widget>[
              Expanded(
                child: ListView.builder(
                  itemCount: _cartItems.length,
                  itemBuilder: (context, index) {
                    final item = _cartItems[index];
                    final imagePath = categoryImageMap[item.prodotto.categoria] ?? 'assets/images/default.png';

                    return Card(
                      margin: EdgeInsets.symmetric(vertical: 8, horizontal: 16),
                      elevation: 5,
                      child: ListTile(
                        leading: Image.asset(
                          imagePath,
                          width: 60,
                          height: 60,
                          fit: BoxFit.cover,
                        ),
                        title: Text(
                          item.prodotto.nome,
                          style: TextStyle(
                            fontSize: 16,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                        subtitle: Row(
                          children: [
                            IconButton(
                              icon: Icon(Icons.remove, color: Colors.red),
                              onPressed: () {
                                  _updateQuantity(_cartItems[index].prodotto.id,_cartItems[index].quantita-1);
                              },
                            ),
                            Text('${item.quantita}'),
                            IconButton(
                              icon: Icon(Icons.add, color: Colors.green),
                              onPressed: () {
                                _updateQuantity(_cartItems[index].prodotto.id,_cartItems[index].quantita+1);
                              },
                            ),
                          ],
                        ),
                        trailing: Text(
                          '€${item.prodotto.prezzo * item.quantita}',
                          style: TextStyle(
                            fontSize: 16,
                            color: Colors.blueGrey,
                          ),
                        ),
                      ),
                    );
                  },
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }
}
