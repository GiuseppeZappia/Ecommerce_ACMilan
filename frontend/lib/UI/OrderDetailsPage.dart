import 'package:flutter/material.dart';
import 'package:frontend/model/Model.dart';
import 'package:frontend/model/objects/DettaglioOrdine.dart';

class OrderDetailsPage extends StatelessWidget {
  final int orderId;

  OrderDetailsPage({required this.orderId});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Dettagli Ordine #$orderId'),
        backgroundColor: Colors.blueGrey.shade300,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12.0)),
      ),
      body: FutureBuilder<List<DettaglioOrdine>?>(
        future: Model.sharedInstance.dettagliOrdineUtente(orderId, 0, 20, "prezzoUnitario"),
        builder: (context, snapshot) {
          if (snapshot.connectionState == ConnectionState.waiting) {
            return Center(child: CircularProgressIndicator());
          } else if (snapshot.hasError) {
            return Center(child: Text('Errore nel caricamento dei dettagli dell\'ordine'));
          } else if (!snapshot.hasData || snapshot.data!.isEmpty) {
            return Center(child: Text('Nessun dettaglio trovato per questo ordine'));
          } else {
            return ListView.separated(
              itemCount: snapshot.data!.length,
              separatorBuilder: (context, index) => Divider(),
              itemBuilder: (context, index) {
                DettaglioOrdine dettaglio = snapshot.data![index];

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

                final imagePath = categoryImageMap[dettaglio.prodotto.categoria] ?? 'assets/images/default.png';

                return Card(
                margin: EdgeInsets.symmetric(vertical: 8.0, horizontal: 16.0),
                  elevation: 5, child:
                  ListTile(
                  leading: Image.asset(
                    imagePath,
                    width: 50,
                    height: 50,
                    fit: BoxFit.cover,
                  ),
                  title: Text(dettaglio.prodotto.nome ?? 'Prodotto sconosciuto',style: TextStyle(fontWeight: FontWeight.bold),),
                  subtitle: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text('Quantità: ${dettaglio.quantita}',style: TextStyle(fontWeight: FontWeight.bold),),
                      Text('Prezzo: ${dettaglio.prezzoUnitario}€', style: TextStyle(color: Colors.green)),
                    ],
                  ),
                  contentPadding: EdgeInsets.symmetric(vertical: 10, horizontal: 16),
                  ),
                );
              },
            );
          }
        },
      ),
    );
  }
}
