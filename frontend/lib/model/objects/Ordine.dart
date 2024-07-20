class Ordine {
  int id;
  DateTime data;
  double totale;
  int puntiusati;



  Ordine({required this.id, required this.data, required this.totale,required this.puntiusati});

  factory Ordine.fromJson(Map<String, dynamic> json) {
    return Ordine(
        id: json['id'],
        data: DateTime.fromMillisecondsSinceEpoch(json['data']),
        totale: json['totale'],
        puntiusati: json['puntiusati']
    );
  }

  Map<String, dynamic> toJson() => {
    'id': id,
    'data': data.millisecondsSinceEpoch,
    'totale': totale,
    'puntiusati':puntiusati,
  };

  @override
  String toString() {
    return 'Ordine{data: $data, totale: $totale, puntiusati: $puntiusati}';
  }
}