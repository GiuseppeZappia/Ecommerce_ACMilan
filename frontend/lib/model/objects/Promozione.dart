import 'package:frontend/model/objects/ProdottoPromozione.dart';

class Promozione {
  int id;
  String nome;
  String dettagli;
  DateTime inizio;
  DateTime fine;
  int attiva;
  List<ProdottoPromozione> prodottiPromozione;

  Promozione({
    required this.id,
    required this.nome,
    required this.dettagli,
    required this.inizio,
    required this.fine,
    required this.attiva,
    required this.prodottiPromozione,
  });

  factory Promozione.fromJson(Map<String, dynamic> json) {
    return Promozione(
      id: json['id'],
      nome: json['nome'],
      dettagli: json['dettagli'],
      inizio: DateTime.fromMillisecondsSinceEpoch(json['inizio']),
      fine: DateTime.fromMillisecondsSinceEpoch(json['fine']),
      attiva: json['attiva'],
      prodottiPromozione: (json['prodottiPromozione'] as List)
          .map((i) => ProdottoPromozione.fromJson(i))
          .toList(),
    );
  }

  Map<String, dynamic> toJson() => {
    'id': id,
    'nome': nome,
    'dettagli': dettagli,
    'inizio': inizio.millisecondsSinceEpoch,
    'fine': fine.millisecondsSinceEpoch,
    'attiva': attiva,
    'prodottiPromozione': prodottiPromozione.map((e) => e.toJson()).toList(),
  };
}