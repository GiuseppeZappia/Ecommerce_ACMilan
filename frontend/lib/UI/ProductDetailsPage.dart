// import 'package:flutter/material.dart';
// import 'package:frontend/model/Model.dart';
// import 'package:frontend/model/objects/Prodotto.dart';
//
// class ProductDetailsPage extends StatelessWidget {
//   final Prodotto product;
//   Map<String, String> categoryImageMap = {
//     'Tazze': 'assets/images/tazza2.webp',
//     'Portachiavi': 'assets/images/portachiavi.jpg',
//   };
//
//   ProductDetailsPage({required this.product});
//
//   Future<void> _addToCart(BuildContext context, int productId) async {
//     try {
//       await Model.sharedInstance.aggiungiAcarrello(1, productId, 1);
//       ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('Prodotto aggiunto al carrello')));
//     } catch (e) {
//       if (e.toString() == "Utente non autenticato") {
//         showDialog(
//           context: context,
//           builder: (context) => AlertDialog(
//             title: Text("Autenticazione richiesta"),
//             content: Text("Effettua il login o registrati per aggiungere al carrello."),
//             actions: [
//               TextButton(
//                 onPressed: () {
//                   Navigator.of(context).pop();
//                 },
//                 child: Text("OK"),
//               ),
//             ],
//           ),
//         );
//       } else {
//         ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('Errore nell\'aggiunta al carrello')));
//       }
//     }
//   }
//
//   @override
//   Widget build(BuildContext context) {
//     return Scaffold(
//       appBar: AppBar(
//         title: Text(product.nome),
//       ),
//       body: Padding(
//         padding: const EdgeInsets.all(16.0),
//         child: Column(
//           crossAxisAlignment: CrossAxisAlignment.start,
//           children: <Widget>[
//             Center(
//               child: Image.asset(
//                 categoryImageMap[product.categoria] ?? 'assets/images/default.jpg',
//                 height: 300.0,
//                 width: 300.0,
//                 fit: BoxFit.scaleDown,
//               ),
//             ),
//             SizedBox(height: 16.0),
//             Text(
//               product.nome,
//               style: TextStyle(fontSize: 24.0, fontWeight: FontWeight.bold),
//             ),
//             SizedBox(height: 8.0),
//             Text(
//               '${product.prezzo} \$',
//               style: TextStyle(fontSize: 20.0, color: Colors.red),
//             ),
//             SizedBox(height: 16.0),
//             Text(
//               product.descrizione,
//               style: TextStyle(fontSize: 16.0),
//             ),
//             Spacer(),
//             ElevatedButton(
//               onPressed: () => _addToCart(context, product.id),
//               child: Text('Aggiungi al Carrello'),
//             ),
//           ],
//         ),
//       ),
//     );
//   }
// }
import 'package:flutter/material.dart';
import 'package:frontend/model/Model.dart';
import 'package:frontend/model/objects/Prodotto.dart';

import '../model/objects/Utente.dart';

class ProductDetailsPage extends StatefulWidget {
  final Prodotto product;
  Map<String, String> categoryImageMap = {
    'Tazze': 'assets/images/tazza2.webp',
    'Portachiavi': 'assets/images/portachiavi.jpg',
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
                },
                child: Text("OK"),
              ),
            ],
          ),
        );
      } else {
        await Model.sharedInstance.aggiungiAcarrello(loggedUser.id, productId, quantity);
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
      ),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            Center(
              child: Image.asset(
                widget.categoryImageMap[widget.product.categoria] ?? 'assets/images/default.jpg',
                height: 300.0,
                width: 300.0,
                fit: BoxFit.scaleDown,
              ),
            ),
            SizedBox(height: 16.0),
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
            Spacer(),
            Row(
              children: [
                IconButton(
                  icon: Icon(Icons.remove),
                  onPressed: () {
                    setState(() {
                      if (quantity > 1) quantity--;
                    });
                  },
                ),
                Text(quantity.toString(), style: TextStyle(fontSize: 16.0)),
                IconButton(
                  icon: Icon(Icons.add),
                  onPressed: () {
                    setState(() {
                      quantity++;
                    });
                  },
                ),
              ],
            ),
            ElevatedButton(
              onPressed: () => _addToCart(widget.product.id),
              child: Text('Aggiungi al Carrello'),
            ),
          ],
        ),
      ),
    );
  }
}
