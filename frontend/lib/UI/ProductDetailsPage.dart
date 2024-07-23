import 'package:flutter/material.dart';
import 'package:frontend/model/Model.dart';
import 'package:frontend/model/objects/Prodotto.dart';

import '../model/objects/Utente.dart';
import 'HomePage.dart';

class ProductDetailsPage extends StatefulWidget {
  final Prodotto product;
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
  ProductDetailsPage({required this.product});

  @override
  _ProductDetailsPageState createState() => _ProductDetailsPageState();
}

class _ProductDetailsPageState extends State<ProductDetailsPage> {
  int quantity = 1;

  Future<void> _addToCart(int productId) async {
    try {
      Utente? loggedUser = await Model.sharedInstance.getLoggedUser();
      if (loggedUser == null){
        showDialog(
          context: context,
          builder: (context) => AlertDialog(
            title: Text("Autenticazione richiesta"),
            content: Text("Effettua il login o registrati per aggiungere al carrello."),
            actions: [
              TextButton(
                onPressed: () {
                  Navigator.of(context).pop();
                  Navigator.push(
                    context,
                    MaterialPageRoute(builder: (context) => HomePage(selectedIndex: 4,)),
                  );
                },
                child: Text("OK"),
              ),
            ],
          ),
        );
      } else {
        var risposta=await Model.sharedInstance.aggiungiAcarrello(loggedUser.id, productId, quantity);
        if(risposta==null){
          showDialog(
            context: context,
            builder: (context) => AlertDialog(
              title: Text("Quantità non disponibile"),
              content: Text("Quantità non disponibile attualmente in magazzino."),
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
      }
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('Errore nell\'aggiunta al carrello')));
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.product.nome),
        leading: IconButton(
          icon: Icon(Icons.arrow_back),
          onPressed: () {
            Navigator.of(context).pop();
          },
        ),
      ),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            Center(
              child: Image.asset(
                widget.categoryImageMap[widget.product.categoria] ?? 'assets/images/default.png',
                height: 300.0,
                width: 300.0,
                fit: BoxFit.cover,
              ),
            ),
            SizedBox(height: 16.0),
            Card(
              elevation: 4.0,
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(8.0),
              ),
              child: Padding(
                padding: const EdgeInsets.all(16.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      widget.product.nome,
                      style: TextStyle(fontSize: 24.0, fontWeight: FontWeight.bold),
                    ),
                    SizedBox(height: 8.0),
                    Text(
                      '€${widget.product.prezzo}',
                      style: TextStyle(fontSize: 20.0, color: Colors.red),
                    ),
                    SizedBox(height: 16.0),
                    Text(
                      widget.product.descrizione,
                      style: TextStyle(fontSize: 16.0),
                    ),
                  ],
                ),
              ),
            ),
            Spacer(),
            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                IconButton(
                  icon: Icon(Icons.remove,color:Colors.red),
                  onPressed: () {
                    setState(() {
                      if (quantity > 1) quantity--;
                    });
                  },
                ),
                Text(quantity.toString(), style: TextStyle(fontSize: 16.0)),
                IconButton(
                  icon: Icon(Icons.add,color:Colors.green),
                  onPressed: () {
                    setState(() {
                      quantity++;
                    });
                  },
                ),
              ],
            ),
            SizedBox(height: 16.0),
            Center(
              child: ElevatedButton(
                onPressed: () => _addToCart(widget.product.id),
                child: Text('Aggiungi al Carrello',style: TextStyle(color: Colors.black),),
                style: ElevatedButton.styleFrom(
                  padding: EdgeInsets.symmetric(horizontal: 32.0, vertical: 16.0),
                  textStyle: TextStyle(fontSize: 16.0),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}