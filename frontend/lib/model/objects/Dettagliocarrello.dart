
import 'package:frontend/model/objects/Prodotto.dart';
class Dettagliocarrello {
  int id;
  Prodotto prodotto;
  int quantita;
  double prezzoUnitario;


  Dettagliocarrello({required this.id, required this.quantita, required this.prezzoUnitario,required this.prodotto});

  factory Dettagliocarrello.fromJson(Map<String, dynamic> json) {
    return Dettagliocarrello(
        id: json['id'],
        prodotto: Prodotto.fromJson(json['prodotto']),
        quantita: json['quantita'],
        prezzoUnitario: json['prezzoUnitario']
    );
  }

  Map<String, dynamic> toJson() => {
    'id': id,
    'prodotto':prodotto.toJson(),
    'quantita': quantita,
    'prezzoUnitario': prezzoUnitario,
  };

  @override
  String toString() {
    return 'Dettagliocarrello{quantita: $quantita, prezzoUnitario: $prezzoUnitario}';
  }
}