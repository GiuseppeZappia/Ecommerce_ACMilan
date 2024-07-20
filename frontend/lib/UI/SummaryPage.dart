import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:frontend/UI/ConfirmPage.dart';
import 'package:frontend/model/objects/Dettagliocarrello.dart';

class SummaryPage extends StatelessWidget {
  final List<Dettagliocarrello> cartItems;
  final double totalPrice;

  SummaryPage({required this.cartItems, required this.totalPrice});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text('Riepilogo Carrello')),
      body: Column(
        children: [
          Expanded(
            child: ListView.builder(
              itemCount: cartItems.length,
              itemBuilder: (context, index) {
                return ListTile(
                  title: Text(cartItems[index].prodotto.nome),
                  subtitle: Text('Quantità: ${cartItems[index].quantita}'),
                  trailing: Text('€${cartItems[index].prodotto.prezzo * cartItems[index].quantita}'),
                );
              },
            ),
          ),
          Padding(
            padding: const EdgeInsets.all(8.0),
            child: Column(
              children: [
                Text('Totale: €$totalPrice', style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold)),
                SizedBox(height: 10),
                ElevatedButton(
                  onPressed: () {
                    Navigator.push(
                      context,
                      MaterialPageRoute(builder: (context) => ConfirmPage(cartItems: cartItems, totalPrice: totalPrice)),
                    );
                  },
                  child: Text('Conferma'),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
