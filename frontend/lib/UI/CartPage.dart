import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
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
    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (context) => SummaryPage(cartItems: _cartItems, totalPrice: _totalPrice),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Carrello'),
      ),
      body: _cartItems.isEmpty
          ? Center(child: Text('Il carrello è vuoto'))
          : Column(
        children: <Widget>[
          Expanded(
            child: ListView.builder(
              itemCount: _cartItems.length,
              itemBuilder: (context, index) {
                return ListTile(
                  title: Text(_cartItems[index].prodotto.nome,
                    style: TextStyle(
                      fontSize: 15,
                      color: Colors.red,
                      fontStyle: FontStyle.normal,
                    ),
                  ),
                  subtitle: Row(
                    children: [
                      IconButton(
                        icon: Icon(Icons.remove),
                        onPressed: () {
                          _updateQuantity(_cartItems[index].prodotto.id,_cartItems[index].quantita-1);
                        },
                      ),
                      Text('${_cartItems[index].quantita}'),
                      IconButton(
                        icon: Icon(Icons.add),
                        onPressed: () {
                          _updateQuantity(_cartItems[index].prodotto.id,_cartItems[index].quantita+1);
                        },
                      ),
                    ],
                  ),
                  trailing: Text(
                      '€${_cartItems[index].prodotto.prezzo * _cartItems[index].quantita}',
                      style:TextStyle(fontSize: 15,
                      color: Colors.lightGreen,
                        fontStyle: FontStyle.italic,
                      ),
                  ),

                );
              },
            ),
          ),
          Padding(
            padding: const EdgeInsets.all(8.0),
            child: Column(
              children: <Widget>[
                Text(
                  'Totale: €$_totalPrice',
                  style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
                ),
                SizedBox(height: 10),
                ElevatedButton(
                  onPressed: _acquista,
                  child: Text('Acquista'),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
