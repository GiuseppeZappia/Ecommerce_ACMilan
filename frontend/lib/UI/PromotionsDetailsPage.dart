import 'package:flutter/material.dart';
import 'package:frontend/model/objects/Promozione.dart';

class PromotionDetailsPage extends StatelessWidget {
  final Promozione promotion;

  PromotionDetailsPage({required this.promotion});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(promotion.nome),
      ),
      body: promotion.prodottiPromozione.isEmpty
          ? Center(
        child: Text(
          'NESSUN PRODOTTO PRESENTE PER LA PROMOZIONE AL MOMENTO',
          style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
          textAlign: TextAlign.center,
        ),
      )
          : ListView.builder(
        itemCount: promotion.prodottiPromozione.length,
        itemBuilder: (context, index) {
          final productPromo = promotion.prodottiPromozione[index];
          final product = productPromo.prodotto;
          return ListTile(
            title: Text(product.nome),
            subtitle: Text(
              '${product.descrizione}\nPrezzo: ${product.prezzo} €\nSconto: ${productPromo.sconto}%',
            ),
            isThreeLine: true,
          );
        },
      ),
    );
  }
}
