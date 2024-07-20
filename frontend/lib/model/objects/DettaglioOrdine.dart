import 'package:frontend/model/objects/Prodotto.dart';

class DettaglioOrdine {
  int id;
  double prezzoUnitario;
  int quantita;
  Prodotto prodotto;



  DettaglioOrdine({required this.id, required this.prezzoUnitario,required this.quantita,required this.prodotto});

  factory DettaglioOrdine.fromJson(Map<String, dynamic> json) {
    return DettaglioOrdine(
        id: json['id'],
        prezzoUnitario: json['prezzoUnitario'],
        quantita: json['quantita'],
        prodotto: Prodotto.fromJson(json['prodotto'])
    );
  }

  Map<String, dynamic> toJson() => {
    'id': id,
    'prezzoUnitario': prezzoUnitario,
    'quantita': quantita,
    'prodotto':prodotto.toJson()
  };

  @override
  String toString() {
    return 'DettaglioOrdine{prezzoUnitario: $prezzoUnitario, quantita: $quantita, prodotto: $prodotto}';
  }
}