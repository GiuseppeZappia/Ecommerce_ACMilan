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

    return Scaffold(
      appBar: AppBar(title: Text('Riepilogo Carrello'),
        backgroundColor: Colors.blueGrey.shade300,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12.0)),),
      body: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Expanded(
            child: ListView.builder(
              itemCount: cartItems.length,
              itemBuilder: (context, index) {
                final item = cartItems[index];
                final imagePath = categoryImageMap[item.prodotto.categoria] ?? 'assets/images/default.png';

                return Card(
                  margin: EdgeInsets.symmetric(vertical: 8, horizontal: 16),
                  elevation: 5,
                  child: ListTile(
                    leading: Image.asset(imagePath, width: 50, height: 50, fit: BoxFit.cover),
                    title: Text(item.prodotto.nome, style: TextStyle(fontWeight: FontWeight.bold)),
                    subtitle: Text('Quantità: ${item.quantita}'),
                    trailing: Text('€${item.prodotto.prezzo * item.quantita}', style: TextStyle(fontSize: 16, color: Colors.green)),
                  ),
                );
              },
            ),
          ),
          Padding(
            padding: const EdgeInsets.all(16.0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text('Totale: €$totalPrice', style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold, color: Colors.black)),
                SizedBox(height: 16),
                SizedBox(
                  width: 150,child:
                ElevatedButton(
                  onPressed: () {
                    Navigator.push(
                      context,
                      MaterialPageRoute(builder: (context) => ConfirmPage(cartItems: cartItems, totalPrice: totalPrice)),
                    );
                  },
                  style: ElevatedButton.styleFrom(
                    backgroundColor: Colors.blueGrey,
                    padding: EdgeInsets.symmetric(vertical: 16),
                  ),
                  child: Text('Conferma', style: TextStyle(fontSize: 15,color: Colors.black)),
                ),),
              ],
            ),
                ),
        ],
      ),
    );
  }
}
