import 'package:flutter/material.dart';
import 'package:frontend/UI/PromotionsDetailsPage.dart';
import 'package:frontend/model/objects/Promozione.dart';
import 'package:frontend/model/Model.dart';

class PromotionsPage extends StatefulWidget {
  @override
  _PromotionsPageState createState() => _PromotionsPageState();
}

class _PromotionsPageState extends State<PromotionsPage> {
  Future<List<Promozione>?>? _futurePromotions;

  @override
  void initState() {
    super.initState();
    _futurePromotions = Model.sharedInstance.elencoPromozioni(0, 20, "nome");
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Promozioni Attive'),
      ),
      body: FutureBuilder<List<Promozione>?>(
        future: _futurePromotions,
        builder: (context, snapshot) {
          if (snapshot.connectionState == ConnectionState.waiting) {
            return Center(child: CircularProgressIndicator());
          } else if (snapshot.hasError) {
            return Center(child: Text('Errore nel caricamento delle promozioni'));
          } else if (!snapshot.hasData || snapshot.data!.isEmpty) {
            return Center(child: Text('Nessuna promozione disponibile'));
          } else {
            final promotions = snapshot.data!;
            return ListView.builder(
              itemCount: promotions.length,
              itemBuilder: (context, index) {
                final promotion = promotions[index];
                return ListTile(
                  title: Text(promotion.nome),
                  subtitle: Text(promotion.dettagli),
                  onTap: () {
                    Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder: (context) => PromotionDetailsPage(promotion: promotion),
                      ),
                    );
                  },
                );
              },
            );
          }
        },
      ),
    );
  }
}
