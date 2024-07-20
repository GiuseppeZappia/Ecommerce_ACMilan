import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
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
            return ListView.builder(
              itemCount: snapshot.data!.length,
              itemBuilder: (context, index) {
                DettaglioOrdine dettaglio = snapshot.data![index];
                return ListTile(
                  title: Text(dettaglio.prodotto.nome ?? 'Prodotto sconosciuto'),
                  subtitle: Text('Quantità: ${dettaglio.quantita}\nPrezzo: ${dettaglio.prezzoUnitario}€'),
                );
              },
            );
          }
        },
      ),
    );
  }
}
