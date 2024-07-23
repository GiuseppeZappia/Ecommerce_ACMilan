import 'package:flutter/material.dart';
import 'package:frontend/model/objects/Promozione.dart';

class PromotionDetailsPage extends StatelessWidget {
  final Promozione promotion;

  PromotionDetailsPage({required this.promotion});

  // Mappa delle immagini per categoria
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
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(promotion.nome),
        backgroundColor: Colors.blueGrey.shade300,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12.0)),
      ),
      body: promotion.prodottiPromozione.isEmpty
          ? Center(
        child: Padding(
          padding: const EdgeInsets.all(16.0),
          child: Text(
            'NESSUN PRODOTTO PRESENTE PER LA PROMOZIONE AL MOMENTO',
            style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold, color: Colors.black54),
            textAlign: TextAlign.center,
          ),
        ),
      )
          : ListView.builder(
        itemCount: promotion.prodottiPromozione.length,
        itemBuilder: (context, index) {
          final productPromo = promotion.prodottiPromozione[index];
          final product = productPromo.prodotto;

          final imagePath = categoryImageMap[product.categoria] ?? 'assets/images/default.png';

          return Card(
            margin: EdgeInsets.symmetric(vertical: 8.0, horizontal: 16.0),
            elevation: 5,
            child: ListTile(
              leading: Image.asset(
                imagePath,
                width: 60,
                height: 60,
                fit: BoxFit.cover,
                errorBuilder: (context, error, stackTrace) {
                  return Icon(Icons.image_not_supported, size: 60);
                },
              ),
              title: Text(
                product.nome,
                style: TextStyle(fontWeight: FontWeight.bold),
              ),
              subtitle: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(product.descrizione),
                  SizedBox(height: 8),
                  Text(
                    'Prezzo: ${product.prezzo.toStringAsFixed(2)} €',
                    style: TextStyle(color: Colors.black, fontWeight: FontWeight.bold),
                  ),
                  Text(
                    'Sconto: ${productPromo.sconto}%',
                    style: TextStyle(color: Colors.green, fontWeight: FontWeight.bold),
                  ),
                ],
              ),
              contentPadding: EdgeInsets.all(16.0),
            ),
          );
        },
      ),
    );
  }
}
