import 'package:frontend/model/objects/Prodotto.dart';

class ProdottoPromozione {
  int id;
  int sconto;
  Prodotto prodotto;

  ProdottoPromozione({required this.id, required this.sconto, required this.prodotto});

  factory ProdottoPromozione.fromJson(Map<String, dynamic> json) {
    return ProdottoPromozione(
      id: json['id'],
      sconto: json['sconto'],
      prodotto: Prodotto.fromJson(json['prodotto']),
    );
  }

  Map<String, dynamic> toJson() => {
    'id': id,
    'sconto': sconto,
    'prodotto': prodotto.toJson(),
  };

  @override
  String toString() {
    return 'ProdottoPromozione{sconto: $sconto, prodotto: $prodotto}';
  }
}