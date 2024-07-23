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
        backgroundColor: Colors.blueGrey.shade300,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12.0)),
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
              padding: EdgeInsets.all(8.0),
              itemCount: promotions.length,
              itemBuilder: (context, index) {
                final promotion = promotions[index];
                return Card(
                  elevation: 5.0,
                  margin: EdgeInsets.symmetric(vertical: 8.0),
                  child: ListTile(
                    contentPadding: EdgeInsets.all(16.0),
                    leading: Icon(Icons.local_offer, size: 40.0, color: Colors.blueGrey),
                    title: Text(
                      promotion.nome,
                      style: TextStyle(fontWeight: FontWeight.bold, fontSize: 18.0),
                    ),
                    subtitle: Text(
                      promotion.dettagli,
                      style: TextStyle(color: Colors.grey[600]),
                    ),
                    onTap: () {
                      Navigator.push(
                        context,
                        MaterialPageRoute(
                          builder: (context) => PromotionDetailsPage(promotion: promotion),
                        ),
                      );
                    },
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
